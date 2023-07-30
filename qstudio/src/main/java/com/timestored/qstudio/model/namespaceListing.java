package com.timestored.qstudio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.timestored.cstore.CAtomTypes;


/**
 * Contains details on all elements (tables/functions/variables)  within a namespace.
 */
class NamespaceListing {
	
	private final List<TableSQE> tables;
	private final List<ServerQEntity> functions;
	private final List<ServerQEntity> variables;
	private final List<ServerQEntity> views;
	private final List<ServerQEntity> allElements;
	
	NamespaceListing(List<ServerQEntity> allElements) {
		super();
		
		this.allElements = new ArrayList<ServerQEntity>(Preconditions.checkNotNull(allElements));
		List<TableSQE> tables = new ArrayList<TableSQE>();
		List<ServerQEntity> functions = new ArrayList<ServerQEntity>();
		List<ServerQEntity> variables = new ArrayList<ServerQEntity>();
		List<ServerQEntity> views = new ArrayList<ServerQEntity>();
		
		for(ServerQEntity ed : allElements) {
			if(ed.isTable() && ed instanceof TableSQE) {
				tables.add((TableSQE)ed);
			} else if(ed.getType().equals(CAtomTypes.VIEW)) {
				views.add(ed);
			} else if(ed.getType().isFunction()) {
				functions.add(ed);
			} else {
				variables.add(ed);
			}
		}

		this.tables = Collections.unmodifiableList(tables);
		this.functions = Collections.unmodifiableList(functions);
		this.variables = Collections.unmodifiableList(variables);
		this.views = Collections.unmodifiableList(views);
	}
	
	@Override
	public String toString() {
		return "tables = " + tables.toString()
				+ "views = " + views.toString()
				+ "functions = " + functions.toString()
				+ "variables = " + variables.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((allElements == null) ? 0 : allElements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamespaceListing other = (NamespaceListing) obj;
		if (allElements == null) {
			if (other.allElements != null)
				return false;
		} else if (!allElements.equals(other.allElements))
			return false;
		return true;
	}
	
	
	
	public List<ServerQEntity> getAllElements() {
		return allElements;
	}

	
	public List<TableSQE> getTables() {
		return tables;
	}

	
	public List<ServerQEntity> getFunctions() {
		return functions;
	}

	
	public List<ServerQEntity> getVariables() {
		return variables;
	}

	
	public List<ServerQEntity> getViews() {
		return views;
	}

	public boolean contains(QEntity element) {
		return allElements.contains(element);
	}
	
	
}
