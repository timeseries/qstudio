/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2024 TimeStored
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.timestored.qdoc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.timestored.misc.IOUtils;
import com.timestored.qstudio.model.ServerQEntity;

/**
 * Takes a Q file parses it and outputs {@link ParsedQFile}, useful for 
 * creating documentation. Intended only to cover the most basic code and
 * to fail gracefully where possible.
 * 
 * Multiple occurrences of the same fully specified variable name are collapsed into one.
 * This is to allow {@link ServerQEntity}'s to show the associated {@link ParsedQEntity}
 * without having to list lots of separate ones. Not too fussy on handling as devs are 
 * not recommended to write code that way.
 */
public class QFileParser {
	
	/**
	 * ARCHITECTURAL DECISIONS
	 * In the name of efficiency I want to try and make parser one-pass.
	 * The acceptYYYY,parseXXXXXX functions try to
	 * 	1. Quickly decide if the current pos matches their pattern
	 *  2. If so accept it and add to state.
	 *  3. Else reset pos and return false.
	 */
	
	private static final Logger LOG = Logger.getLogger(QFileParser.class.getName());
	private static final String DEFAULT_NS = ".";
	
	private final char[] s;
	private final String srcFilePath;
	private final String srcFileTitle;
	
	private final List<String> commentsBuffer = new ArrayList<String>();
	private final List<ParsedQEntity> qEntities = new ArrayList<ParsedQEntity>();
	private int pos = 0;
	private String curNamespace = DEFAULT_NS;
	private ParsedQFile pqf;
	
	
	private QFileParser(String fullFileContents, 
			String srcFilePath, String srcFileTitle) {
		
		s = fullFileContents.replace("\r\n", "\n").toCharArray();
		this.srcFilePath = srcFilePath;
		this.srcFileTitle = srcFileTitle;
		curNamespace = DEFAULT_NS;
	}
	
	private ParsedQFile parse() {		
		// header comment
		while(pos<s.length && s[pos]=='/') {
			parseCommentsToBuffer();
		}
		ParsedComments headerDoc = ParsedComments.parse(commentsBuffer);
		
		commentsBuffer.clear();
		LOG.fine("headerDoc = " + headerDoc);
		pqf = new ParsedQFile(headerDoc, "",  srcFilePath, srcFileTitle);
		
		
		while(pos<s.length) {
			if(isWhiteSpace(s[pos])) {
				pos++;
			} else if(isNewLine(s[pos])){
				pos++;
				commentsBuffer.clear();
			} else {
				boolean found = parseCommentsToBuffer()
						| parseNamespace()
						| parseAssignment()
						| parseCode();
				if(!found) {
					LOG.fine("nothing found:" + s[pos]);
					pos++;
				}
			}
		}

		combineByFullName(qEntities);
		pqf.setqEntities(qEntities);
		return pqf;
	}

	/**
	 * combine duplicate entries i.e. getFullname matches, then just have a list of offsets
	 */
	private void combineByFullName(List<ParsedQEntity> qEntities) {

		// combine to multiset
		Multimap<String, ParsedQEntity> namespaceToEntities = ArrayListMultimap.create();
		for(ParsedQEntity pqe : qEntities) {
			namespaceToEntities.put(pqe.getFullName(), pqe);
		}
		
		// go through, find multiples, remove them and create new combined one
		for(Collection<ParsedQEntity> nsEntries : namespaceToEntities.asMap().values()) {
			if(nsEntries.size()>1) {
				qEntities.removeAll(nsEntries);
				qEntities.add(combine(nsEntries));
			}
		}
	}

