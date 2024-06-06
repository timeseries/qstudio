package com.timestored.jgrowl;

import java.util.logging.Level;

import javax.swing.ImageIcon;

public abstract class AbstractGrowler  implements Growler {


	/** {@inheritDoc} */	@Override
	public void show(String message, String title, ImageIcon imageIcon) {
		show(Level.INFO, message, title, imageIcon, false);
	}
	
	/** {@inheritDoc} */	@Override
	public void show(String message, String title) {
		show(Level.INFO, message, title);
	}
	
	/** {@inheritDoc} */	@Override
	public void show(String message) {
		show(Level.INFO, message);
	}
	
	/** {@inheritDoc} */	@Override
	public void showInfo(String message, String title) {
		show(Level.INFO, message, title);
	}

	/** {@inheritDoc} */	@Override
	public void showWarning(String message, String title) {
		show(Level.WARNING, message, title);
	}

	/** {@inheritDoc} */	@Override
	public void showSevere(String message, String title) {
		show(Level.SEVERE, message, title);
	}

	/** {@inheritDoc} */	@Override
	public void show(Level level, String message) {
		show(level, message, null);
	}

	/** {@inheritDoc} */	@Override
	public void show(Level level, String message, String title) {
		show(level, message, title, null, false);
	}
}
