package com.timestored.qstudio.qdoc;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import com.timestored.connections.JdbcIcons;
import com.timestored.connections.ServerConfig;
import com.timestored.qdoc.DocCompleter;
import com.timestored.qdoc.DocumentedEntity;
import com.timestored.qstudio.Language;
import com.timestored.qstudio.QStudioModel;
import com.timestored.qstudio.model.ServerQEntity;
import com.timestored.qstudio.model.ServerQEntity.QQuery;
import com.timestored.sqldash.theme.DBIcons;
import com.timestored.theme.Icon;
import com.timestored.theme.Theme;
import com.timestored.theme.Theme.CIcon;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContextualDocCompleter implements DocCompleter {
	private final @NonNull QStudioModel qStudioModel;

	@Override
	public List<DocumentedEntity> findByPrefix(String txt, int caratPos) {
		List<DocumentedEntity> l = new ArrayList<>();
		if(caratPos <= txt.length()) {
			String fileEnding = qStudioModel.getOpenDocumentsModel().getSelectedDocument().getFileEnding();
			Language language = Language.getLanguage(fileEnding);
			String pre = txt.substring(0, caratPos);
			
			if(language.equals(Language.MARKDOWN)) {
				int p = pre.lastIndexOf('\n');
				String afterNL = (p >= 0 ? pre.substring(p) : pre).trim();
				int q = pre.lastIndexOf(' ');
				String afterSpace = (q >= 0 ? pre.substring(q) : pre).trim();
				
				if(afterNL.equals("`") || afterNL.equals("``") || afterNL.equals("```")) {
					for(ServerQEntity sqe : qStudioModel.getAdminModel().getAllVariables()) {
						if(sqe.isTable() && sqe.getQQueries().size()>0) {
							String srvr = qStudioModel.getQueryManager().getSelectedServerName();
							QQuery qqry = sqe.getQQueries().get(0);
							String code = "```sql type='grid' server='" + srvr + "' \n" + qqry.getQuery() + "\n```\n";
							l.add(new FakeDocumentedEntity(code.substring(afterNL.length()), Theme.CIcon.MARKDOWN_GREY));
						}
					}
				} else if(afterSpace.equals("type='") || afterSpace.equals("type=\"")) {
					for(int i=0; i < pulseChartTypes.length; i++) {
						String code = afterSpace + pulseChartTypes[i];
						l.add(new FakeDocumentedEntity(code.substring(afterSpace.length()), pulseChartIcons[i]));
					}
				} else if(afterSpace.equals("server='") || afterSpace.equals("server=\"")) {
					for(ServerConfig sc : qStudioModel.getConnectionManager().getServerConnections()) {
						String code = afterSpace + sc.getName();
						JdbcIcons icon = JdbcIcons.getIconFor(sc.getJdbcType());
						l.add(new FakeDocumentedEntity(code.substring(afterSpace.length()), icon));
					}
				}
			}
			if(pre.toUpperCase().endsWith(" FROM ")) {
				for(ServerQEntity sqe : qStudioModel.getAdminModel().getAllVariables()) {
					if(sqe.isTable()) {
						String code = sqe.getFullName();
						l.add(new FakeDocumentedEntity(code, Theme.CIcon.TABLE_ELEMENT));
					}
				}
			}
		}
		return l;
	}

	private static final String[] pulseChartTypes = new String[] {"grid", "timeseries", "area", 
			"line", "bar", "stack", "bar_horizontal", "stack_horizontal", "pie",
		    "scatter", "bubble", "candle", "depthmap", "radar", "treemap", 
		    "heatmap", "calendar", "boxplot", "3dsurface", "3dbar",
		    "sunburst", "tree", "metrics", "sankey"};
	
	private static final com.timestored.theme.Icon[] pulseChartIcons = new Icon[] { CIcon.TABLE_ELEMENT, CIcon.CHART_CURVE, DBIcons.CHART_AREA, 
			DBIcons.CHART_LINE, DBIcons.CHART_BAR, DBIcons.CHART_BAR, DBIcons.CHART_BAR, DBIcons.CHART_BAR,DBIcons.CHART_PIE, 
			DBIcons.CHART_SCATTER_PLOT, DBIcons.CHART_BUBBLE, DBIcons.CHART_CANDLESTICK, null, null, null, 
			DBIcons.CHART_HEATMAP, null, null, null, null, 
			null, null, null, null };
	
	@Data
	private static class FakeDocumentedEntity implements DocumentedEntity {
		private final String fullName;
		private final Icon icon;
		
		private String source = "Server";
		private SourceType sourceType = DocumentedEntity.SourceType.SERVER;
		
		@Override public String getDocName() { return fullName; }
		@Override public String getHtmlDoc(boolean shortFormat) { return fullName; }
		@Override public ImageIcon getIcon() { return icon == null ? Theme.CIcon.MARKDOWN_GREY.get16() : icon.get16(); }
		
	}
}
