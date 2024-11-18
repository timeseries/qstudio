package com.timestored.sqldash.chart;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Paint;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.DefaultDrawingSupplier;

class ColorToolTipListener implements ChartMouseListener {

	private final Paint[] PAINTS = DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE;
	private static final ColorToolTipListener INSTANCE = new ColorToolTipListener();
	private final int PADDING = 5;
	
	private XYItemEntity lastSeenEntity = null;
	private JPopupMenu lastPopupMenu = null;
	
	private ColorToolTipListener() { }
	
	public static final ColorToolTipListener getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void chartMouseClicked(ChartMouseEvent arg0) {
		//purposefully empty
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent cme) {
		if (cme != null && (cme.getEntity() instanceof XYItemEntity)) {

			XYItemEntity xyEntity = (XYItemEntity) cme.getEntity();

			if (!xyEntity.equals(lastSeenEntity)) {
				if (lastPopupMenu != null) {
					lastPopupMenu.setVisible(false);
					lastPopupMenu = null;
				}

				JLabel label = new JLabel(xyEntity.getToolTipText());
				lastPopupMenu = new JPopupMenu();
				lastPopupMenu.add(label);
				label.setBorder(BorderFactory.createEmptyBorder(PADDING, 
						PADDING, PADDING, PADDING));
				int seriesIdx = xyEntity.getSeriesIndex();
				Color bgColor = (Color) PAINTS[seriesIdx % PAINTS.length];
				lastPopupMenu.setBorder(BorderFactory.createLineBorder(bgColor));

				cme.getTrigger();
				Point loc = MouseInfo.getPointerInfo().getLocation();
				lastPopupMenu.setLocation(10 + (int) loc.getX(), 20 + (int) loc.getY());
				lastPopupMenu.setVisible(true);
				lastSeenEntity = xyEntity;
			}
		} else {

			if (lastPopupMenu != null) {
				lastPopupMenu.setVisible(false);
				lastPopupMenu = null;

				lastSeenEntity = null;
			}
		}
	}
	
}
