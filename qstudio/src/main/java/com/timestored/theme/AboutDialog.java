package com.timestored.theme;

import java.awt.Container;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.timestored.TimeStored;
import com.timestored.misc.InfoLink;

/**
 * Displays version and homepage information.
 */
public class AboutDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;

	public AboutDialog(JFrame parentFrame, String title,
			Icon icon, String htmlTitle, String version) {
		
		super(parentFrame, title);
        setIconImage(icon.get().getImage());
		
        
		JPanel logoPanel = new JPanel();
		logoPanel.add(new JLabel(icon.get()));
		logoPanel.add(Theme.getHtmlText(htmlTitle));
		logoPanel.setAlignmentX(CENTER_ALIGNMENT);

		JLabel label = new JLabel("<html><h4>Version: " + version + "</h4></html>");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		String txt = "Homepage: TimeStored.com";		
		
        JPanel timestoredLinkPanel = Theme.getVerticalBoxPanel();
		timestoredLinkPanel.add(label);
		timestoredLinkPanel.add(InfoLink.getLabel(txt, txt, TimeStored.URL, true));
		timestoredLinkPanel.setAlignmentX(CENTER_ALIGNMENT);
		
		Container cp = this.getContentPane();
		
		cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
		cp.add(logoPanel);
		cp.add(timestoredLinkPanel);

		pack();
		setLocationRelativeTo(parentFrame);
	}
}
