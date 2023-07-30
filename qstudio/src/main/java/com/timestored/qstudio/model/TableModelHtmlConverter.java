package com.timestored.qstudio.model;

import javax.swing.table.TableModel;

import com.google.common.base.Joiner;
import com.timestored.misc.HtmlUtils;

/** * Allows converting a table model to html */
public class TableModelHtmlConverter {
	
	private IndentingAppender sb = new IndentingAppender();
	
	private TableModelHtmlConverter() { }

	private IndentingAppender wrap(String tag, String inner) { 
		return sb.b("<" + tag + ">").b(HtmlUtils.escapeHTML(inner)).b("</" + tag + ">");
	}
	
	private String convertTable(TableModel tm) {

		sb.start("<table>");
		sb.a("<tr>");
		for(int c=0; c<tm.getColumnCount(); c++) {
			wrap("th", tm.getColumnName(c));
		}
		sb.b("</tr>");
		for(int r=0; r<tm.getRowCount(); r++) {
			sb.start("<tr>");
			for(int c=0; c<tm.getColumnCount(); c++) {
				sb.a();
				Object o = tm.getValueAt(r, c);
				String s = "" + o.toString();
				if(o instanceof String) {
					s = (String) o;
				} else if(o instanceof char[]) {
					s = new String((char[])o);
				} else if(o instanceof String[]) {
					String[] v = (String[])o;
					s = "`" + Joiner.on('`').join(v);
				} else {
					
				}
				wrap("td", s);
			}
			sb.end("</tr>");
		}
		sb.end("</table>");
		return sb.toString();
	}
	
	/** convert table model to html **/
	public static String convert(TableModel tableModel) {
		return new TableModelHtmlConverter().convertTable(tableModel);
	}
	
	
	private static class IndentingAppender {
		private String indent = "";
		private StringBuilder sb = new StringBuilder();

		IndentingAppender a() { return a("");}
		IndentingAppender a(String s) {
			return b("\r\n").b(indent).b(s);
		}
		IndentingAppender b(String s) {
			sb.append(s);
			return this;
		}

		IndentingAppender start(String s) { return a(s).increaseIndent(); };
		IndentingAppender end(String s) { return decreaseIndent().a(s); };
		private IndentingAppender increaseIndent() { indent += "\t"; return this;}
		private IndentingAppender decreaseIndent() { indent = indent.substring(1); return this; }
		@Override public String toString() { return sb.toString(); }
	}
}
