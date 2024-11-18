package com.timestored.swingxx;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * Presents a labelled color chooser panel that pops up a color chooser dialog.
 */
public class ColorChooserPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final JLabel  colorPreviewLabel = new JLabel("       ");
	private final JButton  colorButton;

	public ColorChooserPanel(final Component parent) {
		
		colorPreviewLabel.setOpaque(true);
		colorPreviewLabel.setBorder(BorderFactory.createLoweredBevelBorder());
		
		// let user choose a color
		colorButton = new JButton("Choose Color");
		colorPreviewLabel.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				Color c = JColorChooser.showDialog(parent, 
						"Choose Color", colorButton.getBackground());
				colorPreviewLabel.setBackground(c);
				colorButton.setBackground(c);
			}
		});
		colorButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				Color c = JColorChooser.showDialog(parent, 
						"Choose Color", colorButton.getBackground());
				colorPreviewLabel.setBackground(c);
				colorButton.setBackground(c);
			}
		});

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(colorButton);
		p.add(colorPreviewLabel);
		add(p);
	}

	public Color getColor() {
		return colorButton.getBackground();
	}

	public void setColor(Color color) {
		colorButton.setBackground(color);
		colorPreviewLabel.setBackground(color);
	}
}
