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
package com.timestored.qstudio.servertree;

import javax.swing.JPopupMenu;

import com.timestored.qstudio.model.DatabaseDirector;
import com.timestored.qstudio.model.TableSQE;

/** 
 * Represents a single column on a given server/table.
 * Allows actions to modify columns.
 */
class ColNode extends CustomNode {

	private final DatabaseDirector.ActionsGenerator actionsGenerator;
	private final TableSQE table;
	private final String column;
	private final boolean partitionColumn;
	
	public ColNode(DatabaseDirector.ActionsGenerator actionsGenerator, TableSQE table, 
			String column, boolean partitionColumn) {
		
		super(column);
		this.actionsGenerator = actionsGenerator;
		this.table = table;
		this.column = column;
		this.partitionColumn = partitionColumn;
	}
	
	@Override public void addMenuItems(JPopupMenu menu) {
		actionsGenerator.addColumnMenuItems(menu, table, column, partitionColumn);
	}

}