package com.timestored.sqldash.chart;


import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.timestored.connections.JdbcTypes;
import com.timestored.misc.IOUtils;
import com.timestored.swingxx.SwingUtils;
import com.timestored.theme.Icon;
import com.timestored.theme.ShortcutAction;
import com.timestored.theme.Theme;
import com.timestored.theme.Theme.CIcon;

/**
 * Allows rendering resultSets using GraphViz dot rendering.
 */
public class DotViewStrategy implements ViewStrategy {

	private static final String KDB_QUERY = "([] from:`a`b`c`d`e; \r\n\tto:`c`c`e`c`a; \r\n\tlabel:1 2 0N 0N 3; \r\n\tstyle:(\"color=green\";\"\";\"fillcolor=\\\"cyan:red\\\"\";\"\";\"color=red,penwidth=3.0\"))";
	private static final Logger LOG = Logger.getLogger(DotViewStrategy.class.getName());
	public static final DotViewStrategy INSTANCE = new DotViewStrategy();

	private static final String[] FORMATA = 
		{ "The table can contain columns labelled from/to/label/style.",
				"If it does not, the first two columns are assumed to be from and to respectively.",
				"The dot file generated gives a directed graph from \"from\" nodes to \"to\" nodes with applicable style/labels.",
				"Graphviz Dot MUST be on your command line PATH for this to work." };

	private DotViewStrategy() {}

	@Override
	public UpdateableView getView(ChartTheme theme) {
		Preconditions.checkNotNull(theme);

		return new HardRefreshUpdateableView(new HardRefreshUpdateableView.ViewGetter() {

			@Override
			public Component getView(ResultSet resultSet, ChartResultSet chartResultSet) throws ChartFormatException {

				if (chartResultSet == null) { throw new ChartFormatException("Could not construct ResultSet."); }
				String dot;
				try {
					dot = createDot(resultSet);
					Component display = null;
					try {
						display = getDotRendered(dot);
					} catch (IOException e) {
						LOG.fine("Error drawing graph:" + e);
					}
					if(display == null) {
						JPanel p = Theme.getVerticalBoxPanel();
						p.add(Theme.getHeader("Dot File"));
						p.add(Theme.getTextArea("dot-explain", "Dot must be in your system path to allow rendering the graph.\r\n"
								+ "Alternatively copy-paste the below into the website: http://www.webgraphviz.com/"));
						p.add(Theme.getTextArea("dot", dot));
						display = new JScrollPane(p);
					}
					return display;
				} catch (SQLException e) {
					throw new ChartFormatException("Bad Dot format.");
				}
			}
		});
	}

