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
package com.timestored.misc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

/**
 * Allows running commands as if at the terminal. 
 */
public class CmdRunner {

	private static final Logger LOG = Logger.getLogger(CmdRunner.class.getName());

	/**
	 * @param envp array of strings, each element of which has environment variable settings in the format name=value, or null if the subprocess should inherit the environment of the current process. 
	 * @param dir the working directory of the subprocess, or null if the subprocess should inherit the working directory of the current process.
	 * @return The result of running the command.
	 */
	public static String run(String commands[], String[] envp, File dir) throws IOException {
		Process p = Runtime.getRuntime().exec(commands, envp, dir);
		LOG.info("getRuntime().exec " + commands);
		return waitGobbleReturn(p);
	}

	private static String waitGobbleReturn(Process p) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		new Thread(new StreamGobbler(p.getInputStream(), ps)).start();
		new Thread(new StreamGobbler(p.getErrorStream(), ps)).start();
		try {
			p.waitFor();
			return baos.toString("utf-8");
		} catch (InterruptedException e) { 
			LOG.log(Level.SEVERE, "CmdRunner Run Error", e);
		} catch(UnsupportedEncodingException e) {
			LOG.log(Level.SEVERE, "CmdRunner Run Error", e);
		}
		return "";
	}
	
	/**
	 * @param commands Terminal commands where each is a separate string in an array.
	 * @return The result of running the command.
	 */
	public static String run(String[] commands) throws IOException { return run(commands, null, null); }

	/**
	 * @param command Terminal command.
	 * @return The result of running the command.
	 */
	public static String run(String command) throws IOException { 
		Process p = Runtime.getRuntime().exec(command, null, null);
		LOG.info("getRuntime().exec " + command);
		return waitGobbleReturn(p);
	}

	/** 
	 * @param command a specified system command
	 * @param dir the working directory of the subprocess, or null if the subprocess should inherit the working directory of the current process.
	 * @return The result of running the command.
	 */
	public static Process startProc(String command, File dir) throws IOException {
		Process p = Runtime.getRuntime().exec(command, null, dir);
		gobbleStreams(p);
		return p;
	}
	
	/**
	 * @param commands a specified system command
	 * @param envp array of strings, each element of which has environment variable settings in the format name=value, or null if the subprocess should inherit the environment of the current process. 
	 * @param dir the working directory of the subprocess, or null if the subprocess should inherit the working directory of the current process.
	 */
	public static Process startProc(String[] commands, String[] envp, File dir) throws IOException {
		Process p = Runtime.getRuntime().exec(commands, envp, dir);
		LOG.info("getRuntime().exec " + Joiner.on(' ').join(commands));
		gobbleStreams(p);
		return p;
	}

	private static void gobbleStreams(Process p) {
		new Thread(new StreamGobbler(p.getInputStream(), System.out)).start();
		new Thread(new StreamGobbler(p.getErrorStream(), System.err)).start();
	}


	/** Consumes streams to ensure that process completes. */
	private static class StreamGobbler implements Runnable {
	
		private InputStreamReader isr;
		private PrintStream ps;
		
		public StreamGobbler(InputStream is, PrintStream outputPS) {
	        isr = new InputStreamReader(is);
	        this.ps = Preconditions.checkNotNull(outputPS);
		}
		
		@Override public void run() {
	        try {
	        	BufferedReader br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null) {
					ps.println(line);
				}    
	        } catch (IOException ioe) {
	            ioe.printStackTrace();  
	        }
	    }
	}

	private static final String REG_EXP = "\"(\\\"|[^\"])*?\"|[^ ]+";
	private static final Pattern PATTERN = Pattern.compile(REG_EXP, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

	/** 
	 * Convert string to separate arguments similar to what the OS usually does
	 * i.e. careful of quotes. 
	 **/
	public static String[] parseCommand(String cmd) {
		if (cmd == null || cmd.length() == 0) {
			return new String[] {};
		}

		cmd = cmd.trim();
		Matcher matcher = PATTERN.matcher(cmd);
		List<String> matches = new ArrayList<String>();
		while (matcher.find()) {
			String s = matcher.group();
			if(s.length()>=2) {
				boolean hasQuotes = (s.charAt(0)=='"' && s.charAt(s.length()-1)=='"')
						|| (s.charAt(0)=='\'' && s.charAt(s.length()-1)=='\'');
				if(hasQuotes) {
					s = s.substring(1, s.length()-1);
				}
			}
			matches.add(s);
		}
		String[] parsedCommand = matches.toArray(new String[] {});
		return parsedCommand;
	}
}
