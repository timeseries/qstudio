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

import kx.c;

/**
 * Provides large part of the functionality of CTable reducing the effort 
 * for anyone wishing to implement {@link CTable}.
 */
abstract class AbstractCTable implements CTable {

	private final class QColumnIterator implements Iterator<CColumn> {
		
		private final int toIdx;
		private int i = 0;
		
		public QColumnIterator(final int fromIdx, final int toIdx) {
			i = fromIdx;
			this.toIdx = toIdx;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public CColumn next() {
			return new QuickQColumn(++i);
		}
		
		@Override
		public boolean hasNext() {
			return i < toIdx + 1;
		}
	}	

	@Override
	public String getKeysTitle() {
		if(getKeyColumnCount()==0) {
			return "index";
		} else {// (getKeyColumnCount() > 0)
			String title = "";
			for(int c=0; c<getKeyColumnCount(); c++) {
				title += getColumnName(c) + " ";
			}
			return title;
		}
	}

	@Override
	public String getRowTitle(int row) {
		
		if(getKeyColumnCount() == 0) {
			return "" + (row+1);
		} else { // (getKeyColumnCount() > 0)
			String title = "";
			for(int col=0; col<getKeyColumnCount(); col++) {
				title += c.at(getColumn(col), row) + " ";
			}
			return title;
		} 
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		} else if (o instanceof CTable) {
			CTable b = (CTable) o;
			if(b.getColumnCount() == this.getColumnCount()
					&& b.getRowCount() == this.getRowCount()) {
				for(int c=0; c<this.getColumnCount(); c++) {
					for(int r=0; r<this.getRowCount(); r++) {
						if(!this.getValueAt(r, c).equals(b.getValueAt(r, c))) {
							return false;
						}
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public CAtomTypes getType(int col) {
		return CAtomTypes.getType(getTypeNum(col));
	}

	@Override
	public Iterator<CColumn> getColumns() {
		return new QColumnIterator(0, getColumnCount() - 1);
	}
	
	@Override
	public Iterator<CColumn> getKeyColumns() {
		return new QColumnIterator(0, getKeyColumnCount() - 1);
	}

	@Override
	public CColumn getColumn(final String name) {
		for(int i = 0; i < this.getColumnCount(); i++) {
			if(getColumnName(i).equalsIgnoreCase(name)) {
				return new QuickQColumn(i);
			}
		}
		return null;
	}


	@Override
	public int getColumnIndex(final String name) {
		for(int i = 0; i < this.getColumnCount(); i++) {
			if(getColumnName(i).equalsIgnoreCase(name)) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public Iterator<CColumn> getNonKeyColumns() {
		return new QColumnIterator(getKeyColumnCount() - 1, getColumnCount());
	}

	
	private class QuickQColumn implements CColumn {
		
		private final int col;
		
		private QuickQColumn(int col) {
			this.col = col;
		}
		
		@Override
		public boolean isKey() {
			return col<getKeyColumnCount();
		}
		
		@Override
		public Object getValues() {
			return getColumn(col);
		}
		
		@Override
		public CAtomTypes getType() {
			return AbstractCTable.this.getType(col);
		}
		
		@Override
		public String getTitle() {
			return getColumnName(col);
		}
	}

	@Override
	public Object getDoubleAt(int row, int col) {
		return ((Number) getValueAt(row, col)).doubleValue();
	}
	
	@Override
	public String toString() {
		String s =  "QTable[ ([";
		for(int col=0; col<getKeyColumnCount(); col++) {
			s += getColumnName(col) + ",";
		}
		s += "]";
		for(int col=getKeyColumnCount(); col<getColumnCount(); col++) {
			s += getColumnName(col) + ",";
		}
		
		return s + ") ]";
	}
	
	@Override
	public boolean isKeyed() {
		return getKeyColumnCount()>0;
	}
}
