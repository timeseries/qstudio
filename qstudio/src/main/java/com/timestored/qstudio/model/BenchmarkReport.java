package com.timestored.qstudio.model;

import java.io.IOException;
import java.util.logging.Logger;

import javax.activation.UnsupportedDataTypeException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import kx.c.KException;

import com.google.common.base.Preconditions;
import com.timestored.kdb.KdbConnection;
import com.timestored.misc.IOUtils;
import com.timestored.qstudio.kdb.KdbTableFactory;

/**
 * Contains information about a single namespace on a selected server.
 */
public class BenchmarkReport {

	private static final Logger LOG = Logger.getLogger(BenchmarkReport.class.getName());

	private final TableModel throughputReport;
	private final TableModel ioReport;
	private final TableModel tableReadReport;
	

	private final String[] ioOp = new String[] { "append", "hcount", "read1", "value", "Segmented Read" };
	private final String[] tableReadOp = new String[] { "boolean scan", "int scan", "long scan", "sorted int" };

	/**
	 * Try to generate a benchmark for the given connection.
	 * @param kdbConn
	 * @throws UnsupportedDataTypeException If response from server is unrecognised format.
	 */
	BenchmarkReport(KdbConnection kdbConn) throws IOException, KException {
		
		Preconditions.checkNotNull(kdbConn);
		
		String funcDefs = IOUtils.toString(BenchmarkReport.class, "bench.q");
		String qry = "{ " + funcDefs + " ; select batchSize,{6#x}'[string insertsPerSecond] from " +
				"runThroughputBenchmark[]}[]";
		LOG.fine("Running throughputReport.");
		Object r = kdbConn.query(qry);
		throughputReport = KdbTableFactory.getAsTableModel(r);
		
		if(throughputReport == null) {
			throw new UnsupportedDataTypeException("Response from server was not a table");
		}
		

		String[] columnNames = new String[] { "Operation", "Time", "Standard Time" };
		ioReport = new DefaultTableModel(columnNames, ioOp.length) {
			@Override public Object getValueAt(int row, int column) {
				if(column==0) {
					return ioOp[row];
				}
				return ".";
			}
		};

		String[] cNames = new String[] { "Operation", "Time", "Standard Time" };
		tableReadReport = new DefaultTableModel(cNames, tableReadOp.length) {
			@Override public Object getValueAt(int row, int column) {
				if(column==0) {
					return tableReadOp[row];
				}
				return ".";
			}
		};
		
	}

	/**
	 * @return table model displaying throughput report.
	 */
	public TableModel getThroughputReport() {
		return throughputReport;
	}

	public TableModel getIOReport() {
		return ioReport;
	}

	public TableModel getTableReadReport() {
		return tableReadReport;
	}
}
