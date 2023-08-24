/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2023 TimeStored
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.timestored.qstudio.model;

import java.io.IOException;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import kx.c.KException;
import net.jcip.annotations.Immutable;

import com.google.common.base.Preconditions;
import com.timestored.kdb.KdbConnection;
import com.timestored.qstudio.kdb.KdbHelper;
import com.timestored.qstudio.kdb.KdbTableFactory;


/**
 * Contains information on the disk format, memory usage and
 * general info about one KDB server.
 */
@Immutable
public class ServerReport {
	
	private TableModel diskTab;
	private TableModel memTab;

	private final int ip;
	private final String hostname;
	private final int pid;
	private final java.sql.Date kdbReleaseDate;
	private final double kdbMajorVersion;
	private final String licenseInfo;
	private final String os;
	private final String userId;
	private final String commandLineArguments;
	private final int slaveThreads;

	private static final String INFO_K = ".:'[$`.z.a`.z.h`.z.i`.z.k`.z.K"
			+ "`.z.l`.z.o`.z.u`.z.x,`$\"\\\\s\"]";

	private static final String SEGMENTS_PARTITIONS_TAB_K = "{$[min `PV`PD`D`pf in !: `.Q; "
			+ "([] desc:! b; val:. b:(`Segments`Partitions`PartitionType)!(#.Q.D;#.Q.PD;.Q.pf)); "
			+ "([] Data:,\"Not partitioned or segmented\")]}[]";

	/** K code to retrieve a table reporting memory usage */
	private static final String MEMORY_REPORT_TAB_K = "+:{,x}'.Q.w[]";

	/**
	 * use the kdbConn to try and retrieve a server report. If invalid response returned
	 * all kinds of exeptions may be thrown.
	 */
	ServerReport(KdbConnection kdbConn) throws IOException, KException {

		Preconditions.checkNotNull(kdbConn);
		Preconditions.checkArgument(kdbConn.isConnected());

		Object k = kdbConn.query("k)("
				+ SEGMENTS_PARTITIONS_TAB_K + ";" + MEMORY_REPORT_TAB_K + ";"
				+ INFO_K + ")");

		Object[] resArray = (Object[]) k;
		
		this.diskTab = KdbTableFactory.getAsTableModel(resArray[0]);
		this.memTab = KdbTableFactory.getAsTableModel(resArray[1]);

		Object[] obj = (Object[]) resArray[2];

		this.ip = (int) (Integer) obj[0];
		this.hostname = (String) obj[1];
		this.pid = (int)  (Integer) obj[2];
		this.kdbReleaseDate = (java.sql.Date) obj[3];
		this.kdbMajorVersion = (Double) obj[4];
		
		licenseInfo = KdbHelper.asLine(obj[5]);
		
		this.os = (String) obj[6];
		this.userId = (String) obj[7];
		commandLineArguments = KdbHelper.asLine(obj[8]);
		this.slaveThreads = (int)  (Integer) obj[9];

	}

	public String getIp() {
		return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
				+ ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
	}

	public TableModel getGeneralInfoTable() {
		return new DefaultTableModel(new String[][] {
				new String[] { "IP Address", getIp() },
				new String[] { "Hostname", hostname },
				new String[] { "PID", "" + pid },
				new String[] { "KDB Release Date",
						(kdbReleaseDate!=null ? kdbReleaseDate.toString() : "") },
				new String[] { "KDB Version", "" + kdbMajorVersion },
				new String[] { "License", licenseInfo },
				new String[] { "OS", os },
				new String[] { "User ID", userId },
				new String[] { "Command Line Args", commandLineArguments },
				new String[] { "Slave Threads", "" + slaveThreads } },
				new String[] { "Property", "Value" });
	}

	/**
	 * @return Table displaying segments / partitions / partition type etc or null if unknown.
	 */
	public TableModel getDiskTab() {
		return diskTab;
	}

	/**
	 * @return Table showing memory usage etc. Similar to .Q.w[] or null if unknown.
	 */
	public TableModel getMemoryTab() {
		return memTab;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPid() {
		return pid;
	}

	public java.sql.Date getKdbReleaseDate() {
		return new java.sql.Date(kdbReleaseDate.getTime());
	}

	public double getKdbMajorVersion() {
		return kdbMajorVersion;
	}

	public String getLicenseInfo() {
		return licenseInfo;
	}

	public String getOs() {
		return os;
	}

	public String getUserId() {
		return userId;
	}

	public String getCommandLineArguments() {
		return commandLineArguments;
	}

	public int getSlaveThreads() {
		return slaveThreads;
	}
	
}