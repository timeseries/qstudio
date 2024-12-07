---
server: localhost:5000
---

# Asof AJ WJ Time Series Joins

Original Source: https://www.timestored.com/kdb-guides/asof-time-joins-aj-wj

Kdb has timeseries data specific joins that provide powerful tools for analysing tick data in particular. 
Due to kdb being column-oriented and based on ordered lists, the syntax is usually much more concise and the speed much faster than standard sql databases. 



# Asof Time Join

We will use the following simplified trade-t and quote-q tables to demonstrate the various joins.

```
t:([] 
    time:07:00 08:30 09:59 10:00 12:00 16:00t; 
    sym:`a`a`a`a`b`a; 
    price:0.9 1.5 1.9 2 9. 10.; 
    size:100*6?10);
t
```

```
q:([] 
    time:08:00+`time$60*60000*til 8; 
    sym:`a`b`a`b`b`a`b`a;
    bid:1 9 2 8 8.5 3 7 4.);
q
```

# AJ 

- **AJ** aj[ cols; sourceTable; lookupTable]
- **AJ0** aj0[ cols; sourceTable; lookupTable]

For each row in the source table lookup a matching value in the lookup table, by matching on the columns specified in cols. cols is a list of column names where the initial columns MUST match exactly and the last column matches the closest value LESS-THAN in the source table.

 - ``sourceTable`` - The table whos items you want to try and find close matches for, the result will have the same number of rows as this table.
 - ``lookupTable`` - The table used for finding matching data to join, the size and schema of this table will strongly affect the speed.
 - ``cols`` -  A list of columns to use for joining on. The initial columns excluding the last will be matched exactly. The last column matches if an entry less-than is found.


```
q)aj[`sym`time; t; q]
```

Adding some columns makes it clearer which time columns are which:
```
fq:update  qtime:time,qsym:sym from q;
ft:update ftime:time, fsym:sym from t;
aj[`sym`time; ft; fq]
```

# aj0

AJ0 is the exact same as aj but returns the lookup tables time column.

```
q)aj[`sym`time; ft; fq]
```

```
q)aj0[`sym`time; ft; fq]
```

# asof

Asof is a built-in kdb function, that provides a limited version of AJ, you may find it used occasionally. 

```
t,'q asof `sym`time#t
```

# Union Join

An alternative method of viewing time-series data for examing sequential events between tables, is using the union join uj to get a combined table then sorting the full table on time.

```
q)`time xasc q uj t
```

# Running AJ on large tables

```sql showcodeonly
q)\l trades.q
(+`date`sym!(2013.09.27 2013.09.28 2013.09.29 2013.09.30 2013.10.01;`RBS`RBS`RBS`RBS`..
q)trade:100?trade
q)count each (trade;quote)
100 1700000
q)meta quote
c    | t f a
-----| -----
date | d   s
time | t
sym  | s
size | i
cond | c
bid  | f
ask  | f
asize| j
bsize| j
q)\t r1:aj[`sym`time; trade; quote]
681
q)\t update `g#sym from `quote
46
q)meta quote
c    | t f a
-----| -----
date | d   s
time | t
sym  | s   g
size | i
cond | c
bid  | f
ask  | f
asize| j
bsize| j
q)\t r2:aj[`sym`time; trade; quote]
0
q)r1~r2
1b
q)
```

Running time-series joins such as AJ on large amounts of data takes a significant amount of time. 
By applying a grouped attribute to the sym column we reduced the time from over half a second to under a tenth of a second. 
You must be careful running aj/wj's, particularly against on-disk data, it is recommended that you consult the documentation on 
code kx or consult an experienced kdb programmer if you have any issues.


# Time Window Join

We will use the following simplified trade-t and quote-q tables to demonstrate the various time window joins.

```
tt:([] 
    time:  09:00 09:04 09:12 09:13t; 
    sym:   `a`a`a`a; 
    price: 10 11 12 13.);
tt
```

```
qq:([] 
    time: 09:00+`time$60000*til 13; 
    sym: `a`a`a`a`a`b`b`b`a`a`a`a`a;
    bid: asc 9.+13?10);
qq
```
     
# wj     


- **WJ**      wj[ windows; cols; sourceTab; (lookupTab;(agg0;col0);(agg1;col1)]
- **WJ1**      wj1[ windows; cols; sourceTab; (lookupTab;(agg0;col0);(agg1;col1)]

For each row in the sourcetable, a time window pair is specified, matches on cols are then found and those that occur within the time window have the aggregate functions applied to the selected columns.

 - ``sourceTable`` - The table whos items you want to try and find close matches for, the result will have the same number of rows as this table.
 - ``lookupTable`` - The table used for finding matching data to join
 - ``cols`` A list of columns to use for joining on. The initial columns excluding the last will be matched exactly. The last column will match within the specified windows.

```sql type="table"
windows:flip tt.time +\: -00:02 00:02t;
windows
```

```
wj[windows; `sym`time; tt; (qq; (::; `bid))]
```
```
wj[windows; `sym`time; tt; (qq; (avg; `bid))]
```

# WJ1

The only difference between wj1 and wj, the difference is that where wj pulls in prevailing values not within the time window, wj1 strictly excludes values outside the interval.

```
win2:(08:58:00.000 09:02:00.000 09:10:00.000 10:10:00.00; 09:02:00.000 09:06:00.000 09:14:00.000 10:15:00.0);
wj[win2; `sym`time; tt; (qq; (::; `bid))]
```

### See Also:

 - [Setting up a Kdb Development Environment](https://www.timestored.com/kdb-guides/developer-environment) - installation, linux/windows tools
 - [Commonly encountered kdb limits](https://www.timestored.com/kdb-guides/kdb-database-limits) - rank branch constant errors
 
 
 
 