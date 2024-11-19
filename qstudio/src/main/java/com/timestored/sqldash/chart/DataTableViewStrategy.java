package com.timestored.sqldash.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import com.google.common.collect.ImmutableList;
import com.timestored.connections.JdbcTypes;
import com.timestored.swingxx.SaveTableMouseAdapter;
import com.timestored.theme.Icon;
import com.timestored.theme.Theme;
import com.timestored.theme.Theme.CIcon;

/**
 * Strategy for displaying {@link ResultSet}'s as plain table.
 */
public class DataTableViewStrategy implements ViewStrategy {

	private static ViewStrategy INSTANCE = new DataTableViewStrategy(false);
	private static ViewStrategy DEBUG_INSTANCE = new DataTableViewStrategy(true);
	private final boolean debugView;

	private static final List<ExampleView> EXAMPLES;
	private static final TimeStringValuer TIME_STRINGVAL = new TimeStringValuer();
	private static final String FORMAT = "Any format of table is acceptable, " +
				"all rows/columns will be shown as a plain table.";
	
	static {
		ExampleView ev3 = new ExampleView("Many Columned Table", 
				"All rows/columns will be shown as a plain table.",
				ExampleTestCases.COUNTRY_STATS);
		EXAMPLES = ImmutableList.of(ev3);
	}
	
	
	private DataTableViewStrategy(boolean debugView) {
		this.debugView = debugView;
	}
	
	/**
	 * @param debugView true means all columns will be shown in the output,
	 * 	including prefixed sd_ column names.
	 */
	public static ViewStrategy getInstance(boolean debugView) {
		if(debugView) {
			return DEBUG_INSTANCE;
		}
		return INSTANCE;
	}
	
	@Override public UpdateableView getView(ChartTheme theme) {
		return new DataTableUpdateableView(theme, debugView);
	}

	@Override public String getDescription() {
		return (debugView ? "Debug Table" : "Data Table");
	}

	@Override public String getFormatExplainationHtml() { return FORMAT; }
	@Override public String getFormatExplaination() { return FORMAT;	}

	@Override public List<ExampleView> getExamples() { return EXAMPLES; }
	
	@Override public String getQueryEg(JdbcTypes jdbcType) {
		if(jdbcType.equals(JdbcTypes.KDB)) {
			return ExampleTestCases.COUNTRY_STATS.getKdbQuery();
		}
		return null; 
	}
	
	/**
	 * A view that displays {@link ResultSet} data as a fresh {@link JTable} each time new data arrives.
	 */
	private static class DataTableUpdateableView implements UpdateableView {
	
