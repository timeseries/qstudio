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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class Demo implements Runnable {
	
	private Growler growler;
	private JTextArea textArea;
	private JTextField titleField;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Demo());
	}

	public void run() {
		JFrame frame = new JFrame("JGrowl Demo");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(640, 480));
		Container c = frame.getContentPane();
		c.setLayout(new BorderLayout(5,5));
		
		growler = GrowlerFactory.getGrowler(frame);
		textArea = new JTextArea("Hello World! This is my message");
		titleField = new JTextField("growler title");

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		buttonPanel.add(getButton(Level.INFO));
		buttonPanel.add(getButton(Level.WARNING));
		buttonPanel.add(getButton(Level.SEVERE));
		
		JPanel p = new JPanel(new GridLayout(0, 1));
		p.add(buttonPanel);
		p.add(titleField);
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(p);
		c.add(panel, BorderLayout.NORTH);
		c.add(textArea, BorderLayout.CENTER);
		
		
		frame.pack();
		frame.setVisible(true);
	}

	private JButton getButton(final Level l) {
		final JButton addButton = new JButton(l.getName());
		addButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				growler.show(l, textArea.getText(), titleField.getText());
			}
		});
		return addButton;
	}
}
