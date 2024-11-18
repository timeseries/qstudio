package com.timestored.sqldash.chart;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.timestored.sqldash.chart.ChartResultSet.NumericCol;
import com.timestored.sqldash.chart.ChartResultSet.StringyCol;
import com.timestored.sqldash.chart.ChartResultSet.TimeCol;

/**
 * Reads a {@link ResultSet} into components that it then constructs a {@link ChartResultSet} from.
 */
class ChartResultSetBuilder {

	/**
	 * Construct a ChartResultSet with one string col and one or more numerica col. 
	 */
	public static ChartResultSet getChartResultSet(List<NumericCol> numericColumns, StringyCol rowLabels) throws SQLException {
		List<StringyCol> stringyColumns = new ArrayList<StringyCol>();
		stringyColumns.add(rowLabels);
		List<String> rowLabelsL = rowLabels.getVals().stream().map(v -> (""+v)).collect(Collectors.toList());
		return new ChartResultSet(numericColumns, stringyColumns, rowLabelsL, null, "Row", "");
		
	}

	public static ChartResultSet transpose(ChartResultSet crs) {
	    // If no string column and only one row, transpose it to show something sensible.
	    // 
	    // size | price | age
	    // 10   | 1.1   | 20
	    //
	    // Name | Val
	    // size | 10
	    // price| 1.1
	    // age  | 20
		
		if(crs.getRowCount() != 1 || crs.getNumericColumns().size() < 2) {
			throw new IllegalArgumentException("Can only transpose limited results sets for now.");
		}

		List<StringyCol> stringyColumns = new ArrayList<StringyCol>();
		List<String> rowLabels = crs.getNumericColumns().stream().map(nc -> nc.getLabel()).collect(Collectors.toList());
		List<Object> rowLabelsO = rowLabels.stream().collect(Collectors.toList());
		stringyColumns.add(new StringyCol("Name", Types.VARCHAR, rowLabelsO));
		double[] va = new double[rowLabels.size()];
		for(int i=0; i<va.length; i++) {
			va[i] = crs.getNumericColumns().get(i).getDoubles()[0];
		}
		String rl = crs.getRowLabel(0).equals("1") ? "" : crs.getRowLabel(0);
		NumericCol nc =  new NumericCol(rl, Types.DOUBLE, va);
		List<NumericCol> lnc = new ArrayList<>(1);
		lnc.add(nc);
		return new ChartResultSet(lnc, stringyColumns, rowLabels, null, "Row", "");
		
	}
	
	public static ChartResultSet getChartResultSet(ResultSet rs) throws SQLException {

		rs.beforeFirst(); // in case someone else used it first.
		ResultSetMetaData md = rs.getMetaData();
		int colCount = md.getColumnCount();

		List<NumericCol> numericColumns = new ArrayList<NumericCol>();
		List<StringyCol> stringyColumns = new ArrayList<StringyCol>();
		TimeCol timeColumn = null;

		// Get the title of rows based on first consecutive string columns
		String rowTitle = "";
		List<Integer> stringIdxs = new ArrayList<Integer>();
		for(int c=1; c<=colCount; c++) {
			int ctype = md.getColumnType(c);
			if(!SqlHelper.isNumeric(ctype, md.getColumnTypeName(c))) {
				rowTitle += (c==1 ?  "" : " - ") + md.getColumnName(c);
				stringIdxs.add(c);
			} else {
				break;
			}
		}
		if(rowTitle.length()==0) {
			rowTitle = "Row";
		}
		List<String> rowLabels = getRowLabels(rs, stringIdxs);
		int rowCount = rowLabels.size();
		
		for(int c=1; c<=colCount; c++) {
			int ctype = md.getColumnType(c);
			if(SqlHelper.isNumeric(ctype, md.getColumnTypeName(c))) {
				numericColumns.add(new NumericCol(md.getColumnName(c), ctype, getDoubles(c, rs, rowCount)));
			} else {
				if(SqlHelper.isTemporal(ctype, md.getColumnTypeName(c)) && timeColumn==null) {
					timeColumn = new TimeCol(md.getColumnName(c), ctype, getObjects(c, rs));
				}
				stringyColumns.add(new StringyCol(md.getColumnName(c), ctype, getObjects(c, rs)));
			}
		}

		return new ChartResultSet(numericColumns, stringyColumns, rowLabels, timeColumn, rowTitle, "");
	}
	
	/** @return Using a concatenation of each rows initial string columns create row labels */
	private static List<String> getRowLabels(ResultSet rs, List<Integer> stringIdxs) throws SQLException {

		List<String> rowNames = new ArrayList<String>();

		rs.beforeFirst();
		if(stringIdxs.size() > 0) {
			while(rs.next()) {
				String s = ""+rs.getObject(stringIdxs.get(0));
				for(int idx=1; idx<stringIdxs.size(); idx++) {
					s += (" - " + rs.getObject(stringIdxs.get(idx)));
				}
				rowNames.add(s);
			}
		} else {
			int row=1;
			while(rs.next()) {
				rowNames.add(""+row++);
			}
		}
		return rowNames;
	}


	/** @return Entire contents of a column as a list of objects. */
	private static double[] getDoubles(int column, ResultSet rs, int rowCount) throws SQLException {
		
		double nums[] = new double[rowCount];
		rs.beforeFirst();
		int i=0;
		while(rs.next()) {
			Object o = rs.getObject(column);
			if(o==null) {
				nums[i] = Double.NaN;
			} else if(o instanceof Number) {
				Number n = (Number) rs.getObject(column);
				nums[i] = n.doubleValue();
			} else {
				// Essential fallback for DolphinDB as it returns BasicInt/BasicDouble which don't implement Number
				nums[i] = rs.getDouble(column);
			}
			i++;
		}
		assert(i == nums.length);
		return nums;
	}


	/** @return Entire contents of a column as a list of objects. */
	private static List<Object> getObjects(int column, ResultSet rs) throws SQLException {
		ArrayList<Object> vals = new ArrayList<Object>();
		rs.beforeFirst();
		while(rs.next()) {
			vals.add(rs.getObject(column));
		}
		return vals;
	}
}
