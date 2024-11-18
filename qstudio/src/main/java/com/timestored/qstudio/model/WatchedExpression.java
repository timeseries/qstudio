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