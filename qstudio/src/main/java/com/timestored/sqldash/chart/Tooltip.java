package com.timestored.sqldash.chart;

import java.text.DecimalFormat;

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.StandardXYZToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.labels.XYZToolTipGenerator;

/** Helper class for tooltip generators **/
class Tooltip {

	public static String LABEL_XY_FORMAT = "<html><b>{0}:</b><br>{1}<br>{2}</html>";
	public static String LABEL_XYZ_FORMAT = "<html><b>{0}:</b><br>{1}<br>{2}</html>";

	/** Return XY tooltip generator where x/y values are both numbers **/
	public static XYToolTipGenerator getXYNumbersGenerator() {
		return new StandardXYToolTipGenerator(LABEL_XY_FORMAT, 
				new DecimalFormat("#,###.##"), new DecimalFormat("#,###.##"));
	}
	/** Return XY tooltip generator where x/y values are both numbers **/
	public static XYZToolTipGenerator getXYZNumbersGenerator() {
		return new StandardXYZToolTipGenerator(LABEL_XYZ_FORMAT, 
				new DecimalFormat("#,###.##"), new DecimalFormat("#,###.##"), new DecimalFormat("#,###.##"));
	}
}