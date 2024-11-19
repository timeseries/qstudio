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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;

/**
 * Renders {@link Command} in list with icons
 */
class CommandRenderer extends JLabel implements ListCellRenderer {

	private static final CommandRenderer INSTANCE = new CommandRenderer();

	private static final long serialVersionUID = 1L;

	public static CommandRenderer getInstance() {
		return INSTANCE;
	}
	
	private CommandRenderer() {}
	
	@Override public Component getListCellRendererComponent(JList list, Object value, int index, 
			boolean isSelected, boolean cellHasFocus) {

		JPanel p = new JPanel(new BorderLayout());
		p.add(this, BorderLayout.WEST);
		
		// show color / icon if it's a Suggestion (it should be)
		if (value instanceof Command) {
			Command c = (Command) value;
		
			if (c.getIcon() != null) {
				setIcon(c.getIcon());
			}
			
			// set title displayed, show additional in light grey if there's space
			String t = c.getTitle();
			String ta = c.getTitleAdditional();
			if(ta != null && ta.length() > 0 && ta.length() < 60) {
				t = "<html>" + c.getTitle() + "<font color='#666666'> - " + ta + "</font></html>";
			}
			setText(t);
			
			KeyStroke ks = c.getKeyStroke();
			if(ks!=null) {
				String s = ks.toString().replace("pressed ", "")
						.replace("ctrl", "Ctrl").replace("shift", "Shift");
				JLabel l  =new JLabel(s);
				l.setForeground(Color.DARK_GRAY);
				p.add(l, BorderLayout.EAST);
			}
			
		} else {
			setText(value.toString());
		}
		
		// color dependent on being selected
		if (isSelected) {
			p.setBackground(list.getSelectionBackground());
			p.setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			p.setBackground(list.getBackground());
			p.setForeground(list.getForeground());
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		
		return p;
		
	}
}