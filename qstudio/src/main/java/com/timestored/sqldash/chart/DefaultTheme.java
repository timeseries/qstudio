package com.timestored.sqldash.chart;


import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.title.LegendTitle;

import com.google.common.base.Preconditions;

/**
 * Light colored, white background, pastel shaded theme.
 */
class DefaultTheme implements ChartTheme {

    private static final StandardXYBarPainter barPainter = new StandardXYBarPainter();
    private static final StandardBarPainter sbarPainter = new StandardBarPainter();
    private static final DefaultTheme INSTANCE = new DefaultTheme(new LightColorScheme(), "Default", "Default");

    private final ColorScheme colorScheme;
	private final StandardChartTheme chartTheme;
	private final DrawingSupplier drawingSupplier;
	private final String description;
	private final String title;
    
    private DefaultTheme(final ColorScheme colorScheme, String title, String description) {

    	this.colorScheme = Preconditions.checkNotNull(colorScheme);
    	this.title = Preconditions.checkNotNull(title);
    	this.description = Preconditions.checkNotNull(description);
    	
    	/*
    	 * create a modified version of the jfree chart theme to suit our needs
    	 */
        chartTheme = (StandardChartTheme)org.jfree.chart.StandardChartTheme.createJFreeTheme();
        chartTheme.setXYBarPainter(barPainter);
        chartTheme.setBarPainter(sbarPainter);
        
        chartTheme.setShadowVisible(false);
        chartTheme.setShadowPaint(colorScheme.getGridlines());
        
        chartTheme.setPlotBackgroundPaint(colorScheme.getBG());
        chartTheme.setDomainGridlinePaint(colorScheme.getGridlines());
        chartTheme.setRangeGridlinePaint(colorScheme.getGridlines());
        chartTheme.setPlotOutlinePaint(colorScheme.getGridlines());
        chartTheme.setChartBackgroundPaint(colorScheme.getBG());
        chartTheme.setTitlePaint(colorScheme.getFG());

        
        chartTheme.setAxisLabelPaint(colorScheme.getText());
        chartTheme.setLabelLinkPaint(colorScheme.getFG());
        
        // legend related colors
        chartTheme.setLegendItemPaint(colorScheme.getText());
        chartTheme.setLegendBackgroundPaint(colorScheme.getBG());
        
        // The default font used by JFreeChart unable to render Chinese properly.
        // We need to provide font which is able to support Chinese rendering.
        if (Locale.getDefault().getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage())) {
            final Font oldExtraLargeFont = chartTheme.getExtraLargeFont();
            final Font oldLargeFont = chartTheme.getLargeFont();
            final Font oldRegularFont = chartTheme.getRegularFont();
            final Font oldSmallFont = chartTheme.getSmallFont();

            final Font extraLargeFont = new Font("Sans-serif", oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
            final Font largeFont = new Font("Sans-serif", oldLargeFont.getStyle(), oldLargeFont.getSize());
            final Font regularFont = new Font("Sans-serif", oldRegularFont.getStyle(), oldRegularFont.getSize());
            final Font smallFont = new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize());
            
            
            chartTheme.setExtraLargeFont(extraLargeFont);
            chartTheme.setLargeFont(largeFont);
            chartTheme.setRegularFont(regularFont);
            chartTheme.setSmallFont(smallFont);
        }