	/**
	 * Combine multiple {@link ParsedQEntity}'s into one by concatenating documentation 
	 * where it makes sense, otherwise use the first value for things like offset.
	 * @param nsEntries The entries that all occur in the same file that need combined.
	 * @return A combined Entity that represents the separate occurrences.
	 */
	private ParsedQEntity combine(Collection<ParsedQEntity> nsEntries) {
		
		Preconditions.checkArgument(nsEntries.size() > 0);
		if(nsEntries.size()==1) {
			return nsEntries.iterator().next();
		}
		
		ParsedQEntity re = nsEntries.iterator().next();
		String doc = "";
		String ret = "";
		int offset = Integer.MIN_VALUE;
		Map<String,String> paramDescriptions = Maps.newHashMap();
		Map<String,String> exceptionDescriptions = Maps.newHashMap();

		final String BR = "<br/>";
		for(ParsedQEntity pqe : nsEntries) {
			// confirm these can be combined
			Preconditions.checkArgument(
					re.getFullName().equals(pqe.getFullName()) && 
					re.getSource().equals(pqe.getSource()));
			if(!pqe.getReturnDescription().isEmpty()) {
				ret = pqe.getReturnDescription();
			}
			String d = pqe.getDocDescription().trim();
			doc += d.isEmpty() ? d : d + BR;
			offset = Math.min(offset, pqe.getOffset());
			paramDescriptions.putAll(pqe.getParamTags());
			exceptionDescriptions.putAll(pqe.getExceptionTags());
		}
		return ParsedQEntity.get(re.getParentFile(), re.getDocName(), re.getNamespace(), 
				doc, paramDescriptions, exceptionDescriptions, ret, offset);
	}

	/**
	 * Attempt to read and parse the selected file, fails silently.
	 */
	public static ParsedQFile parse(File file) {
		String s = "";
		try {
			s = IOUtils.toString(file);
		} catch (IOException e) {
			LOG.log(Level.INFO, "Could not read q file", e);
		}
		return new QFileParser(s, file.getAbsolutePath(), file.getName()).parse();
		
	}
	
	/**
	 * Parse fullFileContents and return parsed form. 
	 * @param fullFileContents The actual q code.
	 * @param srcFilePath Filepath if known otherwise an identifier is OK.
	 * @return Parsed form of q file.
	 */
	public static ParsedQFile parse(String fullFileContents, 
			String srcFilePath, String srcFileTitle) {

		return new QFileParser(fullFileContents, srcFilePath, srcFileTitle).parse();
		
	}
	
	private static boolean parseCode() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean parseAssignment() {
		int p = pos;
		int startPos = pos;
		String varName = getLegalName(s, p);
		p += varName.length();
		
		if(varName.length()>0 && p<s.length && s[p]==':') {
			pos += varName.length() + 1;
			jumpToEndOFStatement();
			ParsedQEntity pqe;
			
			// definition of .b.c even within .a namespace, still makes variable .b.c
			String ns = curNamespace;
			int secondPos = varName.length()>3 ? varName.substring(2).indexOf('.') : -1;
			if(varName.charAt(0) == '.' && secondPos>-1) {
				ns = varName.substring(0, 2+secondPos);
				varName = varName.substring(3+secondPos);
			}
			
			pqe = ParsedQEntity.get(pqf, varName, ns, 
					ParsedComments.parse(commentsBuffer), startPos);
			commentsBuffer.clear();
			qEntities.add(pqe);
			LOG.fine("assignment: " + pqe.toString());
			return true;
		}
		return false;
	}

	private static boolean isLegalNameStarter(char c) {
		return (Character.isLetter(c) || c=='.');
	}

	/** 
	 * 
	 * @return
	 */
	private static String getLegalName(char[] s, int pos) {
		int p=pos;
		if(p>=0 && p<s.length && isLegalNameStarter(s[p])) {
			p++;
			while(p<s.length && (isLegalNameStarter(s[p]) || s[p]=='_' || Character.isDigit(s[p]))) {
				p++;
			}
			return new String(s,pos,p-pos).intern();
		}
		return "";
	}
	
	/** @return true if current pos is equal to txt otherwise false */
	private boolean isPosEqual(String txt) {
		if(pos<s.length-txt.length()) {
			return new String(s,pos,txt.length()).equals(txt);
		}
		return false;
	}

	private void acceptWhitespace() {
		while(pos<s.length && isWhiteSpace(s[pos])) {	pos++;	}
	}

	private void acceptNewline() {
		while(pos<s.length && isNewLine(s[pos])) {	pos++;	}
	}

	private void acceptOneNewline() {
		if(pos<s.length && s[pos]=='\r') { pos++; };
		if(pos<s.length && s[pos]=='\n') { pos++; };
	}
	
