package com.timestored.qstudio;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import lombok.Data;

/**
 * Contains the full configuration used for Pivoting. Needs passed through to QueryManager
 * as only Query manager sends query. We also need the full state of the form saved otherwise
 * KDBresultPanel couldn't fully refresh else it would lose context. Alternative would have been keeping state inside kdbresultPanel.
 */
@Data public class PivotFormConfig {

	public final static String[] AGG_OPS = new String[] { "count","max","min","sum","avg", "dev", "sdev", "var", "svar" };
	private final static Set<Integer> GROUPING_TYPES;
	private final static Set<Integer> NUM_TYPES;
	
	private final String sqlQuery;
	private final List<String> allCols;
	private final List<String> aggCols;
	private final List<String> byColsShown;

	private final String aggOp;
	private final String aggCol;
	private final List<String> byColsSelected;
	private final List<String> pivotColsSelected;
	
	static {
		Set<Integer> numTypes = new HashSet<>();
		numTypes.add(java.sql.Types.BIGINT);
		numTypes.add(java.sql.Types.DECIMAL);
		numTypes.add(java.sql.Types.DOUBLE);
		numTypes.add(java.sql.Types.FLOAT);
		numTypes.add(java.sql.Types.INTEGER);
		numTypes.add(java.sql.Types.NUMERIC);
		numTypes.add(java.sql.Types.REAL);
		numTypes.add(java.sql.Types.SMALLINT);
		numTypes.add(java.sql.Types.TINYINT);
		NUM_TYPES = ImmutableSet.copyOf(numTypes);

		Set<Integer> groupingTypes = new HashSet<>();
		groupingTypes.add(java.sql.Types.BOOLEAN);
		groupingTypes.add(java.sql.Types.DATE);
		groupingTypes.add(java.sql.Types.INTEGER);
		groupingTypes.add(java.sql.Types.NCHAR);
		groupingTypes.add(java.sql.Types.NVARCHAR);
		groupingTypes.add(java.sql.Types.SMALLINT);
		groupingTypes.add(java.sql.Types.TINYINT);
		groupingTypes.add(java.sql.Types.ROWID);
		groupingTypes.add(java.sql.Types.VARCHAR);
		GROUPING_TYPES = ImmutableSet.copyOf(groupingTypes);
	}
	
	public boolean containsPivot() {
		return pivotColsSelected.size()>0;
	}

	public PivotFormConfig(String originalQry, ResultSetMetaData metaData) throws SQLException {
		this.sqlQuery = originalQry;
		List<ColDetails> cds = getColDetails(metaData);
		Function<Stream<ColDetails>,List<String>> conv = (Stream<ColDetails> s) -> {
			return ImmutableList.copyOf(s.map(ColDetails::getName).collect(Collectors.toList()));
		};
		this.allCols = conv.apply(cds.stream());
		this.aggCols = conv.apply(cds.stream().filter(cd -> NUM_TYPES.contains(cd.getJdbcType())));
		this.byColsShown = conv.apply(cds.stream().filter(cd -> GROUPING_TYPES.contains(cd.getJdbcType())));
		
		this.aggCol = aggCols.size() > 0 ? aggCols.get(0) : "";
		this.aggOp = "sum";

		this.byColsSelected = Collections.emptyList();
		this.pivotColsSelected = Collections.emptyList();
	}
	
	private PivotFormConfig(PivotFormConfig pivotConfig, List<String> byColsSelected, List<String> pivotColsSelected, String aggOp, String aggCol) throws SQLException {
		this.sqlQuery = pivotConfig.sqlQuery;
		this.allCols = pivotConfig.allCols;
		this.aggCols = pivotConfig.aggCols;
		this.byColsShown = pivotConfig.byColsShown;

		this.aggOp = aggOp;
		this.aggCol = aggCol;
		this.byColsSelected = byColsSelected;
		this.pivotColsSelected = pivotColsSelected;
	}
	
	public PivotFormConfig changeSelection(List<String> byColsSelected, List<String> pivotColsSelected, String aggOp, String aggCol) throws SQLException {
		return new PivotFormConfig(this, byColsSelected, pivotColsSelected, aggOp, aggCol);
	}

	@Data private static class ColDetails {
		private final String name;
		private final int jdbcType;
	}
	
	private static List<ColDetails> getColDetails(ResultSetMetaData rsmd) throws SQLException {
		int cols = rsmd.getColumnCount();
		List<ColDetails> cns = new ArrayList<>(cols);
		for(int c=1; c<=cols; c++) {
			cns.add(new ColDetails(rsmd.getColumnName(c), rsmd.getColumnType(c)));
		}
		return cns;
	}
	
	public String getAggSel() {
		return aggCol + ":" + aggOp + " " + aggCol;
	}
	
}
