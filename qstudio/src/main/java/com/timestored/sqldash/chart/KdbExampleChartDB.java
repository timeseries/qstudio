package com.timestored.sqldash.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.timestored.connections.JdbcTypes;
import com.timestored.sqldash.exampledb.ExampleChartDB;
import com.timestored.sqldash.exampledb.ExampleChartQuery;

/*
 * Combines the single stand alone kdb chart examples for each view strategy 
 * into an {@link ExampleChartDB}.
 */
public class KdbExampleChartDB implements ExampleChartDB {

	private static ExampleChartDB INSTANCE = new KdbExampleChartDB();
	
	private KdbExampleChartDB() {}
	public static ExampleChartDB getInstance() { return INSTANCE; }
	
	@Override public String getName() {
		return "Self-Contained Queries";
	}

	@Override public String getDescription() {
		return "Database where no initialisation is needed and each chart example is self-cointained.";
	}

	@Override public List<String> getInitSQL(boolean withComments) { return Collections.emptyList(); }

	@Override public JdbcTypes getDbType() { return JdbcTypes.KDB; }

	@Override public List<ExampleChartQuery> getQueries() {
		List<ExampleChartQuery> l = new ArrayList<ExampleChartQuery>();
		for(ViewStrategy vs : ViewStrategyFactory.getStrategies()) {
			for(ExampleView ev : vs.getExamples()) {
				l.add(new ExampleWrapper(ev, vs));
			}
		}
		
		return l;
	}

	private static class ExampleWrapper implements ExampleChartQuery {

		private final ExampleView ev;
		private final ViewStrategy vs;

		ExampleWrapper(ExampleView exampleView, ViewStrategy viewStrategy) {
			this.ev = Preconditions.checkNotNull(exampleView);
			this.vs = Preconditions.checkNotNull(viewStrategy);
		}
		
		@Override public String getName() { return ev.getName(); }

		@Override public String getDescription() { return ev.getDescription(); }

		@Override public String getSqlQuery() { return ev.getTestCase().getKdbQuery(); }

		@Override public ViewStrategy getSupportedViewStrategy() { return vs; }
		
	}
}
