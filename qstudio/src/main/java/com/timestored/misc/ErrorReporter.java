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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.IllegalComponentStateException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.google.common.base.Preconditions;
import com.timestored.theme.Theme;

/**
 * Reports errors the the user and presents them with option to goto website or email.
 */
public class ErrorReporter {

	private static final Logger LOG = Logger.getLogger(ErrorReporter.class.getName());
	
	private final String websiteUrl; 
	private final String email;
	private final String emailTitle;
	private final long timeDelayBetweenReports;
	
	private long lastErrTime = 0;
	

	/**
	 * 
	 * @param websiteUrl The URL that will be used to contact and send error reports.
	 * 			It is assumed that the url ends with ?blah=blah and that when the user is
	 * 			redirected details will be appended with &amp;details=blah.
	 * @param email Alternative for user to email.
	 * @param delayBetweenReports delay in minutes between which reports won't be shown,
	 * 	useful to prevent bombarding the user with repeated errors.
	 */
	public ErrorReporter(String websiteUrl, String email, String emailTitle, int delayBetweenReports) {

		Preconditions.checkNotNull(websiteUrl);
		Preconditions.checkNotNull(email);
		Preconditions.checkNotNull(emailTitle);
		Preconditions.checkArgument(websiteUrl.contains("?"));
		Preconditions.checkArgument(delayBetweenReports >= 0);
		
		this.websiteUrl = websiteUrl;
		this.email = email;
		this.emailTitle = emailTitle;
		// conver to milliseconds
		this.timeDelayBetweenReports = 60000 * delayBetweenReports;
		
	}
	
	/**
	 * @return exception handler that allows reporting errors via email or website url.
	 */
	public UncaughtErrorReporter getUncaughtExceptionHandler() {
		return new UncaughtErrorReporter();
	}
	
	
	private class UncaughtErrorReporter implements Thread.UncaughtExceptionHandler {
		
		@Override public void uncaughtException(Thread t, Throwable e) {
			
			boolean showException = true;
			String m = e.getMessage();
			
			// List of known issues that seem to recur but are not actually that bad
			if(e instanceof IllegalComponentStateException) {
				if(m.contains("component must be showing")) {
					showException = false;
				}
			}
			
			LOG.log(Level.WARNING, "uncaught error", e);
			if(showException) {
				showReportErrorDialog(e, null);
			}
		}
	}

	/**
	 * If it has been a significant period since last user error dialog then 
	 * present a dialog to the user with a report button that takes them to the website.
	 * @param description The technical details of the error, place in message sent.
	 * @param e The exceptions whos stack trace will be sent or null if none available.
	 */
	public void showReportErrorDialog(Throwable e, String description) {

		showReportErrorDialog(getErrDetails(e, description));
	}

	
	private String getErrDetails(Throwable e, String description) {
		
		// get the error details to send
		String stackTrace = "..";
		if(e != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			stackTrace = sw.toString();
		}
		
		String version = System.getProperty("java.version");
		String os = System.getProperty("os.name");
		
		return "\r\n\r\nDetails:\r\n"
				+ "\r\nOS=" + (os == null ? "unknown" : os)
				+ "\r\nJava=" + (version == null ? "unknown" : version)
				+ "\r\nDes=" + (description == null ? "unknown" : description)
				+ "\r\nStack=" + stackTrace;
	}
	

	/**
	 * If it has been a significant period since last user error dialog then 
	 * present a dialog to the user with a report button that takes them to the website.
	 * @param errDetails The technical details of the error, place in message sent.
	 */
	public void showReportErrorDialog(String errDetails) {
		
		boolean enoughTimeDelay = System.currentTimeMillis() - lastErrTime > timeDelayBetweenReports;
		
		if(enoughTimeDelay) {
			lastErrTime = System.currentTimeMillis();
			
			final String msg = "An error occurred, to allow us to fix the problem please click " +
					"report below which will contact us via the website or email  " 
					+ email + "\r\n\r\nTechnical Details:\r\n";
			
			JPanel b = new JPanel(new BorderLayout());
			
			b.add(Theme.getTextArea("repError", msg), BorderLayout.NORTH);
			
	
			JTextArea errTA = Theme.getTextArea("errDetails", errDetails);
			errTA.setFont(new Font("Verdana", Font.BOLD, 12));
			errTA.setForeground(Color.GRAY);
			errTA.setWrapStyleWord(false);
			errTA.setLineWrap(false);
			
			b.add(new JScrollPane(errTA), BorderLayout.CENTER);
			b.setPreferredSize(new Dimension(400, 300));
	
			String[] options = new String[] {"Report","Close"};
			
			int choice = JOptionPane.showOptionDialog(null, b, "Error", JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.ERROR_MESSAGE, Theme.CIcon.ERROR.get(), 
					options, options[0]);
			
			if(choice == JOptionPane.YES_OPTION) {
				try {
					HtmlUtils.browse(websiteUrl + "&details=" + URLEncoder.encode(errDetails, "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					LOG.log(Level.FINE, "no luck");
				} 
			}
		}
	}
	
	
	private static String getEncoded(String s, int maxUnencodedLength) {
		try {
			String t = s.substring(0, Math.min(s.length(), maxUnencodedLength));
			return URLEncoder.encode(t, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			LOG.log(Level.FINE, "no luck");
		} 
		return "";
	}
	
	/**
	 * Return a small text area with a short description of the problem and a button that
	 * when clicked presents the user with the option to report full technical details.
	 * @return Component with text describing problem and buttons/links to let user report.
	 */
	public Component getErrorReportLink(Throwable e, String description) {
		String txt = "" + description + "<br /><font color='red'>" + e.toString() + "<br /></font>";
		return getErrorReportLink(txt, getErrDetails(e, description));
	}

	/**
	 * Return a small text area with a short description of the problem and a button that
	 * when clicked presents the user with the option to report full technical details.
	 * @param errDetails The exceptions whos stack trace will be sent or null if none available.
	 * @return Component with text describing problem and buttons/links to let user report.
	 */
	public Component getErrorReportLink(String shortDescription, String errDetails) {
		
		// trim as has to fit in URL
		final String encodedSubject = getEncoded(emailTitle, 30);
		final String encodedDetails = getEncoded(errDetails, 435);
		
		JEditorPane editPane = Theme.getHtmlText("<html>" + shortDescription
				+ "<br />If you believe this is a bug contact:" +
				"<br /><a href='mailto:" + email + "?Subject=" + encodedSubject + 
				"&Body=" + encodedDetails + "'>" + email + 
				"</a> to report the problem please.</html>");
		
		JButton reportButton = new JButton("Report via Website", Theme.CIcon.TEXT_HTML.get16());
		
		reportButton.addActionListener(new ActionListener() {
			
			@Override public void actionPerformed(ActionEvent e) {
				HtmlUtils.browse(websiteUrl + "&details=" + encodedDetails);
			}
		});
		

		JPanel p = new JPanel(new BorderLayout());
		p.add(editPane, BorderLayout.CENTER);
		p.add(reportButton, BorderLayout.EAST);
		JPanel c = new JPanel();
		c.add(p);
		return c;
	}
}
