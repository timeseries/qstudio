package com.timestored.qstudio.model;


import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.timestored.cstore.CAtomTypes;
import com.timestored.misc.HtmlUtils;

/**
 * Factory to aid construction of {@link ServerQEntity}'s.
 */
class ServerQEntityFactory {

	
	/**
	 * @return {@link ServerQEntity} that fits the passed params if possible otherwise null.
	 * @param partitioned true if it's partitioned (only applies to tables)
	 * @param isView true if its a view (views in KDB are type 100, count 1 etc)
	 * @param colArgNames If the type is table, this is the column names
	 * 		if it's specified as lambda, this is the arguments
	 * @param count Count if known or -1 to specify unknown.
	 */
	static ServerQEntity get(String serverName, String namespace, String name, Short typeNum, long count,
			boolean isTable, boolean partitioned, boolean isView, String[] colArgNames) {
		CAtomTypes t = CAtomTypes.getType(typeNum);
		if(t==null) {
			return null; // can't recognise type, return
		}
		if(isView) {
			return ServerQEntityFactory.getView(serverName, namespace, name);
		} else if(isTable) {
			return ServerQEntityFactory.getTable(serverName, namespace, name, typeNum, count, partitioned, colArgNames);
		} else if(t.isList()) {
			return new ListSQE(serverName, namespace, name, t, count);
		} else if (t.equals(CAtomTypes.DICTIONARY)){
			return new DictSQE(serverName, namespace, name, count);
		} else if (t.equals(CAtomTypes.LAMBDA)){
			return new LambdaSQE(serverName, namespace, name, colArgNames);
		} else {
			return new AtomSQE(serverName, namespace, name, t);
		}
	}

	static ServerQEntity getTable(String serverName, String namespace, String name, int typeNum, long count, 
			boolean isPartitioned, String[] colNames) {
		return new TableSQE(serverName, namespace, name, CAtomTypes.getType(typeNum), 
				count, isPartitioned,  colNames);
	}

	static ServerQEntity getAtom(String serverName, String namespace, String name, int typeNum) {
		return new AtomSQE(serverName, namespace, name, CAtomTypes.getType(typeNum));
	}

	static ServerQEntity getDict(String serverName, String namespace, String name, int count) {
		return new DictSQE(serverName, namespace, name, count);
	}

	static ServerQEntity getLambda(String serverName, String namespace, String name, String[] argNames) {
		return new LambdaSQE(serverName, namespace, name, argNames);
	}
	
	static ServerQEntity getView(String serverName, String namespace, String name) {
		return new ViewSQE(serverName, namespace, name);
	}
	


	/** {@link ServerQEntity} for lists only */
	private static class ListSQE extends BaseSQE {

		private final long count;
		
		public ListSQE(String serverName, String namespace, String name, CAtomTypes type, long count) {
			super(serverName, namespace, name, type);
			Preconditions.checkArgument(count>=0);
			Preconditions.checkArgument(type.isList());
			this.count = count;
		}

		@Override public long getCount() {
			return count;
		}

		@Override public boolean isTable() {
			return false;
		}
		
	}

	/** {@link ServerQEntity} for dicts only */
	private static class DictSQE extends BaseSQE {

		private final long count;
		
		public DictSQE(String serverName, String namespace, String name, long count) {
			super(serverName, namespace, name, CAtomTypes.DICTIONARY);
			Preconditions.checkArgument(count>=0);
			this.count = count;
		}

		@Override public long getCount() {
			return count;
		}

		@Override public boolean isTable() {
			return false;
		}
	}

	/** {@link ServerQEntity} for atoms only */
	private static class AtomSQE extends BaseSQE {
		
		public AtomSQE(String serverName, String namespace, String name, CAtomTypes type) {
			super(serverName, namespace, name, type);
			Preconditions.checkArgument(type.isAtom());
		}

		@Override public long getCount() {
			return 1;
		}

		@Override public boolean isTable() {
			return false;
		}

		@Override public String getHtmlDoc(boolean shortFormat) {
			if(shortFormat) {
				return HtmlUtils.START + "Type: " + getType().toString().toLowerCase()
						+ HtmlUtils.END;
			}
			return toHtml(ImmutableMap.of("Name: ", getDocName(), 
					"Type: ", getType().toString().toLowerCase()));
		}
	}

	/** {@link ServerQEntity} for atoms only */
	private static class ViewSQE extends BaseSQE {
		
		public ViewSQE(String serverName, String namespace, String name) {
			super(serverName, namespace, name, CAtomTypes.VIEW);
		}

		@Override public long getCount() {
			return 1;
		}

		@Override public boolean isTable() {
			return false;
		}

		@Override public String getHtmlDoc(boolean shortFormat) {
			return toHtml(ImmutableMap.of("Name: ", getDocName(), 
					"Type: ", getType().toString().toLowerCase()));
		}
	}


	/** {@link ServerQEntity} for atoms only */
	private static class LambdaSQE extends BaseSQE {
		
		final List<String> argNames;
		
		public LambdaSQE(String serverName, String namespace, String name, String[] argNames) {
			super(serverName, namespace, name, CAtomTypes.LAMBDA);
			this.argNames = Arrays.asList(argNames);
		}

		@Override public long getCount() {
			return 1;
		}

		@Override public boolean isTable() {
			return false;
		}
		
		@Override public boolean equals(Object o) {
			if(o instanceof LambdaSQE) {
				LambdaSQE that = (LambdaSQE)o;
				return super.equals(o) && Objects.equal(argNames, that.argNames);
			}
			return false;
		}
		
		@Override public String toString() {
			return "LambdaSQE[" + getName() + " args=" + Joiner.on(",").join(argNames) + "]";
		}
		
		@Override public String getDocName() {
			if(argNames.size()>2 || !argNames.contains("x")) {
				return getFullName() + "[" + Joiner.on(";").join(argNames) + "]";
			}
			return getFullName();
		}
		
		@Override public String getHtmlDoc(boolean shortFormat) {
			return toHtml(ImmutableMap.of("Name: ", getDocName(), 
					"Arguments: ", HtmlUtils.toList(argNames)));
		}
	}

	
}