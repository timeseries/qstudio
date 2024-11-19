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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;

import com.timestored.misc.HtmlUtils;
import com.timestored.swingxx.SwingUtils;
import com.timestored.theme.Theme;

/**
 * Dialog similar to eclipses documentation popup that takes the nearest text 
 * in a {@link JEditorPane} and presents relevant docs.
 */
public class DocumentationDialog extends JDialog {

	public static final Dimension PREF_DIMENSION = new Dimension(600, 350);
	
	/**
	 * @param documentedEntity Documentation to show or null will display that none found.
	 */
	public DocumentationDialog(DocumentedEntity documentedEntity) {
		
		setLayout(new BorderLayout());
		JPanel docPanel = getDocPanel(documentedEntity);
		docPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		add(docPanel);
		
		docPanel.getActionMap().put("closeAll", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		docPanel.getInputMap().put(SwingUtils.ESC_KEYSTROKE, "closeAll");
		

		// close when focus lost
		addWindowFocusListener(new WindowFocusListener() {
			@Override public void windowLostFocus(WindowEvent e) {
				dispose();
			}
			@Override public void windowGainedFocus(WindowEvent e) {}
		});
		
		setPreferredSize(PREF_DIMENSION);
		setSize(PREF_DIMENSION);
		setName("documentationDialog");
		// problem is if undecorated have to handle resizing myself
//		setUndecorated(true);
	}

	public static JPanel getDocPanel(DocumentedEntity documentedEntity) {

		JXPanel p = new JXPanel(new BorderLayout());
		JTextPane txtPane = new JTextPane();
		txtPane.setContentType("text/html");
		txtPane.addHyperlinkListener(new HyperlinkListener()
	    {
	        @Override public void hyperlinkUpdate(HyperlinkEvent e) {
	            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
	            	HtmlUtils.browse(e.getURL().toString());
	        }
	    });
		
		if(documentedEntity!=null) {
			p.add(Theme.getSubHeader(documentedEntity.getDocName()), BorderLayout.NORTH);
			txtPane.setText(documentedEntity.getHtmlDoc(false));
		} else {
			p.add(Theme.getSubHeader("No Docs Found"), BorderLayout.NORTH);
			txtPane.setText("<html><body><h3>Could not find any documentation</h3></body></html>");
		}
		
		txtPane.setEditable(false);
		
		p.setBorder(BorderFactory.createRaisedBevelBorder());
		p.setScrollableTracksViewportWidth(true);
		p.setScrollableWidthHint(ScrollableSizeHint.FIT);

		JScrollPane scrollPane = new JScrollPane(txtPane, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		txtPane.setCaretPosition(0);
		
		p.add(scrollPane, BorderLayout.CENTER);
		return p;
	}

	/**
	 * Get the text that would represent this {@link DocumentedEntity} as a tooltip,
	 * Should be very similar to what is shown in {@link #getDocPanel(DocumentedEntity)}
	 * @param documentedEntity The entity you want tooltip text for, may be null if none found.
	 * @return Text that would represent this {@link DocumentedEntity} as a tooltip or null if nothing useful found.
	 */
	public static String getTooltip(DocumentedEntity documentedEntity) {
		String s = HtmlUtils.extractBody(documentedEntity.getHtmlDoc(true).trim()).trim();;
		if(s.length() > 0) {
			return HtmlUtils.START + "<div width='300px'>" +
					"<h2>" + documentedEntity.getDocName() + "</h2>" + 
					 s + "<br/></div>" + HtmlUtils.END;
		}
		return null;
	}

	
}
