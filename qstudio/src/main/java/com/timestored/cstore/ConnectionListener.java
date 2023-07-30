package com.timestored.cstore;

interface ConnectionListener {

	void connectingFailure(String host, int port, String msg);

	void connecting(String host, int port);

	void connected(String host, int port);

	void query(String host, int port, Object query);

	void queryError(String host, int port, String query, String error);
}