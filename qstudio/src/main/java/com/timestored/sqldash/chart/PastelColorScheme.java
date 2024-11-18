package com.timestored.sqldash.chart;

import java.awt.Color;

public class PastelColorScheme implements ColorScheme {

	private static final Color FG_COLOR = Color.WHITE;
	private static final Color BG_COLOR = Color.BLACK;
	private static final Color ALT_BG_COLOR = Color.decode("#333333");
	private static final Color SELECTED_BG_COLOR = Color.decode("#630802");
	
	Color[] seriesColors = new Color[] {
			// generated with http://tools.medialab.sciences-po.fr/iwanthue/
			new Color(240,168,161)
			,new Color(129,217,177)
			,new Color(216,205,113)
			,new Color(159,196,222)
			,new Color(223,222,203)
			,new Color(234,175,208)
			,new Color(228,173,111)
			,new Color(197,234,146)
			,new Color(210,194,242)
			,new Color(227,196,151)
			,new Color(195,205,144)
			,new Color(148,225,234)
			,new Color(168,191,163)
			,new Color(201,180,176)
			,new Color(243,206,217)
			,new Color(220,239,195)
			,new Color(149,220,205)
			,new Color(205,219,223)
			,new Color(197,179,200)
			,new Color(165,229,170)
			,new Color(213,209,237)
			,new Color(218,185,166)
	};

	PastelColorScheme() { };
	@Override public Color getBG() { return BG_COLOR; }
	@Override public Color getAltBG() { return ALT_BG_COLOR; }
	@Override public Color getSelectedBG() { return SELECTED_BG_COLOR; }
	@Override public Color getFG() { return FG_COLOR; }
	@Override public Color getText() { return Color.decode("#DDDDDD"); }
	@Override public Color getGridlines() { return Color.LIGHT_GRAY; }

	@Override public Color[] getColorArray() {
		return seriesColors;
	}
}
