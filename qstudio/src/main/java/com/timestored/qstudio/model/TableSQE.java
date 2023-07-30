package com.timestored.qstudio.model;

import static com.timestored.cstore.CAtomTypes.DICTIONARY;
import static com.timestored.cstore.CAtomTypes.TABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.timestored.cstore.CAtomTypes;
import com.timestored.misc.HtmlUtils;
import com.timestored.theme.Theme.CIcon;


/** {@link ServerQEntity} for tables only */
public class TableSQE extends BaseSQE {

	private final long count;
	private final boolean isPartitioned;
	private final List<String> colNames;

	/**
	 * 
	 * @param count Count if known or -1 to specify unknown.
	 */
	TableSQE(String serverName, String namespace, String name, CAtomTypes type, 
			long count, boolean isPartitioned, String[] colNames) {
		
		super(serverName, namespace, name, type);
		Preconditions.checkArgument(count>=-2);
		Preconditions.checkNotNull(colNames);
		Preconditions.checkArgument(colNames.length>0);
		CAtomTypes t = getType();
		Preconditions.checkArgument(t.equals(TABLE) || t.equals(DICTIONARY));
		
		this.count = count;
		this.isPartitioned = isPartitioned;
		this.colNames = Arrays.asList(colNames);
	}

	@Override public long getCount() {
		return count;
	}

	@Override public boolean isTable() {
		return true;
	}
	
	@Override public ImageIcon getIcon() {
		// The tree query returns count -2 when the count fails
		// This can be to moving from the HDB root directory etc.
		return count == -2 ? CIcon.TABLE_DELETE.get16() : CIcon.TABLE_ELEMENT.get16();
	}

	@Override public String toString() {
		return "TableSQE[" + getName() + " count=" + count 
				+ " cols=" + Joiner.on(",").join(colNames) + "]";
	}
	
	
	public List<String> getColNames() {
		return colNames;
	}
	
	
	public boolean isPartitioned() {
		return isPartitioned;
	}
	
	@Override public boolean equals(Object o) {
		if(o instanceof TableSQE) {
			TableSQE that = (TableSQE)o;
			return super.equals(o) && 
				Objects.equal(colNames, that.colNames) && 
				Objects.equal(isPartitioned, that.isPartitioned) && 
				Objects.equal(count, that.count);
		}
		return false;
	}

	@Override public String getHtmlDoc(boolean shortFormat) {
		
		String t = (getType().equals(DICTIONARY) ? "Keyed " : "") + "Table";
		
		// If there are many columns, do comma separated rather than list.
		// Otherwise its too long for the screen
		String colHtml = getColumnHtml(colNames, shortFormat || colNames.size() > 9);
		
		String s = "";
		if(!shortFormat) {
			Map<String,String> namesToDescs = ImmutableMap.of("Name: ", getDocName(), 
					"Type: ", t,
					"Count: ", count==-1 ? "unknown" : ""+count,
					"Partitioned: ", (isPartitioned ? "Yes" : "No"),
					"Columns: ", colHtml);
			s = toHtml(namesToDescs);
		} else {
			s = HtmlUtils.START + " " + t + " ";
			if(count != -1) {
				s += count + " rows ";
			}
			s += (isPartitioned ? "Partitioned" : "") + "<br />Columns: " + colHtml + HtmlUtils.END;
		}
		return s;
	}

	private static String getColumnHtml(List<String> colNames, boolean shortFormat) {
		String colHtml;
		if(shortFormat) {
			StringBuilder sb = new StringBuilder(colNames.size() * 8);
			for(int i=0; i<colNames.size()-1; i++) {
				sb.append(colNames.get(i)).append(", ");
				if(5 == i % 6) {
					sb.append("<br />");
				}
				if(i>23) {
					sb.append(".... ");
					break;
				}
			}
			sb.append(colNames.get(colNames.size()-1));
			colHtml = sb.toString();
		} else {
			colHtml = HtmlUtils.toList(colNames);
		}
		return colHtml;
	}
	
	@Override public List<QQuery> getQQueries() {
		List<QQuery> r = new ArrayList<ServerQEntity.QQuery>();

		String qry;
		String fn = getFullName();
		
		// select top 100
		if(isPartitioned) {
			qry = ".Q.ind["+fn+"; `long$til 100]";
		} else {
			qry = "select[100] from "+fn;
		}
		r.add(new QQuery("Select Top 100", CIcon.TABLE_ELEMENT, qry));

		// select bottom 100
		r.add(new QQuery("Select Bottom 100", CIcon.TABLE_ELEMENT, 
				getBottom100query(fn, false)));

		// select bottom 100 NAMED
		r.add(new QQuery("Select Col1,Col2... from Bottom 100", 
				CIcon.TABLE_ELEMENT, getBottom100query(fn,true)));
	
		r.addAll(super.getQQueries());
		return r;		
	}
	
	private String getBottom100query(String fullname, boolean includeColumnNames) {
		String cols = "";
		if (includeColumnNames && colNames != null) {
			cols = Joiner.on(',').join(colNames);
		}
		if (isPartitioned) {
			return "select " + cols + " from .Q.ind[" + fullname 
					+ "; `long$-100 + (count " + fullname + ")+til 100]";
		} else {
			return "select[-100] " + cols + " from " + fullname;
		}
	}
}
