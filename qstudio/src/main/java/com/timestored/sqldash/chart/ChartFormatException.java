package com.timestored.sqldash.chart;

/**
 * A data update of unsupported format was passed to a view for drawing.
 */
public class ChartFormatException extends java.io.IOException {
	
	private final String details;

	ChartFormatException(String details) {
		this.details = details;
	}
	
	String getDetails() {
		return details;
	}
	
	@Override public String getMessage() {
		return details;
	}
}
