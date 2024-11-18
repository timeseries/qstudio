package com.timestored.sqldash.chart;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.sql.ResultSet;

import javax.swing.JPanel;

import com.google.common.base.Preconditions;

/**
 * Decorates a viewGetter to provide an {@link UpdateableView} that works by totally regenerating
 * the whole GUI on each update.
 */
class HardRefreshUpdateableView implements UpdateableView {

	private final ViewGetter viewGetter;
	private final JPanel panel = new JPanel(new BorderLayout());
	
	public HardRefreshUpdateableView(ViewGetter viewGetter) {
		this.viewGetter = Preconditions.checkNotNull(viewGetter);
	}
	
	@Override public void update(final ResultSet resultSet, ChartResultSet chartResultSet) 
			throws ChartFormatException {
		
		if(chartResultSet == null) {
			throw new ChartFormatException("Could not construct ResultSet.");
		}
		
		Component c = viewGetter.getView(resultSet, chartResultSet);
		
		if(c!=null) {
			final Component com = c;
			EventQueue.invokeLater(new Runnable() {
				
				@Override public void run() {
					panel.removeAll();
					panel.add(com, BorderLayout.CENTER);
					panel.revalidate();
				}
			});
		}
	}

	@Override public Component getComponent() {
		return panel;
	}
	
	public static interface ViewGetter {
		public Component getView(ResultSet resultSet, ChartResultSet chartResultSet) 
				throws ChartFormatException;
	}
}