	private static Action getSavePngAction(final File targetPng) {
		return new ShortcutAction("Save .png", CIcon.SAVE, "Save .png") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				File loc = SwingUtils.askUserSaveLocation(null, "png");
				try {
					Files.copy(targetPng, loc);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Problem saving .png " + e1);
				}
			}
		};
	}

	/**
	 * @return A component displaying the graph if possible otehrwise null.
	 */
	private static Component getDotRendered(final String dot) throws IOException {

		final Action copyDotTxtAction = new ShortcutAction("Copy .dot", CIcon.COPY, "Copy .dot to clipboard") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(new StringSelection(dot), null);
			}
		};
		
		JPanel p = Theme.getVerticalBoxPanel();
		File f = File.createTempFile("sqldash", ".dot");
		final File targetPng = File.createTempFile("sqldash", ".png");
		IOUtils.writeStringToFile(dot, f);
		String cmd = "dot -Tpng " + f.getAbsolutePath() + " -o " + targetPng.getAbsolutePath();
		LOG.info(cmd);
		try {
			Runtime.getRuntime().exec(cmd).waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BufferedImage myPicture = ImageIO.read(targetPng);
		if(myPicture == null) {
			return null;
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		picLabel.addMouseListener(new MouseAdapter() {
			public void mouseReleased(java.awt.event.MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu p = new JPopupMenu();
					p.add(getSavePngAction(targetPng));
					p.add(copyDotTxtAction);
					p.show(e.getComponent(), e.getX(), e.getY());
				}
			};
		});
		p.add(picLabel);
		return new JScrollPane(p);
		
	}

	private static String createDot(ResultSet rs) throws SQLException {

		StringBuilder sb = new StringBuilder("digraph {\r\n");

		int fromIndex = findColumnElse(rs, "from");
		if (fromIndex == -1) {
			fromIndex = 1;
		}

		int toIndex = findColumnElse(rs, "to");
		if (toIndex == -1 && rs.getMetaData().getColumnCount() > 1) {
			toIndex = fromIndex == 1 ? 2 : 1;
		}
		int connectorIndex = findColumnElse(rs, "connector");
		int labelIndex = findColumnElse(rs, "label");
		int styleIndex = findColumnElse(rs, "style");

		rs.beforeFirst(); // in case someone else used it first.
		while (rs.next()) {
			sb.append('\t').append(e(rs.getString(fromIndex), "")).append(' ');
			sb.append(connectorIndex == -1 ? "-> " : e(rs.getString(connectorIndex), "-> "));
			sb.append(toIndex == -1 ? "" : e(rs.getString(toIndex), ""));
			String labelT = labelIndex == -1 ? "" : e(rs.getString(labelIndex), "");
			String styleT = styleIndex == -1 ? "" : e(rs.getString(styleIndex), "");
			if (labelT.length() > 0) {
				sb.append(" [label=" + labelT + (styleT.length() > 0 ? "," + styleT : "") + "]");
			} else if (styleT.length() > 0) {
				sb.append(" [" + styleT + "]");
			}
			sb.append(";\r\n");
		}

		return sb.append("}").toString();
	}

	private static String e(String s, String defaultVal) {
		if (s == null || s.length() == 0) { return defaultVal; }
		return s.contains(" ") ? ("\"" + s + "\"") : s;
	}

	private static int findColumnElse(ResultSet rs, String columnLabel) {
		try {
			int i = rs.findColumn(columnLabel);
			if (i > rs.getMetaData().getColumnCount()) { return -1; }
			return i;
		} catch (SQLException e) {}
		return -1;
	}

	@Override
	public String getDescription() {
		return ("DOT Graph");
	}


	@Override public String getFormatExplainationHtml() {
		return "<ol><li>" + Joiner.on("</li><li>").join(FORMATA) + "</li></ol>";
	}
	
	@Override public String getFormatExplaination() {
		return Joiner.on("\r\n").join(FORMATA);
	}

	@Override
	public List<ExampleView> getExamples() {
		
		String description = "A directed graph with 5 nodes.";
		String name = "Alphabet Spaghetti";
		String[] colNames = new String[] { "from", "to", "label", "style" };
		String[] colFrom = new String[] { "a", "b", "c", "d", "e" };
		String[] colTo = new String[] { "c", "c", "e", "c", "a" };
		Integer[] colLabel = new Integer[] { 1, 2, null, null, 3 };
		String[] colStyle = new String[] { "c", "c", "e", "c", "a" };
		Object[] colValues = new Object[] { colFrom, colTo, colLabel, colStyle };
		ResultSet resultSet = new SimpleResultSet(colNames, colValues);
		ExampleView graphEV = new ExampleView(name, description, new TestCase(name, resultSet, KDB_QUERY));
		return ImmutableList.of(graphEV);
		
	}

	@Override
	public String getQueryEg(JdbcTypes jdbcType) {
		if (jdbcType.equals(JdbcTypes.KDB)) { return KDB_QUERY; }
		return null;
	}

	@Override public Icon getIcon() { return CIcon.EYE; }

	@Override public boolean isQuickToRender(ResultSet rs, int rowCount, int numColumnCount) {
		return rowCount < 11_000; // Unknown as never ran it
	}
	
	@Override public String getPulseName() { return null; }
}