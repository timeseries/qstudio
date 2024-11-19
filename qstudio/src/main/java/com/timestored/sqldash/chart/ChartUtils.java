package com.timestored.sqldash.chart;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.timestored.misc.SaveableFrame;

/**
 * useful chart utility functions for saving to file etc.
 */
public class ChartUtils {

	private static final int HEIGHT = 600;
	private static final int WIDTH = 800;

	static void save(ViewStrategy viewStrategy, ResultSet resultSet, 
			File file) throws IOException {
		save(viewStrategy, resultSet, file, WIDTH, HEIGHT, false);
	}

	public static void save(ViewStrategy viewStrategy, ResultSet resultSet, 
			File file, int width, int height, boolean watermark) throws IOException {
		save(viewStrategy, resultSet, file, width, height, watermark, null);
	}
	
	/**
	 * 
	 * @throws ChartFormatException If the resultset is the wrong format for this {@link ViewStrategy}.
	 */
	public static void save(ViewStrategy viewStrategy, ResultSet resultSet, 
			File file, int width, int height, boolean watermark, ChartTheme chartTheme) throws IOException {
		
		final JdbcChartPanel jdbcChartPanel = ViewStrategyFactory.getJdbcChartpanel(viewStrategy);
		if(chartTheme != null) {
			jdbcChartPanel.setTheme(chartTheme);
		}
		jdbcChartPanel.update(resultSet);
		try {
			EventQueue.invokeAndWait(new Runnable() {
				
				@Override public void run() {
					jdbcChartPanel.validate();
					jdbcChartPanel.repaint();
				}
			});
		} catch (InterruptedException e) { } 
		catch (InvocationTargetException e) { }
		ChartFormatException cfe = jdbcChartPanel.getLastChartFormatException();
		if(cfe!=null) {
			throw cfe;
		}
		SaveableFrame.saveComponentImage(jdbcChartPanel, width, height, file, watermark);
	}
	

	/**
	 * Query KDB test server, generate view, save as .png
	 * ONLY TO BE USED FOR TESTS AS DEPENDS ON KDB AND DOESNT WATERMARK
	 */
	public static void queryKdbAndSave(ViewStrategy viewStrategy, String query,
			File file, Connection conn) throws IOException {
		try	{
		    Statement st = conn.createStatement();
		    ResultSet rs = st.executeQuery("q)" + query);
			save(viewStrategy, rs, file, WIDTH, HEIGHT, false);
		} catch (SQLException e) {
			throw new IOException(e);
		} 
	}
	
}
