package com.timestored.qstudio.kdb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.serial.SerialArray;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import kx.c;
import kx.c.Flip;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.table.TableColumnExt;

import com.google.common.base.Preconditions;
import com.timestored.babeldb.DBHelper;
import com.timestored.cstore.CColumn;
import com.timestored.cstore.CTable;
import com.timestored.cstore.SimpleCTable;
import com.timestored.swingxx.SaveTableMouseAdapter;
import com.timestored.theme.Theme;

/**
 * Provides methods for converting raw KDB data {@link c.Flip},{@link c.Dict}
 * to {@link CTable}.
 */
public class KdbTableFactory {

	private static final Logger LOG = Logger.getLogger(KdbTableFactory.class.getName());
	private static final KdbStringValuer KDB_STRING_VALER = new KdbStringValuer();
	private static final Highlighter KEY_COL_HIGHLIGHTER = new ColorHighlighter(new Color(222,188,255), Color.BLACK);

	/** Once a column is over this width, restrict it to cutoff some text **/
	private static final int MAX_COL_WIDTH = 700;	

	private static final Comparator MANY_COMPARATOR = new DataComparator();
	
	private static <T> T[] concat(T[] first, T[] second) {
		  T[] result = Arrays.copyOf(first, first.length + second.length);
		  System.arraycopy(second, 0, result, first.length, second.length);
		  return result;
		}

	/**
	 * Convert the {@link c.Flip} into a {@link CTable} if possible.
	 */
	static CTable getQTable(c.Flip flip) {
		return new SimpleCTable(flip.x, flip.y, 0);
	}

	/**
	 * Convert the {@link c.Dict} into a {@link CTable} if possible.
	 */
	static CTable getQTable(c.Dict dict) {
		
		if(dict.x instanceof Flip) {
			Flip keyCols = (Flip)dict.x;
			Flip valCols = (Flip)dict.y;
			
			String[] colnames = concat(keyCols.x,  valCols.x);
			Object[] vals = concat(keyCols.y,  valCols.y);
			
			return new SimpleCTable(colnames, vals, keyCols.x.length);
		}

		if(dict.x.getClass().isArray()) {
			Array.getLength(dict.x);
			String[] colnames = new String[] {"Key", "Value"};
			Object[] colValues = new Object[] { dict.x, dict.y };

			return new SimpleCTable(colnames, colValues, 1);
		}
		return null;
	}

	/**
	 * Convert the KDB object into a {@link CTable} if possible.
	 * @return CTable if possible otherwise null.
	 */
	static CTable getCTable(Object o) {
		if(o == null) {
			return null;
		}
		if(o instanceof c.Flip){
			return getQTable((c.Flip) o);
		} else if(o instanceof c.Dict){
			return getQTable((c.Dict) o);
		} else if(o.getClass().isArray() && !(o instanceof char[])) {
			String title = "List";
			if(Array.getLength(o) > 0) {
				Object v = Array.get(o, 0);
				if(v != null) {
					KdbType kt = KdbType.getType(v.getClass());
					if(kt != null) {
						title = kt.name() + " List";
					}
				}
			}
			return getQTable(new c.Flip(new c.Dict(new String[] {title }, new Object[] {o})));
		}
		return null;
	}


	/**
	 * Convert the KDB object into a {@link TableModel} if possible.
	 * @return TableModel if possible otherwise null.
	 */
	public static TableModel getAsTableModel(Object o) {
		CTable ct = getCTable(o);
		return ct==null ? null : new CTableModel(ct);
	}

	
	public static JPanel getPlainReadonlyTable(TableModel tableModel) {
		
		JXTable table = new JXTable(tableModel);
		
		table.getTableHeader().setReorderingAllowed(false);
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(table, BorderLayout.CENTER);
		tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tablePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JPanel tContainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tContainerPanel.add(tablePanel);

		setPresentation(table);
		
		return tContainerPanel;
	}
	
