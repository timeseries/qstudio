package com.timestored.kdb;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for system commands within KDB.
 */
public enum SysCommand {
	
	//TODO convert this to extending BuiltinDocumentedEntities and make docSource.
	
	b("b","","block client write access to a kdb+ database"),
	f("f","","this is either the script to load (*.q, *.k, *.s), or a file or directory"),
	c("c","rows cols", "Console Height Width", 
			"console maxRows maxCols, default 25 80. This is the maximum " +
			"display size of any single terminal output."),
	C("C","rows cols", "Web Height Width", "webserver maxRows maxCols, " +
			"default 36 2000. This is the maximum display size of any table" +
			" shown through the web server."),
	e("e","B", "Error Trap", "Boolean flag that if true causes the server to break when an error occurs including on client requests."),
	g("g","Mode", "GC Mode", "Switch garbage collection between immediate 1 and deferred 0 modes."),
	l("l","","log updates to filesystem"),
	L("L","","sync log updates to filesystem"),
	o("o","N", "GMT Offset", "offset N hours from GMT (affects .z.Z,.z.T)"),
	p("p","Port", "Port", "port which KDB server listens on (if -Port used, then server is multithreaded)"),
	P("P", "FP Precision", "Precision", "Display precision for floating point number. (default 7, use 0 to display all available)"),
	q("q","","Quiet, ie. No startup, baber text or session prompts (typically used where no console required)"),
	r("r",":H:P","replicate from Host/Port (seems to rely on log and running on same machine)"),
	s("s","N", "Slave Threads", "start N slaves for parallel execution", false),
	t("t","milliseconds", "Timer", "timer in N milliseconds between timer ticks. (default is 0 = no timeout)"),
	T("T","seconds", "Timeout", "timeout in seconds for client queries, i.e. maximum time a client call will execute. Default is 0, for no timeout."),
	u("u","passwdFile","usr:password file to protect access. File access restricted to inside start directory"),
	U("U","passwdFile","usr:password file to protect access. File access unrestricted"),
	w("w","MB","workspace MB limit (default:2*RAM)"),
	W("W","weekOffset","Week Offset", "offset from Saturday, default is 2, meaning Monday is start of week"),
	z("z","B","Date Mode", "format used for `date$ date parsing. 0 is mm/dd/yyyy (default) and 1 is dd/mm/yyyy.");

	private final String command;
	private final String longDesc;
	private final String args;
	private final boolean writable;
	private final String shortDesc;

	private static Map<String, SysCommand> lookup = new HashMap<String, SysCommand>();
	
    static {
          for(SysCommand s : EnumSet.allOf(SysCommand.class)) {
			lookup.put(s.getCommand(), s);
		}
     }
    
	/** @return textual description of arguments to this system command. **/
	public String getArgs() {
		return args;
	}
	

	/** @return Description of what various states of this command mean. **/
	public String getLongDesc() {
		return longDesc;
	}
	
	public String getCommand() {
		return command;
	}
	
	SysCommand(String command, String args, String desc) {
		this(command, args, desc, desc, true);
	}
	
	SysCommand(String command, String args, String desc, boolean writable) {
		this(command, args, desc, desc, writable);
	}

	public String getShortDesc() {
		return shortDesc;
	}
	
	SysCommand(String command, String args, String shortDesc, 
			String longDesc) {
		this(command, args, shortDesc, longDesc, true);
	}
	/** @return true if this can be set by using a slash command from within q **/
	public boolean isWritable() {
		return writable;
	}

	SysCommand(String command, String args, String shortDesc, 
			String longDesc, boolean writable) {

		this.args = args;
		this.shortDesc = shortDesc;
		this.longDesc = longDesc;
		this.command = command;
		this.writable = writable;
	}
	
	


	public String getUrl() {
		return "http://code.kx.com/wiki/Reference/SystemCommands#System_Commands";
	}
	

	/**
	 * @param systemCommand the one letter system command whos details are wanted.
	 * @return details for a given system command or null if no details known.
	 */
	public static SysCommand get(String systemCommand) {
		return lookup.get(systemCommand);
	}
}


