package com.timestored.sqldash.theme;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import com.timestored.theme.Icon;
import com.timestored.theme.IconHelper;


public enum DBIcons implements Icon {

	TEXTFIELD("textfield.png"), 
	CHECKBOX("checkbox.png"), 
	COMBOBOX("combobox.png"), 
	LIST("list.png"), 
	RADIOBUTTON("radiobutton.png"), 
	SLIDER("slider.png"), 
	SPINNER("spinner.png"), 
	TEXTAREA("textarea.png"),
	GO_BOTTOM("go-bottom.png"),
	GO_DOWN("go-down.png"),
	GO_TOP("go-top.png"),
	GO_UP("go-up.png"),
	CHART_HEATMAP("heatmap.png"),
	CHART_CANDLESTICK("candlestick2.png"),
	CHART_LINE("chart-line.png"),
	CHART_AREA("chart-area.png"),
	CHART_SCATTER_PLOT("chart-scatter.png"),
	CHART_BUBBLE("chart-bubble.png"),
	CHART_COLUMN("chart-column.png"),
	CHART_PIE("chart-pie.png"),
	CHART_CURVE("chart-curve.png"),
	CHART_BAR("chart-bar.png"), 
	CHART_HISTOGRAM("chart-histogram-32.png"),
	ATTRIB_N("setattrn.png"),
	ATTRIB_U("setattru.png"),
	ATTRIB_P("setattrp.png"),
	ATTRIB_S("setattrs.png"),
	ATTRIB_G("setattrg.png");
	
	private final ImageIcon imageIcon;
	private final ImageIcon imageIcon16;
	public final ImageIcon imageIcon32;

	/** @return Default sized imageIcon */
	public ImageIcon get() { return imageIcon; }
	
	/** @return Size 16*16 imageIcon */
	public ImageIcon get16() { return imageIcon16; }
	
	/** @return Size 32*32 imageIcon */
	public ImageIcon get32() { return imageIcon32; }
	
	
	public BufferedImage getBufferedImage() {
		return IconHelper.getBufferedImage(imageIcon);
	}
	
	DBIcons(String loc) {
		ImageIcon[] icons = IconHelper.getDiffSizesOfIcon(DBIcons.class.getResource(loc));
		imageIcon = icons[0];
		imageIcon16 = icons[1];
		imageIcon32 = icons[2];
	}
	
}