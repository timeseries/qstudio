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
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.timestored.TimeStored.Page;
import com.timestored.docs.Document;
import com.timestored.misc.HtmlUtils;
import com.timestored.misc.IOUtils;

/**
 * Converts a {@link ParsedQFile} to formatted HTML output similar to javadoc.
 */
public class HtmlPqfOutputter {

	public static final String CSS_LINK = "<link rel=\"stylesheet\" href=\"qdoc.css\" type=\"text/css\" media=\"screen\" />";

	private static final String QDOC_LINK = "<a class='qlogo' href='" + Page.QDOC.url() + "' target='a'>q<span>Doc</span></a>";
	
	private static final String FILE_SUFFIX = ".html";

	private static final Logger LOG = Logger.getLogger(HtmlPqfOutputter.class.getName());
	private static final String C = ";";
	
	/** 
	 * The maximum string length of the short entity description in the 
	 * overview table. 
	 */
	private static final int SD_LENGTH = 60;


	private HtmlPqfOutputter() {	}
	

	/**
	 * Save documentation as HTMl to selected directory.
	 * @param documents The documents to output documentation for.
	 * @param outdir The directory to write to, any existing files will be overwritten
	 * @param baseWeblink The beginning of a URL that is added to to allow sending a webQuery showing example function calls. Can be null.
	 * @return list of error descriptions for end user, no errors = empty
	 */
	public static List<String> output(List<Document> documents, File outdir, String baseWeblink) {

		final List<String> errors = Lists.newArrayList();
		final String destdir = outdir.getPath() + File.separator;
		
		if(!outdir.isDirectory()) {	
			addLogError(errors, "Could not output documentation to " + destdir
					+ "\r\nIt is not a valid directory.");
			return errors;
		}
		
		if(outdir.list().length > 0) {
			LOG.warning("directory is not empty!");
		}
		
		// output each HTML file
		List<ParsedQFile> writtenDocs = Lists.newArrayList();
		for(Document d : documents) {
			try {
				String filename;
				filename = destdir + d.getTitle() + FILE_SUFFIX;
				ParsedQFile pqf = QFileParser.parse(d.getContent(), d.getFilePath(), d.getTitle());
				String html = generateHTML(pqf, baseWeblink);
				IOUtils.writeStringToFile(html, new File(filename));
				writtenDocs.add(pqf);
			} catch (IOException e) {
				addLogError(errors, "Could not output documentation for " + d.getFilePath());
			}
		}
		
		// generate allclasses listing to show namespaces/files
		try {
			String frameSetHtml = generateIndexListing(writtenDocs);
			File framesetFile = new File(destdir + "allclasses-frame.html");
			IOUtils.writeStringToFile(frameSetHtml, framesetFile);
		} catch (IOException e) {
			addLogError(errors, "Could not output allclasses-frame.html");
		}

		try {
			FileWriter fw = new FileWriter(new File(destdir, "manlisting.q"));
			generateQhelpTable(writtenDocs, fw);
			fw.close();
		} catch (IOException e) {
			addLogError(errors, "manlisting.q");
		}
		
		// generate package-summary.html
		try {
			String fileSummaryHTML = generateFileSummaryHtml(writtenDocs);
			File dfile = new File(destdir + "package-summary.html");
			IOUtils.writeStringToFile(fileSummaryHTML, dfile);
		} catch (IOException e) {
			addLogError(errors, "Could not output package-summary.html");
		}
		
		// index
		try {
			String index = IOUtils.toString(HtmlPqfOutputter.class, "index.html");
			IOUtils.writeStringToFile(index, new File(destdir + "index.html"));
			saveQdocCssTo(destdir);
		} catch (IOException e) {
			addLogError(errors, "Could not output index.html");
		}
		return errors;
	}
	
	public static void saveQdocCssTo(String destdir) throws IOException {
		String d = destdir.endsWith(File.separator) ? destdir : destdir + File.separator;
		String css = IOUtils.toString(HtmlPqfOutputter.class, "qdoc.css");
		IOUtils.writeStringToFile(css, new File(d + "qdoc.css"));
	}
	
