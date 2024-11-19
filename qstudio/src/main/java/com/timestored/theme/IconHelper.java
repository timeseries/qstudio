package com.timestored.theme;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

/** Helper class for dealing with Icons, resizing, reading from resource etc. */
public class IconHelper {

	/**  Convert an {@link ImageIcon} to a {@link BufferedImage}. */
	public static BufferedImage getBufferedImage(ImageIcon ii) {
		BufferedImage bi = new BufferedImage(
			ii.getIconWidth(),
			ii.getIconHeight(),
		    BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.createGraphics();
		// paint the Icon to the BufferedImage.
		ii.paintIcon(null, g, 0,0);
		g.dispose();
		return bi;
	}
	

	/**
	 * @param resourceUrl a class URL for a .png icon file
	 * @return An array of three Image Icons sizes = original, 16*16, 32*32 in 
	 * 		that order, or null if it wasn't possible.
	 */
	public static ImageIcon[] getDiffSizesOfIcon(URL resourceUrl) {
		ImageIcon ii = null;
		ImageIcon ii16 = null;
		ImageIcon ii32 = null;
		try {
			ii = new ImageIcon(resourceUrl);
			Image i = ii.getImage();
			ii16 = new ImageIcon(i.getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
			ii32 = new ImageIcon(i.getScaledInstance(32, 32, Image.SCALE_AREA_AVERAGING));
			
		} catch(Exception e) {
			// ignore
		}
		return new ImageIcon[] { ii, ii16, ii32 };
	}
}