	private boolean parseNamespace() {
		
		int startPos = pos;
		String newNS = null;
		
		if(isPosEqual("\\d ")) {
			pos+=3;
			String ns = getLegalName(s, pos);
			pos += ns.length();

			acceptWhitespace();
			if(pos <s.length && isNewLine(s[pos])) {
				acceptNewline();
				newNS = ns;
			}
		} else if(isPosEqual("system")) {
			LOG.fine("parseNamespace - system command found");
			pos+="system".length();
			acceptWhitespace();
			boolean sqBracket = pos<s.length && s[pos]=='[';
			if(sqBracket) {	pos++;	}
			if(isPosEqual("\"d ")) {
				pos+=3;
				String ns = getLegalName(s, pos);
				pos += ns.length();
				acceptWhitespace();
				if(s[pos]=='"') {
					pos++;
					acceptWhitespace();
					if(sqBracket && s[pos]==']') {
						pos++;
					} else {
						LOG.warning("no end sqBracket found. File: " + srcFilePath + " pos = " + pos);
					}
					newNS = ns;
				}
			}
		}

		if(newNS != null) {
			curNamespace = newNS;
			commentsBuffer.clear();
			LOG.fine("curNamespace = " + curNamespace);
			return true;
		}
		
		pos = startPos;
		return false;
	}

	
	private void jumpToEndOFStatement() {
		Stack<Bracket> stack = new Stack<Bracket>();
		for(; pos<s.length; pos++) {
			
			boolean isEnd = (isNewLine(s[pos]) || s[pos]==';' || isPosEqual(" /")) 
					&& stack.size()==0;
			if(isEnd) {
				break;
			}
			
			Bracket b;
			if((b = Bracket.lookupOpen(s[pos])) != null) {
				stack.push(b);
			} else if(stack.size()>0 && ((b = Bracket.lookupClose(s[pos]))!= null)){
				if(b!=stack.pop()) {
					LOG.warning("mismatch in bracket parsing:" + b.toString() + "File: " + srcFilePath + " pos = " + pos);
				}
			}
		}
		
		
	}


	private static boolean isWhiteSpace(char c) {
		return c==' ' || c=='\t';
	}
	
	private static boolean isNewLine(char c) {
		return c=='\r' || c=='\n';
	}
	
	/** 
	 * From the current pos parse as large a comment as possible
	 * @return false if a comment cannot be parsed, true otherwise
	 */ 
	private boolean parseCommentsToBuffer() {
		
		if(s[pos]!='/') {
			return false;
		}
		pos++;
		acceptWhitespace();
		
		if(pos<s.length && isNewLine(s[pos])) { // multi-line comment
			acceptNewline();
			int startOffset = pos;
			while(pos<s.length && !(s[pos]=='\\' && isNewLine(s[pos-1])) ) {
				if(isNewLine(s[pos])) {
					addToCBuffer(startOffset, pos);
					acceptNewline();
					startOffset = pos;
				} else {
					pos++;
				}
			}
			// we reached end of file
			if(pos<s.length && s[pos]=='\\') {
				pos++;
				acceptOneNewline();
			} else if(pos == s.length) {
				addToCBuffer(startOffset, pos);
			}
		} else {	// single line comment
			while(pos<s.length && ((isWhiteSpace(s[pos]) || s[pos]=='/') || s[pos]=='#')) {
				pos++;
			}
			int startOffset = pos;
			while(pos<s.length && !isNewLine(s[pos++]));
			addToCBuffer(startOffset, pos);
//			acceptOneNewline();
		}
		return true;
	}
	
	private void addToCBuffer(int start, int end) {
		if(end > start) {
			String t = new String(s, start, end-start).trim();
			if(t.length()>0) {
				LOG.fine("adding comment to buffer: " + t);
				commentsBuffer.add(t);
			}
		}
	}
	
	

	/**
	 * Opening/Closing pairs
	 */
	private enum Bracket {
		PARENTHESE('(',')'),
		CURLIES('{','}'),
		SQUARE('{','}'),
		SPEECH('{','}');
		
		private final char open;
		private final char close;

		private Bracket(char open, char close) {
			this.open = open;
			this.close = close;
		}
		
		public static Bracket lookupOpen(char opener) {
			for(Bracket b : Bracket.values()) {
				if(b.open == opener) {	return b;	}
			}
			return null;
		}
		
		public static Bracket lookupClose(char closer) {
			for(Bracket b : Bracket.values()) {
				if(b.close == closer) {	return b; }
			}
			return null;
		}
		
		@Override
		public String toString() {
			return this.name();
		}

	}
	
}
