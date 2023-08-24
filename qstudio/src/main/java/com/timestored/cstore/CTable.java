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

import java.util.Iterator;

/**
 * Interface describing a column oriented table 
 */
public interface CTable {

	int getRowCount();

	int getColumnCount();

	Object getValueAt(int row, int col);
	
	Object getDoubleAt(int row, int col);

	String getColumnName(int col);

	Object getColumn(int col);

	int getTypeNum(int col);

	int getKeyColumnCount();

	CAtomTypes getType(int col);

	Iterator<CColumn> getColumns();

	Iterator<CColumn> getKeyColumns();

	Iterator<CColumn> getNonKeyColumns();

	String getRowTitle(int row);

	String getKeysTitle();

	CColumn getColumn(String name);

	int getColumnIndex(String name);

	boolean isKeyed();
}