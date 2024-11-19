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
package com.timestored.command;

import static com.timestored.swingxx.SwingUtils.ESC_KEYSTROKE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Miscellaneous utilites that were in other classes but moved here
 * so that Command has no external dependencies.
 */
class Utils {
	
	/*
	 * Combination of SwingUtils, HtmlUtils, Theme from my existing codebase
	 */
	
	private static final Logger LOG = Logger.getLogger(Utils.class.getName());
	private static final boolean browseSupported;
	private static final int GAP = 4;
	private static final Border CENTRE_BORDER = BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP);

	static {
		boolean browsey = false;
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				browsey = true;
			}
		}
		browseSupported = browsey;
	}
	
	public static void addEscapeCloseListener(final JDialog dialog) {
		dialog.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					
				    WindowEvent wev = new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING);
				    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
				}
				super.keyPressed(e);
			}
		});
	}
	
	/**
	 * Add an action that is added to the inputmap/action map for this component
	 * when the escape key is pressed.
	 */
	public static void putEscapeAction(JComponent searchTextField, Action action) {
		ActionMap am = searchTextField.getActionMap();
		InputMap im = searchTextField.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		am.put("escapeAction", action);
		im.put(ESC_KEYSTROKE, "escapeAction");
	}

	/**
	 * Try to open a url in the system associated web browser if possible
	 * @return true if it seemed to launch browser ok, false otherwise.
	 */
	public static boolean browse(String url) {
		if(browseSupported) {
			try {
				Desktop.getDesktop().browse(new URI(url));
				return true;
			} catch (IOException e) {
				LOG.log(Level.WARNING, "couldn't open browser", e);
			} catch (URISyntaxException e) {
				LOG.log(Level.WARNING, "couldn't open browser", e);
			}
		}
		return false;
	}
	

	public static JPanel getSubHeader(final String title, final Color foregroundColor, 
			final Color backgroundColor) {
		
		JPanel outPanel = new JPanel(new BorderLayout());
		outPanel.setBorder(BorderFactory.createEmptyBorder(GAP, 0, (int)(GAP*1.5), 0));
		JPanel headerPanel = new JPanel(new BorderLayout());
		JLabel titleLabel = new JLabel(title);
		Font cf = titleLabel.getFont();
		titleLabel.setFont(new Font(cf.getName(), Font.BOLD, cf.getSize() + 3));
		if(foregroundColor!=null) {
			titleLabel.setForeground(foregroundColor);
		}
		titleLabel.setBorder(CENTRE_BORDER);
		headerPanel.add(titleLabel, BorderLayout.CENTER);
		if(backgroundColor!=null) {
			headerPanel.setBackground(backgroundColor);
		}
		outPanel.add(headerPanel, BorderLayout.NORTH);
		return outPanel;
	}

}