	/**
	 * @return File summary that is on right hand side of qdoc frameset usually. Giving
	 * 		a summary of each file.
	 */
	private static String generateFileSummaryHtml(List<ParsedQFile> pqfiles) {

		// construct output HTML from parts
		StringBuilder sb = new StringBuilder();
		sb.append(HtmlUtils.getTSPageHead("Index Listing", QDOC_LINK, CSS_LINK, true));
		sb.append("<h1>Files</h1>");

		Collections.sort(pqfiles, new Comparator<ParsedQFile>() {
			@Override public int compare(ParsedQFile o1, ParsedQFile o2) {
				return o1.getFileTitle().compareTo(o2.getFileTitle());
		}});
		
		// file links
		sb.append("<table  class='overviewSummary'>");
		sb.append("<tr><th>File</th><th>Description</th>");
		
		for(ParsedQFile pqf : pqfiles) {
			String fn = pqf.getFileTitle() + FILE_SUFFIX;
			sb.append("<tr><td>");
			makeLink(fn, pqf.getFileTitle());
			sb.append(makeLink(fn, pqf.getFileTitle()));
			sb.append("</td><td>");
			sb.append(pqf.getHeaderDoc());
			sb.append("</td></tr>");
		}
		sb.append("</table>");

		sb.append(HtmlUtils.getTSPageTail(QDOC_LINK));
		return sb.toString();
	}
	
	/** ml for make link */
	private static String makeLink(String href, String txt) {
		return "<a href='" + href + "' target='classFrame' >" + txt + "</a>";
	}
	
	private static String esc(String s) {
		if(s == null) {
			return "";
		}
		return "\"" + s.replace("\"", "\\\"") + "\"";
	}

	private static void generateQhelpTable(List<ParsedQFile> pqfiles, FileWriter w) throws IOException {
		for(ParsedQFile pqFile : pqfiles) {
			for(ParsedQEntity parsedQentity : pqFile.getQEntities()) {
				w.write(".man.registerFunc (");
				w.write(esc(parsedQentity.getFullName()));
				w.write(C);
				w.write(esc(parsedQentity.getNamespace()));
				w.write(C);
				w.write(esc(parsedQentity.getDocDescription()));
				w.write(C);
				w.write(esc(parsedQentity.getDocName()));
				w.write(C);
				String eg = parsedQentity.getTag("eg");
				if(eg == null) {
					eg = parsedQentity.getTag("example");
				}
				w.write(esc(eg == null ? "" : eg));
				w.write(");\r\n");
				
				for(Entry<String, Map<String, String>> namedTags : parsedQentity.getNamedTags().entrySet()) {
					for(Entry<String, String> paramDescriptionPair : namedTags.getValue().entrySet()) {
						writeArg(w, parsedQentity.getFullName(), namedTags.getKey(), 
								paramDescriptionPair.getKey(), paramDescriptionPair.getValue());
					}
				}
				for(Entry<String, String> tags : parsedQentity.getTags().entrySet()) {
					writeArg(w, parsedQentity.getFullName(), tags.getKey(), "", tags.getValue());
				}
			}
		}
	}


	private static void writeArg(FileWriter w, String name, String tag, String subTag,
			String description) throws IOException {
		w.write(".man.registerArg (");
		w.write(esc(name));
		w.write(C);
		w.write(esc(tag));
		w.write(C);
		w.write(esc(subTag));
		w.write(C);
		w.write(esc(description));
		w.write(");\r\n");
	}
	
	/**
	 * Generate HTML page that lists files and namespaces for all files in our list.
	 * This page is usually found in left side of qdocs and is used for navigation.
	 * @param pqfiles
	 * @return HTML page that lists files and namespaces for all files in our list.
	 */
	private static String generateIndexListing(List<ParsedQFile> pqfiles) {
		
		// file links
		List<String> fileLinks = Lists.newArrayList();
		for(ParsedQFile pqf : pqfiles) {
			String fn = pqf.getFileTitle() + FILE_SUFFIX;
			fileLinks.add(makeLink(fn, pqf.getFileTitle()));
			Collections.sort(fileLinks);
		}
		
		// build map from namespace's to distinct files
		Multimap<String, String> namespaceToFiles = ArrayListMultimap.create();
		for(ParsedQFile pqf : pqfiles) {
			for(ParsedQEntity pqe : pqf.getQEntities()) {
				Collection<String> files = namespaceToFiles.get(pqe.getNamespace());
				if(files==null || !files.contains(pqf.getFileTitle())) {
					namespaceToFiles.put(pqe.getNamespace(), pqf.getFileTitle());
				} 
			}
		}
		
		// generate html of links
		// have to be careful of namespaces that may occur in more than one file
		List<String> nsLinks = Lists.newArrayList();
		List<String> nsListing = Lists.newArrayList(namespaceToFiles.asMap().keySet());
		Collections.sort(nsListing);
		for(String ns : nsListing) {
			List<String> files = new ArrayList<String>(namespaceToFiles.get(ns));
			if(files.size()>1) {
				List<String> fileListing = Lists.newArrayList();
				for(String fileTitle : files) {
					fileListing.add(makeLink(fileTitle + FILE_SUFFIX, fileTitle));
				}
				nsLinks.add(ns + ": " + HtmlUtils.toList(fileListing));
			} else {
				String fn = files.get(0) + FILE_SUFFIX;
				nsLinks.add(makeLink(fn, ns));
			}
		}
		
		// construct output HTML from parts
		StringBuilder sb = new StringBuilder();
		String subTitleLink = "<a href='package-summary.html' target='classFrame' >Home</a> - " + QDOC_LINK;
		sb.append(HtmlUtils.getTSPageHead("Index Listing", subTitleLink, CSS_LINK, true));
		sb.append("<h1>Namespaces</h1>");
		sb.append(HtmlUtils.toList(nsLinks));
		sb.append("<h1>Files</h1>");
		sb.append(HtmlUtils.toList(fileLinks));
		sb.append(HtmlUtils.getTSPageTail(QDOC_LINK));
		return sb.toString();
	}
	