		private static final String PREFIX = "sd_";
		private final DefaultTableModel tableModel;
		private final JXTable table;
		private final JPanel p;
		private final boolean debugView;
		private List<Color> rowBgColors = null;
		private List<Color> rowFgColors = null;
		
	
		public DataTableUpdateableView(final ChartTheme theme, boolean debugView) {
			tableModel = new DefaultTableModel();
			this.debugView = debugView;
			table = new JXTable(tableModel);
			table.addMouseListener(new SaveTableMouseAdapter(table, Theme.CIcon.CSV.get()));
			table.setEditable(false);
			

			DefaultTableRenderer defaultTabRenderer = new DefaultTableRenderer(TIME_STRINGVAL, JLabel.RIGHT) {
				@Override public Component getTableCellRendererComponent(
						JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					
					Component c = super.getTableCellRendererComponent(table, value, 
							isSelected, hasFocus, row, column);
					
					synchronized (this) {
						Color bcol = theme.getBackgroundColor();
						if(isSelected){
							bcol = theme.getSelectedBackgroundColor();
						} else if(row%2==1) {
							bcol = theme.getAltBackgroundColor();
						} 
						if(rowBgColors!= null && row<rowBgColors.size()) {
							Color cl = rowBgColors.get(row);
							if(cl != null) {
								bcol = cl;
							}
						} 
						c.setBackground(bcol);

						Color col = theme.getForegroundColor();
						if(isSelected){
							col = col.darker();
						}
						if(rowFgColors!= null && row<rowFgColors.size()) {
							Color cl = rowFgColors.get(row);
							if(cl != null) {
								col = cl;
							}
						} 
						c.setForeground(col);
					}
					
					return c;
				}
			};
			table.setDefaultRenderer(Object.class, defaultTabRenderer);
//			table.setDefaultRenderer(Number.class, defaultTabRenderer);

			table.packAll();
			table.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
			JScrollPane scrollPane = new JScrollPane(table, 
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			JTableHeader thead = table.getTableHeader();
			scrollPane.setColumnHeaderView(thead);
	        table.setPreferredScrollableViewportSize(table.getPreferredSize());
	        
	        thead.setBackground(theme.getBackgroundColor());
			scrollPane.setBackground(theme.getBackgroundColor());
			scrollPane.getViewport().setBackground(theme.getBackgroundColor());
			table.setBackground(theme.getBackgroundColor());
			
			p = new JPanel(new BorderLayout());
			p.add(scrollPane, BorderLayout.CENTER);
		}
	
		@Override public void update(ResultSet rs, ChartResultSet chartResultSet) 
				throws ChartFormatException {
			try {
				rebuildTableModel(rs);
			} catch (SQLException e) {
				throw new ChartFormatException("Could not create ResultSet.");
			}
		}
	
		@Override public Component getComponent() {
			return p;
		}
	
		private void rebuildTableModel(ResultSet rs) throws SQLException {

			rs.beforeFirst(); // in case someone else used it first.
		    ResultSetMetaData metaData = rs.getMetaData();
	
		    // names of columns
		    final Vector<String> columnNames = new Vector<String>();
		    final Vector<String> cleanNames = new Vector<String>();
		    int columnCount = metaData.getColumnCount();
		    for (int c = 1; c <= columnCount; c++) {
		    	String cn = metaData.getColumnName(c);
		        columnNames.add(cn);
		        if(!cn.toLowerCase().startsWith(PREFIX)) {
		        	cleanNames.add(cn);
		        }
		    }
	
			List<Color> rowBgColorsNew = new ArrayList<Color>();
			List<Color> rowFgColorsNew = new ArrayList<Color>();
		    
		    // data of the table
		    final Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		    while (rs.next()) {
		        Vector<Object> vector = new Vector<Object>();
		        for (int cIdx = 1; cIdx <= columnCount; cIdx++) {
		        	String cn = columnNames.get(cIdx-1).toLowerCase();
		        	if(debugView || !cn.startsWith(PREFIX)) {
			            vector.add(rs.getObject(cIdx));
		        	} 
		        	
		        	if(cn.equals(PREFIX + "bgcolor")){
		        		rowBgColorsNew.add(getColor(""+rs.getObject(cIdx)));
		        	} else if(cn.equals(PREFIX + "fgcolor")){
		        		rowFgColorsNew.add(getColor(""+rs.getObject(cIdx)));
		        	}
		        }
		        data.add(vector);
		    }

		    synchronized (this) {
				rowBgColors = rowBgColorsNew.size()>0 ? rowBgColorsNew : null;
				rowFgColors = rowFgColorsNew.size()>0 ? rowFgColorsNew : null;
			}
		    
			tableModel.setDataVector(data, (debugView ? columnNames : cleanNames));
			table.packTable(5);
		}

		private static Color getColor(String cVal) {
			Color cl = null;
			try {
				try {
					Field f = Color.class.getField(cVal);
					cl = (Color) f.get(null);
				} catch (SecurityException e) { } 
				catch (NoSuchFieldException e) { } 
				catch (IllegalArgumentException e) { } 
				catch (IllegalAccessException e) { }

				if(cl == null) {
					cl = Color.decode(cVal);
				}
			} catch (NumberFormatException e) {
				// ignore
			}
			return cl;
		}
	
	}

	@Override public Icon getIcon() { return CIcon.TABLE_ELEMENT; }

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (debugView ? 1231 : 1237);
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataTableViewStrategy other = (DataTableViewStrategy) obj;
		if (debugView != other.debugView)
			return false;
		return true;
	}

	@Override public String toString() {
		return DataTableViewStrategy.class.getSimpleName() + "[" + getDescription() + "]";
	}

	@Override public boolean isQuickToRender(ResultSet rs, int rowCount, int numColumnCount) {
		return rowCount < 211_000; // 1 seconds on Ryans PC
	}
	
	@Override public String getPulseName() { return "grid"; }
}