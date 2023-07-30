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