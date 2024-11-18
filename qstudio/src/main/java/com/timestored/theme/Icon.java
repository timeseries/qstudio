package com.timestored.theme;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public interface Icon {

	/** @return Default sized imageIcon */
	public abstract ImageIcon get();

	public abstract BufferedImage getBufferedImage();

	/** @return Size 16*16 imageIcon */
	public abstract ImageIcon get16();

	/** @return Size 32*32 imageIcon */
	public abstract ImageIcon get32();

}