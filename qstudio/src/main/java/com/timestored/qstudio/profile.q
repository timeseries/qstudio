system "d .prof";

/ Module for profiling q code either by line or by function
/ Author: Ryan Hamilton

// @TODO allow specifying number of times to run profiler so...
// @TODO allow running profiler defined time period
// @TODO get working for functions
// @TODO write unit tests


/ Break one query into it's separate statements
/ @param qry is a self-complete q query string
/ @return list of strings that compose separate statements
separateStatements:{ [qry]
    c:";",qry; / c=code, ; adding ; simplifies processing
    i:count c;
    s:(); / separators
    // starting from end, find ; separators, excluding nested statements
    while[i>=0;
        $[";"~a:c i; s,:i; / mark separator position else skip over quotes
            "]"~a; while[i&not c[i-:1]~"["];  // blocks and args
            ")"~a; while[i&not c[i-:1]~"("];  // for tables
            "\""~a; while[i&not c[i-:1]~"\""]];
        i-:1];
     // remove semi-colons and trim
     trim each 1_'asc[0N!s] _ c};
     
     
     
//*****************      PUBLIC      *************************/  
        

/ Same as profile but more reporting columns returned
profileFull:{ [qry] 
    / timerCode, add dict to a including cur time
    tc:"; a,:enlist @[.Q.w[];`time;:;`long$.z.t];\n"; 
    stList:{(-1_x),enlist "r:",last x} st:.prof.separateStatements qry;
    b:raze tc,/:stList; / statements with time code between
    res:value "a:()",b,";",tc," (r;a)";
    / tidy result of profiling into table format
    report:update statement:(enlist[""],st) from res 1;
    (res 0; `heap`peak`wmax`mphy _ report)};


/ Profile how long and how much memory each statement within qry takes
/ note the statements cannot make use of the a/r variable as used for profiling
/ @param qry is a self-complete q query string
/ @return two item list, 1 - result of query, 2 - full profile report
profile:{ [qry]
    a:.prof.profileFull qry;
    f:{100*{0^x%max x}{x-prev x} x}; / find relative change
    // -10's are to ignore small changes
    (a 0; 1_ select statement, space:f used,f time from a 1)};

system "d .";
