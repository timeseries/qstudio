package com.timestored.kdb;

import java.awt.Component;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JScrollPane;

import com.timestored.TimeStored;
import com.timestored.connections.JdbcTypes;
import com.timestored.qstudio.UpdateHelper;
import com.timestored.theme.Theme;

import kx.c.KException;

/**  KDB errors, descriptions and examples. */
@SuppressWarnings("javadoc")
public enum KError {
	
	Mlim("Mlim","","more than 999 nested columns in splayed tables"),
	Q7("Q7","","nyi op on file nested array"),
	XXX("XXX","","value error (XXX undefined)"),
	BRACKET("[{(\"\")}]","","open brackets or speech marks"),
	arch("arch","`:test set til 100;-17!`:test","attempt to load file of wrong endian format"),
	access("access","","attempt to read files above directory; run system commands or failed usr/pwd"),
	accp("accp","","tried to accept an incoming tcp/ip connection but failed to do so"),
	assign("assign","cos:12","attempt to reuse a reserved word"),
	badtail("badtail","","incomplete transaction at end of file  get good (count;length) with -11!(-2;`:file)"),
	branch("branch","","a branch(if;do;while;$.;.;.) more than 255 byte codes away"),
	cast("cast","s:`a`b; c:`s$`a`e",""),
	Char("char","","invalid character"),
	conn("conn","","too many incoming connections (1022 max)"),
	constants("constants","","too many constants (max 96)"),
	core("core","","too many cores for license"),
	cpu("cpu","","too many cpus for license"),
	domain("domain","1?`10","out of domain"),
	exp("exp","","expiry date passed"),
	from("from","select price trade","Badly formed select statement"),
	glim("glim","","g# limit  kdb+ currently limited to 99 concurrent g#'s"),
	globals("globals","","too many global variables (31 max)"),
	host("host","","unlicensed host"),
	k4Lic("k4.lic","","k4.lic file not found  check QHOME/QLIC"),
	length("length","()+til 1","incompatible lengths"),
	limit("limit","0W#2","tried to generate a list longer than 2 000 000 000"),
	locals("locals","","too many local variables (23 max)"),
	loop("loop","a::a","dependency loop"),
	mismatch("mismatch","","columns that can't be aligned for R;R or K;K"),
	mq("mq","","Multi-threading not allowed."),
	noamend("noamend","t:([]a:1 2 3); enum:`a`b`c;","Cannot perform global amend from within an amend."),
	noupdate("noupdate","","update not allowed when using negative port number"),
	nyi("nyi","","not yet implemented"),
	os("os","","Operating System error OR wrong os (if licence error)"),
	par("par","","You are trying to select from a partitioned table without and constraint on the partitioned column."),
	params("params","f:{[a;b;c;d;e;f;g;h;e]}","too many parameters (8 max)"),
	parse("parse","","invalid syntax"),
	part("part","","something wrong with the partitions in the hdb"),
	pl("pl","","peach can't handle parallel lambda's (2.3 only)"),
	rank("rank","+[2;3;4]","invalid rank or valence"),
	sFail("s-fail","`s#3 2","invalid attempt to set sorted attribute"),
	splay("splay","","nyi op on splayed table"),
	srv("srv","","attempt to use client-only license in server mode"),
	stack("stack","{.z.s[]}[]","ran out of stack space"),
	stop("stop","","user interrupt(ctrl-c) or time limit (-T)"),
	stype("stype","'42","invalid type used to signal"),
	type("type","key 2.2","wrong type"),
	uFail("u-fail","`u#2 2","invalid attempt to set unique attribute"),
	upd("upd","","attempt to use version of kdb+ more recent than update date"),
	user("user","","unlicensed user"),
	unmappable("unmappable","t:([]sym:`a`b;a:(();()));"," c  c  "),
	value("value","","no value"),
	vd1("vd1","","attempted multithread update"),
	view("view","","Trying to re-assign a view to something else"),
	wha("wha","","invalid system date (release date is after system date)"),
	wsfull("wsfull","","malloc failed. ran out of swap (or addressability on 32bit). or hit -w limit."),
	// See replyformat in QueryManager.
	replyformat("replyformat","","Result returned from server was unexepected format. <br />Is it a Torq gateway or use deferred Response? Try turning off Query Wrapping. <br />See https://www.timestored.com/qstudio/help/issues/qstudio-deferred-response-error");

	private String example;
	private String desc;
	private String sym;
	
	private static Map<String, KError> lookup = new HashMap<String, KError>();
	
    static {
          for(KError e : EnumSet.allOf(KError.class)) {
			lookup.put(e.getSym(), e);
		}
     }
    
	/** @return Example of how to cause this error. **/
	public String getExample() {
		return example;
	}
	

	/** @return Description of what various states of this command mean. **/
	public String getDesc() {
		return desc;
	}
	
	/** symbol returned by q when this error occurs **/
	public String getSym() {
		return sym;
	}
	
	KError(String sym, String args, String desc) {

		this.example = args;
		this.desc = desc;
		this.sym = sym;
	}


	/**
	 * @param error sym as returned by console
	 * @return details for a given error or null if no details known.
	 */
	public static KError get(String error) {
		return lookup.get(error);
	}
	

	/** @return A component that describes the kdb error and what may cause it. */
	public static Component getDescriptionComponent(KException ke) {
		// display error / description
		KError kErr = KError.get(ke.getTitle());
		Box box = Theme.getErrorBox("ERROR: " + ke.getMessage());

		String sm = ke.getStackMessage();
		if(sm.length() > 0) {
			box.add(Theme.getTextArea("stackMessageTA", sm));
		}
		
		
		if (kErr != null) {
			// recognised then show more detail
			String html = "<html>" + kErr.getDesc();
			if (kErr.getExample().length() > 0) {
				html += "<br /><br />Example of how this could be caused: " + kErr.getExample();
			}
			html += "<br /><br />" + TimeStored.getRandomNewsLink();
			
			box.add(Theme.getHtmlText(html));
		}
		
		box.add(UpdateHelper.getNewsPanel(JdbcTypes.KDB));
		return new JScrollPane(box);
	}

}
