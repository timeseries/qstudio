package com.timestored.sqldash.chart;

import java.awt.Component;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;

import net.sf.jtreemap.swing.JTreeMap;
import net.sf.jtreemap.swing.TreeMapNode;
import net.sf.jtreemap.swing.TreeMapNodeBuilder;
import net.sf.jtreemap.swing.ValuePercent;
import net.sf.jtreemap.swing.provider.RedGreenColorProvider;
import net.sf.jtreemap.swing.provider.ZoomPopupMenu;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.timestored.connections.JdbcTypes;
import com.timestored.sqldash.chart.ChartResultSet.NumericCol;
import com.timestored.sqldash.chart.ChartResultSet.StringyCol;
import com.timestored.sqldash.theme.DBIcons;
import com.timestored.theme.Icon;

/**
 * Strategy for displaying {@link ResultSet}'s as a heatmap.
 * See {@link #getFormatExplainationHtml()} for details.
 */
public enum HeatMapViewStrategy implements ViewStrategy {

	INSTANCE;

	private static final String[] FORMATA = 
		{ "Starting from the left each string column is taken as one nesting level",
				"The first numerical column will be taken as size, the second as colour." };

	
	@Override public UpdateableView getView(ChartTheme theme) {

		return new HardRefreshUpdateableView(new HardRefreshUpdateableView.ViewGetter() {


			@Override public Component getView(ResultSet resultSet, ChartResultSet chartResultSet) 
					throws ChartFormatException {

				JTreeMap treeMap = new JTreeMap(createTreeMapDataset(chartResultSet));
		        treeMap.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		        treeMap.setColorProvider(new RedGreenColorProvider(treeMap));
		        new ZoomPopupMenu(treeMap);
		        
				return treeMap;
			}
		});
	}

	@Override public Icon getIcon() { return DBIcons.CHART_HEATMAP; }
	
	private TreeMapNode createTreeMapDataset(ChartResultSet colResultSet) throws ChartFormatException {

		List<NumericCol> numColumns = colResultSet.getNumericColumns();

        // check we have enough number columns
        if(numColumns.size() < 1) {
        	throw new ChartFormatException("There must be atleast one number column.");
        }
        // use string cols if they are available otherwise row labels.
        List<StringyCol> stringCols = colResultSet.getStringyColumns();
        if(stringCols.size()==0) {
        	stringCols = ImmutableList.of(colResultSet.getRowLabels());
        }
        
        double[] weights = numColumns.get(0).getDoubles();
        double[] values = weights;
        if(numColumns.size()>1) {
        	values = numColumns.get(1).getDoubles();
        }
        // need to 
        return buildStringColTree(stringCols, weights, values);
	}
	

	/**
	 * Build a tree where each stringCol other than the last is a level of branching.
	 * e.g. continent,country,state could be 3 columns giving continent and country as branches
	 * 	with the state as leafs with size and color.
	 */
	private static TreeMapNode buildStringColTree(List<StringyCol> stringCols, 
			double[] weights, double[] values) {

		Preconditions.checkArgument(stringCols.size() > 0);
		final TreeMapNodeBuilder builder = new TreeMapNodeBuilder();
        final TreeMapNode rootNode = builder.buildBranch("Root", null);
        
        int levels = stringCols.size() - 1;
        // maps the name of branches at each level to their node
        List<Map<String, TreeMapNode>> levelMaps;
        if(levels >= 0) {
        	levelMaps = Lists.newArrayListWithCapacity(levels);
        	for(int l=0; l<levels; l++) {
        		levelMaps.add(new HashMap<String, TreeMapNode>());
        	}
        	
            for(int row=0; row < weights.length; row++) {
            	TreeMapNode parent = rootNode;
            	for(int col=0; col < stringCols.size(); col++) {

            		List<Object> curLabels = stringCols.get(col).getVals();
            		boolean atBranch = !(col == stringCols.size()-1);
            		if(atBranch) {
            			// creat branch if it doesn't already exist.
            			String branchName = stringCols.get(col).getVals().get(row).toString();
            			TreeMapNode branchNode = levelMaps.get(col).get(branchName);
                		if(branchNode == null) {
                			branchNode = builder.buildBranch(branchName, parent);
                			levelMaps.get(col).put(branchName, branchNode);
                		}
                		parent = branchNode;
            		} else {
            			// at leaf
        		        builder.buildLeaf(curLabels.get(row).toString(), weights[row], 
        		        		new ValuePercent(values[row]), parent);
            		}
            	}
            }
        }
        
        return builder.getRoot();
	}


	@Override
	public String getDescription() {
		return "Heat Map";
	}

	
	@Override public String getFormatExplainationHtml() {
		return "A HeatMap works best with 1+ string columns." +
				"<ol><li>" + Joiner.on("</li><li>").join(FORMATA) + "</li></ol>";
	}
	
	@Override public String getFormatExplaination() {
		return "A HeatMap works best with 1+ string columns.\r\n" + 
				Joiner.on("\r\n").join(FORMATA);
	}

	@Override
	public List<ExampleView> getExamples() {
		ExampleView ev = new ExampleView("Country GDP's", 
				"The continent column is a top-level branch, the country column becomes leafs." +
				"The first two columns are GDP and GDP per Capita which become the size and " +
				"color of the leafs respectively.",
				ExampleTestCases.COUNTRY_STATS);
		return ImmutableList.of(ev);
	}


	@Override public String getQueryEg(JdbcTypes jdbcType) {
		if(jdbcType.equals(JdbcTypes.KDB)) {
			return ExampleTestCases.COUNTRY_STATS.getKdbQuery();
		}
		return null; 
	}

	@Override public String toString() {
		return HeatMapViewStrategy.class.getSimpleName() + "[" + getDescription() + "]";
	}
	
	@Override public boolean isQuickToRender(ResultSet rs, int rowCount, int numColumnCount) {
		return rowCount < 4_000; // 2 seconds on Ryans PC
	}

	@Override public String getPulseName() { return "heatmap"; }
}