	private static void addLogError(final List<String> errors, String msg) {
		LOG.severe(msg);
		errors.add(msg);
	}

	/**
	 * @return Full HTML page for one {@link ParsedQFile} showing all docs/entities.
	 */
	static String generateHTML(ParsedQFile pqf) {
		return generateHTML(pqf, null);
	}
	
	/**
	 * @param baseWeblink The beginning of a URL that is added to to allow sending a webQuery showing example function calls. Can be null.
	 * @return Full HTML page for one {@link ParsedQFile} showing all docs/entities. 
	 */
	static String generateHTML(ParsedQFile pqf, String baseWeblink) {
		StringBuilder s = new StringBuilder();
		final String title = pqf.getFileTitle();
		s.append(HtmlUtils.getTSPageHead(title, QDOC_LINK, CSS_LINK, true));
			s.append("\r\n\t<div id='headerDoc'>");
			s.append(pqf.getHeaderDoc());
			s.append(HtmlUtils.toList(pqf.getHeaderTags(), true));
			
			s.append("</div>");
			
			s.append("\r\n\t<div id='summary'>");
			s.append("<h2>Entity Summary</h2>");
			appendTableOverview(s, pqf);
			s.append("</div>");
			
			s.append("\r\n\t<div class='details'>");
			s.append("\r\n\t<h2>Entity Details</h2>");
			appendEntityDetails(s, pqf, baseWeblink);
			s.append("</div>");
		s.append(HtmlUtils.getTSPageTail(QDOC_LINK));
		return s.toString();
	}

	/** @return true iff the documented entity is for internal use only */
	private static boolean isInternal(ParsedQEntity e) {
		return e.getDocName().contains(".i.");
	}
	
	/** @return true if more than just name/namespace is known about this entity. */
	private static boolean hasDetails(ParsedQEntity e) {
		return !e.getDocDescription().equals(e.getShortDescription()) 
				|| e.getTags().size()>0
				|| e.getNamedTags().size()>0;
	}

	/**
	 * For a given entity return a unique HTML id.
	 */
	private static String getHtmlId(ParsedQEntity e) {
		return e.getDocName().replace('.', '-');
	}
	

	private static void appendEntityDetails(StringBuilder s, ParsedQFile pqf, String baseWeblink) {
		for(ParsedQEntity e : pqf.getQEntities()) {
			if(hasDetails(e) && !isInternal(e)) {
				s.append("\r\n\t\t<div class='entity' id='"+getHtmlId(e)+"'>");
				s.append("<h2>");
				if(baseWeblink!= null && baseWeblink.length()>0) {
					String args = "/" + e.getDocDescription() + "\r\n" + e.getDocName();
					try {
						args = URLEncoder.encode(args, "UTF-8");
					} catch (UnsupportedEncodingException e1) {}
					String anchor = "<a href='" + baseWeblink + "?" + args + "' target='a'>";
					s.append(anchor  + e.getDocName()).append("</a>");
				} else {
					s.append(e.getDocName());
				}
				s.append("</h2>");
				s.append(HtmlUtils.extractBody(e.getHtmlDoc(false, baseWeblink)));
				s.append("</div>");
			}
		}
	}

	private static void appendTableOverview(StringBuilder s, ParsedQFile pqf) {

		s.append("<table class='overviewSummary'><tbody>");
		s.append("<tr><th>Entities</th><th>Short Description</th></tr>");
		
		for(ParsedQEntity e : pqf.getQEntities()) {
			// Ignore internal functions, we don't want to generate HTML for them
			if(!isInternal(e)) {
				s.append("\r\n\t\t<tr><td>");
				if(hasDetails(e)) {
					s.append("<a href='#"+getHtmlId(e)+"'>");
					s.append(e.getDocName());
					s.append("</a>");
				} else {
					s.append(e.getDocName());
				}
				s.append("</td><td>");
				String sd = e.getShortDescription();
				s.append(sd.length()<=SD_LENGTH ? sd : sd.substring(0, SD_LENGTH));
				s.append("</td></tr>");
			}
		}
		s.append("\r\n</tbody></table>");
	}



	private static final String TAIL = "</body></html>";
	
}
