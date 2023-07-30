package com.timestored.cstore;

public class ConnectionAdapter implements ConnectionListener {

	@Override
	public void connectingFailure(String host, int port, String msg) {
	}

	@Override
	public void connecting(String host, int port) {
	}

	@Override
	public void connected(String host, int port) {
	}

	@Override
	public void query(String host, int port, Object query) {
	}

	@Override
	public void queryError(String host, int port, String query, String error) {
	}
}
