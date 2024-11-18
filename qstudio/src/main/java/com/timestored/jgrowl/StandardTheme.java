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
package com.timestored.jgrowl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.Border;

/**
 * Default appearance of Growl messages and how quickly they fade etc.
 */
class StandardTheme implements Theme {

	private static final Logger LOG = Logger.getLogger(Theme.class.getName());
	
	/** space between top ofparentFrame and top of first message */
	private static final int TOP_SPACER = 150;
	private static final int BORDER_WIDTH = 1;
	
	/** width of messages */
	private static final int MESSAGE_WIDTH = 400;
	/** space between right border of parentFrame and right edge of message items */
	private static final int RIGHT_SPACER = 77;
	private static final int PAD = 3;

	public static enum Icon {
		INFO("dialog-information.png"), 
		WARNING("dialog-warning.png"), 
		SEVERE("dialog-error.png");

		private final ImageIcon imageIcon;
		private final ImageIcon imageIcon16;
		public final ImageIcon imageIcon32;

		/** @return Default sized imageIcon */
		public ImageIcon get() {
			return imageIcon;
		}
		
		/** @return Size 16*16 imageIcon */
		public ImageIcon get16() {
			return imageIcon16;
		}
		
		/** @return Size 32*32 imageIcon */
		public ImageIcon get32() {
			return imageIcon32;
		}
		
		Icon(String loc) {
			ImageIcon ii = null;
			ImageIcon ii16 = null;
			ImageIcon ii32 = null;
			try {
				ii = new ImageIcon(Theme.class.getResource(loc));
				Image i = ii.getImage();
				ii16 = new ImageIcon(i.getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
				ii32 = new ImageIcon(i.getScaledInstance(32, 32, Image.SCALE_AREA_AVERAGING));
				
			} catch(Exception e) {
				LOG.log(Level.WARNING, "missing icon image", e);
			}
			imageIcon = ii;
			imageIcon16 = ii16;
			imageIcon32 = ii32;
		}
	}
		
	// TODO find where this space is coming from programmatically
	/** unknown space coming from where? */
	private static final int SPACE_HACK = 75;

		
	/** {@inheritDoc} */
	@Override public long getFadeTimerDelay() {
		return 50;
	}

	/** {@inheritDoc} */ 
	@Override public int getSpaceBetweenItems() {
		return 5;
	}

	/** {@inheritDoc} */
	@Override public int getMoveSpeed() {
		return 10;
	}
	
	/** {@inheritDoc} */
	@Override public float getFadeRate() {
		return 0.01f;
	}


	/** {@inheritDoc} */
	@Override public int getTopSpacer() {
		return TOP_SPACER;
	}
	
	/** {@inheritDoc} */
	@Override public int getFadeRangeMinimum() {
		return 200;
	}

	/** {@inheritDoc} */
	@Override public int getLeftRuler(JFrame parentFrame) {
		return (parentFrame.getX() + parentFrame.getWidth())
				- (MESSAGE_WIDTH + RIGHT_SPACER);
	}

	private static  Border getBorder(Growl growl, boolean hover) {

		// color depends on level / hover
		Color c = Color.LIGHT_GRAY;
		int ll = growl.getLogLevel().intValue();
		if(ll >= Level.SEVERE.intValue()) {
			c = Color.RED;
		} else if(ll >= Level.WARNING.intValue()) {
			c = Color.ORANGE;
		} 
		if(hover) {
			c = c.darker();
		}

		// titled or not depending if there is a title
		Border b = BorderFactory.createLineBorder(c, BORDER_WIDTH);
		if(growl.getTitle()==null) {
			BorderFactory.createLineBorder(c, 0);
		} else if(growl.getTitle().length()==0) {
			b = BorderFactory.createTitledBorder(b, growl.getTitle());
		}
		return b;
	}
	
	static JLabel getLabelWithFixedWidth(String msg, int adjustment) {
		JLabel label = new JLabel("<html><body style='width:" 
				+ ((MESSAGE_WIDTH-BORDER_WIDTH-SPACE_HACK)+adjustment) + "px'>" 
				+ msg + "</body></html>");
		label.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
		label.setMaximumSize(new Dimension(MESSAGE_WIDTH-BORDER_WIDTH, Integer.MAX_VALUE));
		return label;
	}
	
	/** {@inheritDoc} */
	@Override public Window getWindow(Growl message, final JFrame parentFrame) {
		
		final JWindow frame = new JWindow();
		new BoxLayout(frame, BoxLayout.PAGE_AXIS);
		frame.setAlwaysOnTop(parentFrame.isFocused());
		frame.setMaximumSize(new Dimension(MESSAGE_WIDTH, Integer.MAX_VALUE));

		final JPanel panel = new JPanel();
		new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		Component c = message.getMessagePanel();
		if(c == null) {
			c = getLabelWithFixedWidth(message.getMessage(), 0);;
		} else {
			c.setMaximumSize(new Dimension(MESSAGE_WIDTH-BORDER_WIDTH, Integer.MAX_VALUE));
			c.setMinimumSize(new Dimension(22, 22));
		}

		final Border border = getBorder(message, false);
		final Border hoverBorder = getBorder(message, true);
		panel.setBorder(border);
		
		/*
		 * Behaviour of window while hovering etc.
		 */
		frame.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				parentFrame.toFront();
				super.mousePressed(e);
			}
			
			@Override public void mouseEntered(MouseEvent e) {
				panel.setBorder(hoverBorder);
				super.mouseEntered(e);
			}
			
			@Override public void mouseExited(MouseEvent e) {
				panel.setBorder(border);
				super.mouseExited(e);
			}
		});
		
		panel.add(c);
		
		frame.add(panel);
		frame.pack();
		frame.setMinimumSize(new Dimension(MESSAGE_WIDTH, 0));
		frame.setVisible(true);
		return frame;
	}
	
	
}