	/**
	 * COnverts the underlying KDB objects to a presentable form for in a table.
	 */
	public static class KdbStringValuer implements StringValue {
		
		private static final long serialVersionUID = 1L;

		@Override public String getString(Object value) {
			Object o = KdbHelper.asLine(value, true);
			if(o!=null) {
				return o.toString();
			}
			return StringValues.TO_STRING.getString(value);
		}
	 };
	 

	 
	public static Component getJXTable(Object kObject) {
		return getJXTableFromKdb(kObject, Integer.MAX_VALUE);
	}
	
	
	public static Component getJXTableFromKdb(Object kObject, int maxRowsShown) {
		try {
			Box b = Box.createVerticalBox();
			CTable ctable = getCTable(kObject);
			
			if(ctable != null) {
				final TableModel tableModel = new CTableModel(ctable, maxRowsShown);
				JScrollPane scrollPane = getTable(tableModel, ctable.getKeyColumnCount());
				if(ctable.getRowCount() > maxRowsShown) {
					b.add(new JLabel("<html><b>Warning: some rows not shown " +
						"as over max display limit: " + maxRowsShown + "</b></html>"));
				}
				b.add(scrollPane);
				return b;
			}
		} catch(Exception e) {
			LOG.log(Level.WARNING, "problem creating table", e);
		}
		return null;
	}
	
	public static DefaultTableModel buildTableModel(ResultSet rs, int maxRowsShown) throws SQLException {

	    ResultSetMetaData metaData = rs.getMetaData();

	    // names of columns
	    Vector<String> columnNames = new Vector<String>();
	    int columnCount = metaData.getColumnCount();
	    for (int column = 1; column <= columnCount; column++) {
	        columnNames.add(metaData.getColumnName(column));
	    }

	    // data of the table
	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    for(int row = 0;rs.next() && row < maxRowsShown;row++) {
	        Vector<Object> vector = new Vector<Object>();
	        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
	        	Object o = rs.getObject(columnIndex); // notice this is getObject. getArray doesn't work for H2
	        	if(rs.getMetaData().getColumnType(columnIndex) == java.sql.Types.ARRAY) {
	        		vector.add(DBHelper.convertArrayToString(o));
	        	} else {
	        		vector.add(o);
	        	}
	        }
	        data.add(vector);
	    }

