package com.timestored.connections;

import java.io.IOException;
import java.sql.SQLException;

import com.google.common.base.Preconditions;
import com.timestored.kdb.KdbTestHelper;

/**
 * Allows getting a {@link DBTestRunner} for a particular database type. 
 */
public class DBTestRunnerFactory {

	/** @return A test runner for the given database type where possible, otherwise null **/
	public static DBTestRunner getDbRunner(JdbcTypes jdbcType) {
		Preconditions.checkNotNull(jdbcType);
		
		if(jdbcType.equals(JdbcTypes.H2)) {
			return H2DBTestRunner.getInstance();
		} else if(jdbcType.equals(JdbcTypes.KDB)) {
			return KdbDBTestRunner.INSTANCE;
		}
		
		return null;
	}
	
	
	private static class KdbDBTestRunner implements DBTestRunner {

		private static KdbDBTestRunner INSTANCE = new KdbDBTestRunner();
		
		@Override public ConnectionManager start() throws SQLException {
			try {
				return KdbTestHelper.getNewConnectedMangager();
			} catch (IOException e) {
				throw new SQLException(e);
			} catch (InterruptedException e) {
				throw new SQLException(e);
			}
		}

		@Override public void stop() {
			try {
				KdbTestHelper.killAnyOpenProcesses();
			} catch (IOException e) { }
		}

		@Override public ServerConfig getServerConfig() {
			return KdbTestHelper.SERVER_CONFIG;
		}
		
	}
}
