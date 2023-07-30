package com.timestored.qstudio.model;

import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.timestored.cstore.CAtomTypes;
import com.timestored.misc.HtmlUtils;
import com.timestored.theme.Theme.CIcon;

/** Base class that implements most methods for concreate classes to base themself off. */
abstract class BaseSQE implements ServerQEntity {
	
	private final String namespace;
	private final String name;
	private final CAtomTypes type;
	private final String serverName;
	
	protected BaseSQE(String serverName, String namespace, String name, CAtomTypes type) {
		this.namespace = Preconditions.checkNotNull(namespace);
		this.name = Preconditions.checkNotNull(name);
		this.type = type;
		this.serverName = serverName;
	}

	@Override public String getName() {
		return name;
	}

	@Override public String getNamespace() {
		return namespace;
	}

	@Override public CAtomTypes getType() {
		return type;
	}

	@Override public String getFullName() {
		 return (namespace.equals(".") ? "" : namespace + ".") + name;
	}
	
	@Override public String getDocName() {
		 return getFullName();
	}

	@Override public String getHtmlDoc(boolean shortFormat) {
		if(shortFormat) {
			return HtmlUtils.START +  "Type: " + getType().toString().toLowerCase() 
					+ " Count: " + getCount() + HtmlUtils.END;
		}
		return toHtml(ImmutableMap.of("Name: ", getDocName(), 
				"Type: ", getType().toString().toLowerCase(), 
				"Count: ", getCount()==-1 ? "unknown" : ""+ getCount()));
	}

	static String toHtml(Map<String, String> namesToDescs) {
		return HtmlUtils.START + HtmlUtils.toTable(namesToDescs, true) + HtmlUtils.END;
	}

	@Override public ImageIcon getIcon() {
		return type.getIcon().get16();
	}

	@Override public List<QQuery> getQQueries() {
		List<QQuery> r = Lists.newArrayList();
		r.add(new QQuery("Delete", CIcon.DELETE, "delete "+getName()+" from `"+getNamespace()));
		r.add(new QQuery("Count", null, "count " + getFullName()));
		return r;
	}

	@Override public String toString() {
		long c = getCount();
		return "Element[" + getName() + " type=" + type 
				+ (c!=1 ? " count=" + c : "") 
				+  (isTable() ? " isTable" : " ") 
				+ "]";
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (obj instanceof ServerQEntity) {
			ServerQEntity that = (ServerQEntity) obj;
			
			return Objects.equal(name, that.getName()) && 
				Objects.equal(namespace, that.getNamespace()) && 
				Objects.equal(type, that.getType()) && 
				Objects.equal(getCount(), that.getCount()) && 
				Objects.equal(getDocName(), that.getDocName()) && 
				Objects.equal(isTable(), that.isTable()) && 
				Objects.equal(getFullName(), that.getFullName()) && 
				Objects.equal(getQQueries(), that.getQQueries());
		}
		return false;
	}

	@Override public SourceType getSourceType() {
		return SourceType.SERVER;
	}

	@Override public String getSource() {
		return serverName;
	}

}