	    return new DefaultTableModel(data, columnNames);

	}

	public static JScrollPane getTable(final TableModel tableModel, int keyColCount) {
		final JXTable table = Theme.getStripedTable(tableModel);
		
		table.setCellSelectionEnabled(true);
		for(int i=0; i<keyColCount; i++) {
			table.getColumnExt(i).setHighlighters(KEY_COL_HIGHLIGHTER );
		}
		setPresentation(table);
		for(int ci=0; ci<table.getColumnCount(); ci++) {
			TableColumnExt tce = table.getColumnExt(ci);
			tce.setComparator(MANY_COMPARATOR);
			if(tce.getPreferredWidth() > MAX_COL_WIDTH) {
				tce.setPreferredWidth(MAX_COL_WIDTH);
			}
		}
		
		JScrollPane scrollPane = new JScrollPane(table,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		// Add a frozen first column that contains the row number
		TableModel rowTableModel = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;
			@Override public Object getValueAt(int rowIndex, int columnIndex) {
				return rowIndex;
			}
			@Override public int getRowCount() { return tableModel.getRowCount(); }
			@Override public int getColumnCount() { return 1; }
			@Override public String getColumnName(int column) {return "";	}
			@Override public Class<?> getColumnClass(int columnIndex) {return Number.class;}
		};
		JXTable rowTable = new JXTable(rowTableModel);
		rowTable.getColumnExt(0).setComparator(MANY_COMPARATOR);
		rowTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		rowTable.getRowSorter().addRowSorterListener(new RowSorterListener() {
			
			@Override public void sorterChanged(RowSorterEvent e) {
				RowSorter v = e.getSource();
				List l = v.getSortKeys();
				table.getRowSorter().setSortKeys(l);
				System.out.println(Arrays.toString(l.toArray()));
				System.out.println(l.toString());
			}
		});
		rowTable.packAll();
		rowTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		rowTable.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
		        int row = rowTable.rowAtPoint(e.getPoint());
		        // If shift held and already row selected, highlight between the rows.
		        if(row >= 0) {
			        if(e.isShiftDown() && table.getSelectedRow() >= 0) {
			        	int lowRow = Math.min(row, table.getSelectedRow());
			        	int hiRow = Math.max(row, table.getSelectedRow());
						table.setRowSelectionInterval(lowRow, hiRow);
						table.setColumnSelectionInterval(0, table.getColumnCount() - 1);	
			        } else {
						table.setRowSelectionInterval(row, row);
						table.setColumnSelectionInterval(0, table.getColumnCount() - 1);	
			        }
		        }
				super.mouseClicked(e);
			}
		});

		rowTable.getColumnExt(0).setCellRenderer(new DefaultTableRenderer() {
			private static final long serialVersionUID = 1L;
			@Override public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel l = new JLabel("" + value.toString());
				l.addMouseListener(new MouseAdapter() {
					@Override public void mouseClicked(MouseEvent e) {
						System.out.println("" + row);
						table.setRowSelectionInterval(row, row+1);
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						System.out.println("" + row);
					}
				});
				l.setBorder(BorderFactory.createRaisedBevelBorder());
				return l; 
			}
		});
		
		scrollPane.setRowHeaderView(rowTable);
		scrollPane.setColumnHeaderView(table.getTableHeader());
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
		return scrollPane;
	}

	/** Set how values are rendered and pack the columns */
	private static void setPresentation(final JXTable table) {
		
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.addMouseListener(new SaveTableMouseAdapter(table, Theme.CIcon.CSV.get(), KDB_STRING_VALER));
		DefaultTableRenderer defaultTabRenderer = new DefaultTableRenderer(KDB_STRING_VALER, JLabel.RIGHT);
		table.setDefaultRenderer(Object.class, defaultTabRenderer);
		table.setDefaultRenderer(Number.class, defaultTabRenderer);

		table.packAll();
		table.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
	}
	
	
	static TableModel getAsTableModel(CTable qtab) {
		return new CTableModel(qtab);
	}
	
	/**
	 * Can be used to wrap a {@link CTable} and make it a {@link TableModel};
	 */
	private static class CTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private final CTable tab;
		private final int rowsReported;
		
		public CTableModel(CTable tab) {
			this.tab = Preconditions.checkNotNull(tab);
			rowsReported = tab.getRowCount();
		}
		
		/**
		 * Construct a tableModel but limit the number of rows reported.
		 * @param rowLimit Limit TableModel to this number of rows.
		 */
		public CTableModel(CTable tab, int rowLimit) {
			this.tab = Preconditions.checkNotNull(tab);
			Preconditions.checkArgument(rowLimit >= 0);
			int rc = tab.getRowCount();
			rowsReported = rc>rowLimit ? rowLimit : rc;
		}
		
		@Override
		public int getColumnCount() {
			return tab.getColumnCount();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			CColumn c = tab.getColumn(tab.getColumnName(columnIndex));
			if(c.getType().isNumber()) {
				return Number.class;
			}
			return super.getColumnClass(columnIndex);
		}
		
		@Override
		public int getRowCount() {
			return rowsReported;
		}

		@Override
		public Object getValueAt(int row, int col) {
			return tab.getValueAt(row, col);
		}
		
		@Override
		public String getColumnName(int column) {
			return tab.getColumnName(column);
		}
	}

	public static JScrollPane getTable(ResultSet rs, int maxRowsShown) throws SQLException {
		DefaultTableModel tableModel = KdbTableFactory.buildTableModel(rs, maxRowsShown);
		return getTable(tableModel, 0);
	}
	
}
