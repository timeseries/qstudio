package com.timestored.sqldash.chart;

import java.awt.Color;

/**
 * A group of collected colors used for rendering a chart. 
 */
interface ColorScheme {

	/** @return the main background color **/
	public abstract Color getBG();
	
	/** 
	 * An alternate backgorund colo, e.g. table display will 
	 * show rows in alternating background colors 
	 **/
	public abstract Color getAltBG();

	/** 
	 * e.g. table display will selected cells in this color 
	 **/
	public abstract Color getSelectedBG();
	
	public abstract Color getFG();
	public abstract Color getGridlines();
	public abstract Color[] getColorArray();
	Color getText();
}