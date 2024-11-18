package com.timestored.sqldash.chart;

import java.awt.Component;
import java.sql.ResultSet;

/**
 * A visual component that can be updated with the latest {@link ResultSet} data.
 */
interface UpdateableView {
	
	/**
	 * Update the view with new data, in some cases this means regenerating for just new data
	 * in other cases the new RS data is just appended.
	 * @param rs The raw {@link ResultSet} as retrieved from database.
	 * @param chartResultSet Where possible a more chart oriented {@link ResultSet} that many
	 * 	{@link ViewStrategy}'s need. Generated higher level to save regerating each time.
	 * @throws ChartFormatException Thrown if data is incompatible with view.
	 */
	public void update(ResultSet rs, ChartResultSet chartResultSet) throws ChartFormatException;
	
	public Component getComponent();
}
