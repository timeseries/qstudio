/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2024 TimeStored
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
package com.timestored.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * Limited size container that keeps on FIFO basis.
 */
public class FifoBuffer<E> {
	
	private final LinkedList<E> linkList;
    private final int limit;
    
    
	public FifoBuffer(int limit) {
		this.limit = limit;
		this.linkList = new LinkedList<E>();
	}

	public void addAll(Collection<E> items) {
		for(E e : items) {
			this.add(e);
		}
	}

	/**
	 * Add e to buffer, removing thelast entry to make room if needed.
	 * If e already in buffer it will be moved to front and
	 * and no other items will be evicted.
	 */
	public void add(E e) {
		int p = linkList.lastIndexOf(e);
		// not already in it and need to make room
		if(p==-1) {
			if(linkList.size()==limit) {
				linkList.removeLast();
			}
		} else {
			// re-adding at start of list
			linkList.remove(p);
		}
		linkList.addFirst(e);
	}

	public List<E> getAll() {
		return new ArrayList<E>(linkList);
	}
}