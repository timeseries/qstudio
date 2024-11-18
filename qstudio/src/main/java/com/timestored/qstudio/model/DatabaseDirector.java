package com.timestored.qstudio.model;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPopupMenu;

import com.google.common.base.Preconditions;
import com.timestored.connections.JdbcTypes;

public class DatabaseDirector {

	private static Map<JdbcTypes,ActionsGeneratorSupplier> dbtypeToSuppliers = new HashMap<>();
	private static final ActionsGenerator EMPTY = new EmptyActionsGenerator();
	
	public static void registerActionsGenerator(JdbcTypes jdbcType, ActionsGeneratorSupplier actionsGeneratorSupplier) {
		Preconditions.checkNotNull(jdbcType);
		dbtypeToSuppliers.put(jdbcType, actionsGeneratorSupplier);
	}

	public static ActionsGenerator getActionsGenerator(QueryManager queryManager, AdminModel adminModel, ServerModel sm) {
		ActionsGeneratorSupplier supp = dbtypeToSuppliers.get(sm.getServerConfig().getJdbcType());
		if(supp != null) {
			return supp.getActionsGenerator(queryManager, adminModel, sm);
		}
		return EMPTY;
	}

	public static interface ActionsGeneratorSupplier {
		ActionsGenerator getActionsGenerator(QueryManager queryManager, AdminModel adminModel, ServerModel sm);
	}

	public static interface ActionsGenerator {
		void addColumnMenuItems(JPopupMenu menu, TableSQE table,  String column, boolean partitionColumn);
	}

	private static class EmptyActionsGenerator implements ActionsGenerator {
		public void addColumnMenuItems(JPopupMenu menu, TableSQE table, String column, boolean partitionColumn) {}
	}
}
