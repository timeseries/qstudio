package com.timestored.sqldash.chart;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.google.common.base.Preconditions;

/**
 * Allows constructing  ResultSetMetaData from defined values.
 */
class SimpleResultSetMetaData implements ResultSetMetaData {

		private final String[] colNames;
		private final int[] colTypes;

		/**
		 * Construct a rs meta data with known values
		 * @param colNames
		 * @param colTypes
		 */
		public SimpleResultSetMetaData(String[] colNames, int[] colTypes) {
			this.colNames = Preconditions.checkNotNull(colNames);
			this.colTypes = Preconditions.checkNotNull(colTypes);
			Preconditions.checkArgument(colNames.length == colTypes.length, "length of names and types match");
		}

		@Override public int getColumnCount() throws SQLException {
			return colNames.length;
		}

		@Override public int getColumnType(int column) throws SQLException {
			
			if(column>colNames.length) {
				throw new IllegalArgumentException("column outside data range");
			}
			
			return colTypes[column-1];
		}
		
		@Override public String getColumnLabel(int column) throws SQLException {
			return getColumnName(column);
		}

		@Override public String getColumnName(int column) throws SQLException {
			return colNames[column-1];
		}

		@Override public String getColumnClassName(int column) throws SQLException {
			throw new UnsupportedOperationException();
//			return getObject(column).getClass().getName();
		}
		
		@Override public boolean isWrapperFor(Class<?> iface) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public <T> T unwrap(Class<T> iface) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public String getCatalogName(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}


		@Override public int getColumnDisplaySize(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}


		@Override public String getColumnTypeName(int column) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override public int getPrecision(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public int getScale(int column) throws SQLException {
			return 0;
		}

		@Override public String getSchemaName(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public String getTableName(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public boolean isAutoIncrement(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public boolean isCaseSensitive(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public boolean isCurrency(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public boolean isDefinitelyWritable(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public int isNullable(int column) throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override public boolean isReadOnly(int column) throws SQLException {
			return true;
		}

		@Override public boolean isSearchable(int column) throws SQLException {
			return false;
		}

		@Override public boolean isSigned(int column) throws SQLException {
			return false;
		}

		@Override public boolean isWritable(int column) throws SQLException {
			return false;
		}
		
	}