    	drawingSupplier = new DefaultDrawingSupplier() {

			private static final long serialVersionUID = 1L;
			int i = 0;
    		int j = 0;
    		Color[] colors = colorScheme.getColorArray();
			
			@Override public Paint getNextPaint() {
				return colors[(i++) % colors.length];
			}
			
			@Override public Paint getNextFillPaint() {
				return colors[(j++) % colors.length];
			}
		};
		chartTheme.setDrawingSupplier(drawingSupplier);
	}

    public static ChartTheme getInstance(ColorScheme colorScheme, String title, 
    		String description) {
    	return new DefaultTheme(colorScheme, title, description);
    }
    
    public static ChartTheme getInstance() {
    	return INSTANCE;
    }
    
    
    /**
     * Applying chart theme based on given JFreeChart.
     * @param chart the JFreeChart
     */
    @Override
    public JFreeChart apply(JFreeChart chart) {

        Plot p = chart.getPlot();
		LegendTitle legend = chart.getLegend();
		if(legend != null) {
			legend.setFrame(BlockBorder.NONE);
		}
        p.setDrawingSupplier(drawingSupplier);
        
        p.setForegroundAlpha(0.8F);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
//        chart.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
        
        chartTheme.apply(chart);

        if (chart.getPlot() instanceof CombinedDomainXYPlot) {
            @SuppressWarnings("unchecked")
            List<Plot> plots = ((CombinedDomainXYPlot)chart.getPlot()).getSubplots();
            for (Plot plot : plots) {
                final int domainAxisCount = ((XYPlot)plot).getDomainAxisCount();
                final int rangeAxisCount = ((XYPlot)plot).getRangeAxisCount();
                for (int i = 0; i < domainAxisCount; i++) {
                    setAxisColor(((XYPlot)plot).getDomainAxis(i), colorScheme);
                }
                for (int i = 0; i < rangeAxisCount; i++) {
                    setAxisColor(((XYPlot)plot).getRangeAxis(i), colorScheme);
                }
            }
            
            
        } else {
            final Plot plot = chart.getPlot();
            if (plot instanceof XYPlot) {          
                final org.jfree.chart.plot.XYPlot xyPlot = (org.jfree.chart.plot.XYPlot)plot;
                final int domainAxisCount = xyPlot.getDomainAxisCount();
                final int rangeAxisCount = xyPlot.getRangeAxisCount();
                for (int i = 0; i < domainAxisCount; i++) {
                    setAxisColor(xyPlot.getDomainAxis(i), colorScheme);
                }
                for (int i = 0; i < rangeAxisCount; i++) {
                    setAxisColor(xyPlot.getRangeAxis(i), colorScheme);
                }
            }
        }

        
        if(chart.getPlot() instanceof CategoryPlot) {
            final CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
            categoryPlot.setDomainGridlinesVisible(true);
            CategoryAxis categoryAxis = categoryPlot.getDomainAxis();
            categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
            final double margin = 0.02;
            categoryAxis.setCategoryMargin(margin);
            categoryAxis.setLowerMargin(0);
            categoryAxis.setUpperMargin(0);
            
            categoryAxis.setAxisLinePaint(colorScheme.getGridlines());
            categoryAxis.setTickMarkPaint(colorScheme.getFG());
            categoryAxis.setTickLabelPaint(colorScheme.getFG());

            setAxisColor(categoryPlot.getRangeAxis(), colorScheme);
            
        } else if(chart.getPlot() instanceof PiePlot) {
            final PiePlot piePlot = (PiePlot) chart.getPlot();
            piePlot.setLabelOutlinePaint(colorScheme.getFG());
            piePlot.setLabelLinkPaint(colorScheme.getFG());
        }
        return chart;
    }

	public static void setAxisColor(final ValueAxis valueAxis, ColorScheme colorScheme) {
		valueAxis.setAxisLinePaint(colorScheme.getFG());
		valueAxis.setTickMarkPaint(colorScheme.getText());
		valueAxis.setTickLabelPaint(colorScheme.getText());
	}

	@Override public boolean showChartLegend() { return true; }

	@Override public String getTitle() { return title; }

	@Override public String getDescription() { return description; }

	@Override public Color getForegroundColor() { return colorScheme.getFG(); }

	@Override public Color getBackgroundColor() { return colorScheme.getBG(); }

	@Override public Color getAltBackgroundColor() { return colorScheme.getAltBG(); }

	@Override public Color getSelectedBackgroundColor() { return colorScheme.getSelectedBG(); }
		
}


