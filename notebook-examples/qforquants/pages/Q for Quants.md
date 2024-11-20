# Q for Quants by Nick Psaris

Nick has produced a goldmine of kdb+ material including [Q for Quants](https://nick.psaris.com/presentation/q-for-quants/).
Nick has kindly granted permission to reproduce this section, see [Q for Quants](https://nick.psaris.com/presentation/q-for-quants/) for the full tutorial.

In it Nick covers:

 - loading CSV/fixed-width datasets into Q
 - list/differentiate each Q datatype
 - The Four Q attributes and explain when to use each
 - Performing simple q-SQL queries with aggregation
 - Saving datasets to disk

The data file can be downloaded from [here](https://www.cmegroup.com/market-data/datamine-historical-data/files/2012-11-05-e-mini-s-p-futures.csv).


## Loading the CSV

```sql showcodeonly
t:(" VI   MI FCC         D ";1#",") 0: `$"2012-11-05-e-mini-s-p-futures.csv";
```

```sql server='kdbserver'
-100 sublist (" VI   MI FCC         D ";1#",") 0: `$"2012-11-05-e-mini-s-p-futures.csv"
```

 - The 0: operator is the dyadic version of the monadic read0
 - The 0: operator can be supplied with a list of strings or the file itself
 - It allows us to supply the types: " VI MI FCC D "
 - and the delimiter: "," (1#"," treats the first row as column headers)
 - Ignored columns are indicated by the space characters

## Renaming Columns

```sql showcodeonly
q)t:`time`seq`expiry`qty`px`side`ind`date xcol t
q)t
time     seq expiry  qty px      side ind date
----------------------------------------------------
16:02:57 11  2012.12 0   1405.75      I   2012.11.04
16:04:23 12  2012.12 0   1405.5       I   2012.11.04
16:22:24 29  2012.12 0   1406.75      I   2012.11.04
16:22:41 30  2012.12 0   1406.5       I   2012.11.04
16:22:50 35  2012.12 0   1405.75      I   2012.11.04
```

## Using q-SQL

```sql showcodeonly
q)trade:select `p#expiry,seq,time+date,tp:px,ts:qty from t where null side, null ind
q)trade
```

```sql server='kdbserver'
t:(" VI   MI FCC         D ";1#",") 0: `$"2012-11-05-e-mini-s-p-futures.csv";
t:`time`seq`expiry`qty`px`side`ind`date xcol t;
trade:select `p#expiry,seq,time+date,tp:px,ts:qty from t where null side, null ind;
-50 sublist trade
```
## Time-series Graph 

```sql showcodeonly
select time,tp from trade
```
```sql server='kdbserver' type='timeseries'
`time xdesc select last tp by 0D00:01 xbar time from trade
```

# Aggregations

- The xbar operator rounds data down to the nearest specified unit
- The by q-SQL clause groups the data before operations are performed on each group

```sql showcodeonly
q)ohlc:select o:first tp,h:max tp,l:min tp,c:last tp by expiry,0D00:01 xbar time from trade
q)ohlc

```
```sql server='kdbserver' type='candle'
ohlc:select open:first tp,high:max tp,low:min tp,close:last tp by 0D00:10 xbar time from trade;
ohlc
```

To continue this tutorial see: https://nick.psaris.com/presentation/q-for-quants

