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
package com.timestored.cstore;

import java.lang.reflect.Array;

import kx.c;

/**
 * Construct a {@link CTable} by simply specifying the constituent columns.
 */
public class SimpleCTable extends AbstractCTable implements CTable {
	
	private final String[] colNames;
	private final Object[] colValues;
	private final int keyColumns;
	private final int rowCount;
	

	public SimpleCTable(String[] colNames, Object[] colValues, 
			int keyColumns) {
		super();
		
		this.colNames = colNames;
		this.colValues = colValues;
		this.keyColumns = keyColumns;
		rowCount = Array.getLength(colValues[0]);
	}

	/** {@inheritDoc} */ @Override
	public int getRowCount() {
		return rowCount;
	}

	/** {@inheritDoc} */ @Override
	public int getColumnCount() {
		return colNames.length;
	}

	/** {@inheritDoc} */ @Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return Array.get((colValues[columnIndex]), rowIndex);
	}

	/** {@inheritDoc} */ @Override
	public String getColumnName(int columnIndex) {
		return colNames[columnIndex];
	}

	/** {@inheritDoc} */ @Override
	public Object getColumn(final int col) {
		return colValues[col];
	}

	/** {@inheritDoc} */ @Override
	public int getKeyColumnCount() {
		return keyColumns;
	}

	/** {@inheritDoc} */ @Override
	public int getTypeNum(int col) {
		return c.t(colValues[col]);
	}

}
