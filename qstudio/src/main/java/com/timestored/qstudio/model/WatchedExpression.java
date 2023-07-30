package com.timestored.qstudio.model;

/**
 * An expression on a given server that is being watched.
 */
public class WatchedExpression {
	
	private String expression = "";
	private Object lastResult = null;
	private boolean changedDuringRefresh = false;
	
	WatchedExpression(String expression) {
		setExpression(expression);
	}
	
	public String getExpression() {
		return expression;
	}
	
	void setExpression(String expression) {
		this.expression = expression==null ? "" : expression;
	}
	
	public Object getLastResult() {
		return lastResult;
	}
	
	void setLastResult(Object lastResult) {
		
		if(lastResult != null) {
			changedDuringRefresh = lastResult.equals(this.lastResult);
		} else {
			changedDuringRefresh = this.lastResult == null;
		}
		this.lastResult = lastResult;
	}
	
	public boolean isChangedDuringRefresh() {
		return changedDuringRefresh;
	}

	@Override
	public String toString() {
		return "WatchedExpression [expression=" + expression + ", lastResult="
				+ lastResult + ", changedDuringRefresh=" + changedDuringRefresh + "]";
	}
	
}