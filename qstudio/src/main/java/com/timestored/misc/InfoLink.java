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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.timestored.TimeStored;
import com.timestored.theme.Theme;

/**
 * A small info icon, that displays popup help and links to a website.
 */
public class InfoLink extends JLabel {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Get an informational link that has text to provide the user with information 
	 * and a clickable link to a website with more information.
	 * @param text Information the user would find relevantly useful.
	 * @param webUrl link to website where user can get more information format: "a.com"
	 * @param showTitleText Whether the link should show title text or just an iconed link.
	 * @return Informational link that has text to provide the user with information 
	 */
	public static JLabel getLabel(String title, String text, final String webUrl, 
			boolean showTitleText) {
		return getLabel(title, text, webUrl, addHttp(webUrl), showTitleText);
	}

	private static String addHttp(final String webUrl) {
		boolean hasHttp = webUrl.toLowerCase().startsWith("http://") || webUrl.toLowerCase().startsWith("https://");
		String actualUrl =  hasHttp ? webUrl : "http://" + webUrl;
		return actualUrl;
	}
	
	/**
	 * Get an informational link that has text to provide the user with information 
	 * and a clickable link to a website with more information.
	 * @param text Information the user would find relevantly useful.
	 * @param webPage webpage that contains more information, used to find URL also.
	 * @param showTitleText Whether the link should show title text or just an iconed link.
	 * @return Informational link that has text to provide the user with information 
	 */
	public static JLabel getLabel(String title, String text, 
			final TimeStored.Page webPage, boolean showTitleText) {
		return getLabel(title, text, webPage.niceUrl(), webPage.url(), true);
	}
	
	/**
	 * @param niceWebUrl Full URL e.g. http://a.com/p?a=10 that will be linked to
	 * @param actualWebUrl URL shown to user as being linked to
	 */
	private static JLabel getLabel(String title, String text, final String niceWebUrl,
			final String actualWebUrl,	boolean showTitleText) {
	
		JLabel l = new JLabel(Theme.CIcon.INFO.get());
		if(text!=null && text.length()>0) {
			l.setToolTipText("<html><b>" + title + "</b>"
					+ "<br><br>" + text
					+ "<br><br><a href='" + actualWebUrl + "' >" + niceWebUrl + "</a></html>");
		}
		if(showTitleText) {
			l.setText(title);
		}
		
		if(HtmlUtils.isBrowseSupported()) {
			l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			l.addMouseListener(new MouseAdapter() {
				@Override public void mouseClicked(MouseEvent e) {
					HtmlUtils.browse(actualWebUrl);
				}
			});
		} 
		return l;
	}
	
//	/**
//	 * @param text Information the user would find relevantly useful.
//	 * @param webUrl link to website where user can get more information format: "a.com/page"
//	 * @return Informational link that has text to provide the user with information 
//	 */
//	public static JButton getButton(String title, String text, final String webUrl) {
//		return getButton(title, text, webUrl, addHttp(webUrl));
//	}

	/**
	 * @param text Information the user would find relevantly useful.
	 * @return Informational link that has text to provide the user with information 
	 */
	public static JButton getButton(String title, String text, final TimeStored.Page webPage) {
		return getButton(title, text, webPage.niceUrl(), webPage.url());
	}

	public static JButton getButton(String title, String text,  
			final String niceWebUrl, final String actualWebUrl) {
		
		JButton b = new JButton(Theme.CIcon.INFO.get());
		if(text!=null && text.length()>0) {
			b.setToolTipText("<html><b>" + title + "</b>"
					+ "<br><br>" + text
					+ "<br><br><a href='" + actualWebUrl + "' >" + niceWebUrl + "</a></html>");
		}
//		if(showTitleText) {
			b.setText(title);
//		}
		
		if(HtmlUtils.isBrowseSupported()) {
			b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			b.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					HtmlUtils.browse(actualWebUrl);					
				}
			});
		} 
		return b;
	}
	
	/**
	 * Show an informational popup message that contains HTML and hyperlinks open a browser.
	 */
	public static void showMessageDialog(Component parent, String htmlBody, String title) {
		
		  // for copying style
		JLabel label = new JLabel();
	    Font font = label.getFont();

	    // create some css from the label's font
	    StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
	    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
	    style.append("font-size:" + font.getSize() + "pt;");

	    String html = "<html><body style=\"" + style + "\">" //
	            + htmlBody  + "</body></html>";
	    JEditorPane ep = Theme.getHtmlText(html);

	    JOptionPane.showMessageDialog(parent, ep, title, JOptionPane.INFORMATION_MESSAGE);
	}

	
}
