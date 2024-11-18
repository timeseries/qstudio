package com.timestored.sqldash.chart;

import java.awt.Color;

/**
 *	Allows creating a {@link ColorScheme} that is the RGB inverse of an existing {@link ColorScheme}. 
 */
public class InverseColorScheme implements ColorScheme {

	private final Color bg;
	private final Color fg;
	private final Color text;
	private final Color gridLines;
	private final Color[] seriesColors;
	private final Color altBgColor;
	private final Color selectedBgColor;
	
	public InverseColorScheme(ColorScheme colorScheme) {
		
		this.bg = invert(colorScheme.getBG());
		this.fg = invert(colorScheme.getFG());
		this.text = invert(colorScheme.getText());
		this.gridLines = invert(colorScheme.getGridlines());
		this.altBgColor = invert(colorScheme.getAltBG());
		this.selectedBgColor = invert(colorScheme.getSelectedBG());
		
		Color[] originalColors = colorScheme.getColorArray();
		seriesColors = new Color[originalColors.length];
		for(int i=0; i<originalColors.length; i++) {
			seriesColors[i] = invert(originalColors[i]);
		}
	}

	private static Color invert(Color c) {
		return new Color(255-c.getRed(),
                		255-c.getGreen(),
                		255-c.getBlue());
	}

	@Override public Color getBG() { return bg; }
	@Override public Color getAltBG() { return altBgColor; }
	@Override public Color getSelectedBG() { return selectedBgColor; }
	@Override public Color getFG() { return fg; }
	@Override public Color getGridlines() { return gridLines; }
	@Override public Color[] getColorArray() { return seriesColors; }
	@Override public Color getText() { return text; }

}
