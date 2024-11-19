package com.timestored.sqldash.chart;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This can be used to configure either a time series domain, category domain,
 * or numerical domain chart. 
 * The domain can be set to either:
 * 		- 1 time series
 * 		- 1 numerical
 * 		- 1 numerical
 * 		- 1+ columns - in which case the domain value is the rows concatenated.
 * 	
 */
public class ChartViewConfiguration  {

	public enum Shape { LINE,BAR,AREA,CANDLESTICK,HIGHLOW };
	public enum Axis { LEFT,RIGHT,HIDE };	

	private static class ColumnConfiguration {
		private final Shape shape;
		private final Axis axis;
		
		public ColumnConfiguration(Shape shape, Axis axis) {
			super();
			this.shape = shape;
			this.axis = axis;
		}
	}
	
	private final Set<String> domainColumnNames = new HashSet<String>();
	private final Map<String,ColumnConfiguration> colConfigs;
	private static final ColumnConfiguration DEFAULT_COLUMN_CONFIG = 
			new ColumnConfiguration(Shape.LINE, Axis.LEFT); 
	/** Generate the defaults  */
	public ChartViewConfiguration() {
		colConfigs = new HashMap<String,ColumnConfiguration>();
	}
	
	/**
	 * 
	 * @param tab
	 * @return true if changed
	 */
	private boolean refresh(ResultSet tab) {
		domainColumnNames.clear();
//		generateDefaults(tab);
		return true;
	}
	
	public List<String> getAllColumnNames() {
		Set<String> cols = new HashSet<String>(colConfigs.keySet());
		cols.addAll(domainColumnNames);
		return new ArrayList<String>(cols);
	}
	
	public boolean isDomainColumn(final String colName) {
		return domainColumnNames.contains(colName);
	}

	public Shape getShape(final String colName) {
		ColumnConfiguration colConfig = colConfigs.get(colName);
		if(colConfig==null) {
			throw new IllegalArgumentException("column name not known");
		}
		return colConfig.shape;
	}

	public Axis getAxis(final String colName) {
		ColumnConfiguration colConfig = colConfigs.get(colName);
		if(colConfig==null) {
			throw new IllegalArgumentException("column name not known:" + colName);
		}
		return colConfig.axis;
	}
	
//	public boolean addAsDomainColumn(String columnName) {
//		return domainColumnNames.add(columnName);
//	}
//	
//	public void removeAsDomainColumn(String columnName) {
//		if(domainColumnNames.remove(columnName)) {
//			if(!colConfigs.containsKey(columnName)) {
//				colConfigs.put(columnName, DEFAULT_COLUMN_CONFIG);
//			}
//			if(domainColumnNames.isEmpty() && colConfigs.)
//		}
//		throw new IllegalArgumentException(columnName + " isn't a domain column");
//	}
//	
//	public void setColumn(String colName, Shape shape, AXIS axis) {
//		colConfigs.put(colName, new ColumnConfiguration(shape, axis));
//	}
}
	