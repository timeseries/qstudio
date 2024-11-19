package com.timestored.kdb;

import javax.sql.RowSet;

public interface QueryResultI extends AutoCloseable {

	String getConsoleView();

	Exception getE();

	Object getK();

	String getQuery();

	RowSet getRs();

	boolean isCancelled();

	boolean isExceededMax();

}