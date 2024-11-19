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
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import javax.activation.UnsupportedDataTypeException;

import kx.c.KException;

import com.timestored.kdb.KdbConnection;
import com.timestored.kdb.SysCommand;

/**
 * Contains the information commonly accessible in KDB via the \ system commands. 
 */
public class ServerSlashConfig {

	private final String GET_CONFIG_K = ".:'[\"\\\\\",/:\"cCegopPstTWz\"]";

	private final Map<SysCommand, String> sysCmdToVal = new HashMap<SysCommand, String>();
	
	private final int consoleHeight;
	private final int consoleWidth;
	private final int webHeight;
	private final int webWidth;
	private final boolean errorTrap;
	private final int garbageCollectionMode;
	private final int gmtOffset;
	private final int port;
	private final int fpPrecisionShown;
	private final int slaveThreads;
	private final int timer;
	private final int timeout;
	private final int weekOffset;
	private final int dateMode;
	/**
	 * use the kdbConn to try and retrieve a server report. If invalid response returned
	 * all kinds of exeptions may be thrown.
	 */
	ServerSlashConfig(KdbConnection kdbConn) throws IOException, KException {

		Object o = kdbConn.query("k)" + GET_CONFIG_K);
		if(!(o instanceof Object[])) {
			throw new UnsupportedDataTypeException("ServerSlashConfig");
		}
		
		Object[] resArray = (Object[]) o;
		this.consoleHeight = (Integer) Array.get(resArray[0],0);
		this.consoleWidth = (Integer) Array.get(resArray[0],1);
		sysCmdToVal.put(SysCommand.c, consoleHeight + " " + consoleWidth);
		this.webHeight = (Integer) Array.get(resArray[1],0);
		this.webWidth = (Integer) Array.get(resArray[1],1);
		sysCmdToVal.put(SysCommand.C, webHeight + " " + webWidth);
		this.errorTrap = ((Integer) resArray[2]) == 0 ? false : true;
		sysCmdToVal.put(SysCommand.e, ""+resArray[2]);
		this.garbageCollectionMode = (Integer) resArray[3];
		sysCmdToVal.put(SysCommand.g, ""+garbageCollectionMode);
		this.gmtOffset = (Integer) resArray[4];
		sysCmdToVal.put(SysCommand.o, ""+gmtOffset);
		this.port = (Integer) resArray[5];
		sysCmdToVal.put(SysCommand.p, ""+port);
		this.fpPrecisionShown = (Integer) resArray[6];
		sysCmdToVal.put(SysCommand.P, ""+fpPrecisionShown);
		this.slaveThreads = (Integer) resArray[7];
		sysCmdToVal.put(SysCommand.s, ""+slaveThreads);
		this.timer = (Integer) resArray[8];
		sysCmdToVal.put(SysCommand.t, ""+timer);
		this.timeout = (Integer) resArray[9];
		sysCmdToVal.put(SysCommand.T, ""+timeout);
		this.weekOffset = (Integer) resArray[10];
		sysCmdToVal.put(SysCommand.W, ""+weekOffset);
		this.dateMode = (Integer) resArray[11];
		sysCmdToVal.put(SysCommand.z, ""+dateMode);
	}

	public int getConsoleHeight() {
		return consoleHeight;
	}

	public int getConsoleWidth() {
		return consoleWidth;
	}

	public int getWebHeight() {
		return webHeight;
	}

	public int getWebWidth() {
		return webWidth;
	}

	public boolean isErrorTrap() {
		return errorTrap;
	}

	public int getGarbageCollectionMode() {
		return garbageCollectionMode;
	}

	public int getGmtOffset() {
		return gmtOffset;
	}

	public int getPort() {
		return port;
	}

	public int getFpPrecisionShown() {
		return fpPrecisionShown;
	}

	public int getSlaveThreads() {
		return slaveThreads;
	}

	public int getTimer() {
		return timer;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getWeekOffset() {
		return weekOffset;
	}

	public int getDateMode() {
		return dateMode;
	}
	
	/** Return the value of this {@link SysCommand} if known, otherwsie null */
	public String getVal(SysCommand sysCommand) {
		return sysCmdToVal.get(sysCommand);
	}

	public void setVal(SysCommand scmd, String text) {
		//TODO this needs written
		throw new IllegalArgumentException(scmd.toString() + " text=" + text);
	}

}