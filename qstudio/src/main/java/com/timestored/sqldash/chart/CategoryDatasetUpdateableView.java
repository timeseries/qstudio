package com.timestored.sqldash.chart;

import java.awt.Component;
import java.awt.Font;
import java.sql.ResultSet;
import java.text.DecimalFormat;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

import com.google.common.base.Preconditions;
import com.timestored.sqldash.chart.ChartResultSet.NumericCol;

/**
 * A CategoryDataset based updateable view.
 */
class CategoryDatasetUpdateableView implements UpdateableView {

	private static final Font TINY_FONT = new Font("Times New Roman", Font.PLAIN, 0);
	private final ChartPanel chartPanel;
	private DefaultCategoryDataset dataset;

	public CategoryDatasetUpdateableView(ChartTheme theme, JFreeChart chart) {

		Preconditions.checkNotNull(chart);
		Preconditions.checkNotNull(theme);
		
		this.dataset = new DefaultCategoryDataset();
		chart.getCategoryPlot().setDataset(dataset);
		
		chartPanel = new ChartPanel(theme.apply(chart), false, true, true, false, true);
	}

	@Override public void update(ResultSet rs, ChartResultSet chartRS) throws ChartFormatException {
		
		if(chartRS == null) {
			throw new ChartFormatException("Could not construct ResultSet.");
		}
		if(chartRS.getNumericColumns().size()<1) {
			throw new ChartFormatException("Atleast one numeric column is required.");
		}
		
		// name axis using column names etc.
		JFreeChart chart = chartPanel.getChart();
		CategoryPlot cplot = chart.getCategoryPlot();
		CategoryItemRenderer renderer = cplot.getRenderer();
		
		renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
				Tooltip.LABEL_XY_FORMAT, new DecimalFormat("#,###.##")));
		
		// set the range label
		cplot.getDomainAxis().setLabel(chartRS.getRowTitle());
		ValueAxis rangeAxis = cplot.getRangeAxis();
		if(chartRS.getNumericColumns().size()==1) {
			rangeAxis.setLabel(chartRS.getNumericColumns().get(0).getLabel());
		} else {
			rangeAxis.setLabel("");
		}

		dataset.clear();
		add(chartRS, dataset);

		
		// domain axis labels - too many?
		// if more than 30 categories, hide them and show only 10
		if(dataset.getColumnCount()>30) {
			CategoryAxis dAxis = cplot.getDomainAxis();
			int i = 0;
			int m = dataset.getColumnCount()/7;
			for(Object key : dataset.getColumnKeys()) {
				if(!(i%m==0)) {
					// use tiny font to handle different chart themes and colors better
					dAxis.setTickLabelFont((Comparable<?>) key, TINY_FONT);
				}
				i++;
			}
		}
		
		chart.getLegend().setVisible(dataset.getRowCount()>1 && dataset.getRowCount()<60);	
	}

	@Override public Component getComponent() {
		return chartPanel;
	}

	/** Add the new data in {@link ResultSet} to the {@link Dataset} if possible. */
	public static DefaultCategoryDataset add(ChartResultSet colResultSet, DefaultCategoryDataset dataset) {
		for (NumericCol numCol : colResultSet.getNumericColumns()) {
			double[] vals = numCol.getDoubles();
			for (int i = 0; i < vals.length; i++) {
				dataset.addValue(vals[i], numCol.getLabel(), colResultSet.getRowLabel(i));
			}
		}
		return dataset;
	}
}
