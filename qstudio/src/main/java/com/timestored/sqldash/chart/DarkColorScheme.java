package com.timestored.sqldash.chart;

import java.awt.Color;

/**
 * A light color scheme based on the jfree chart defaults. 
 */
public class DarkColorScheme  implements ColorScheme {

	private static final Color TXT_COLOR = Color.decode("#DDDDDD");
	private static final Color FG_COLOR = Color.decode("#EEEEEE");
	private static final Color BG_COLOR = Color.decode("#111217");
	private static final Color ALT_BG_COLOR = Color.decode("#333333");
	private static final Color SELECTED_BG_COLOR = Color.decode("#630802");
	
	@Override public Color getBG() { return BG_COLOR; }
	@Override public Color getAltBG() { return ALT_BG_COLOR; }
	@Override public Color getSelectedBG() { return SELECTED_BG_COLOR; }
	@Override public Color getFG() { return FG_COLOR; }
	@Override public Color getText() { return TXT_COLOR; }
	@Override public Color getGridlines() { return Color.LIGHT_GRAY; }
	@Override public Color[] getColorArray() { return LightColorScheme.SERIES_COLORS; }
	
}