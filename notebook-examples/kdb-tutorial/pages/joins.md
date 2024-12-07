---
server: localhost:5000
---

# kdb+ Joins

Sql joins allow pulling corresponding data from one table, onto another to give a combined result table with columns from both. 
Most kdb joins rely on column names corresponding (rather than standard sql's more verbose explicit naming). 

# Example Tables

```sql showcodeonly
stock:([sym:`s#`AAPL`C`FB`MS] 
    sector:`Tech`Financial`Tech`Financial; 
    employees:72800 262000 4331 57726);

trades:([] dt:`s#2015.01.01+0 1 2 3 3 4 5 6 6; 
    sym:`C`C`MS`C`DBK`AAPL`AAPL`MS`MS; 
    price:10 10.5 260 11 35.6 1010 1020 255 254; 
    size:10 100 15 200 55 20 300 200 400);
    
fbTrades:([] dt:`s#2015.01.01+1 2 4; sym:`FB; size:1000; book:`A`B`A);
```

## stock

```
stock:([sym:`s#`AAPL`C`FB`MS] 
    sector:`Tech`Financial`Tech`Financial; 
    employees:72800 262000 4331 57726);
stock
```

## trades
```
trades:([] dt:`s#2015.01.01+0 1 2 3 3 4 5 6 6; 
    sym:`C`C`MS`C`DBK`AAPL`AAPL`MS`MS; 
    price:10 10.5 260 11 35.6 1010 1020 255 254; 
    size:10 100 15 200 55 20 300 200 400);
trades
```

## fbTrades

```
fbTrades:([] dt:`s#2015.01.01+1 2 4; sym:`FB; size:1000; book:`A`B`A);
fbTrades
```




# lj - Left Join

The format of lj is: ``t lj kt`` where t is your source table and kt is your lookup table that MUST be keyed. 
**Lj - left join - means for each row in table t, try to look up corresponding values in keyed-table kt, 
where there is no match use nulls**. The columns used for mathing are the key columns of kt. i.e. 
The key columns of kt, must appear in t and their column names MUST match exactly.

Note:
 - Where a lookup table contains non-key columns with the same name as existing columns, the matched columns overwrite the original value.
 - Where multiple matches are possible (duplicate keys in keyed table), the first match is always taken.
 

```
trades lj stock
```


# pj - Plus Join

``t pj kt`` - Same principle as lj, but existing values are added to where column names match.

**Pj - plus join - means for each row in table t, try to look up corresponding values in keyed-table kt, where there are matching numeric columns add their values.**

```
stock pj ([sym:`FB`C] employees:100000 -260000)
```

# ij - Inner Join

``t ij kt`` - **Where matches occur between t and kt on primary key columns, update or add that column.**
Non-matches are not returned in the result. The columns used for matching are the key columns of kt. 
i.e. The key columns of kt, must appear in t and their column names MUST match exactly.

```
trades ij stock
```

As you can see the result has one less row than there was in trade, row 4 `DBK did not have a match by key lookup in the stock table so was dropped from the result.

### Different inner join than standard sql

Notice above that we said the join returns ONLY the first match from the lookup table. This is different than standard SQL which returns the cartesian join of all matches. The code below showns how we could replicate an SQL standard inner join:

#### t1
```sql type="table"
q)t1:([] sym:`a`b`c; v:1 2 3);
t1
```
#### t2
```sql type="table"
t2:([sym:`a`a`b] s:100 200 300);
t2
```

```sql type="table"
t1 ij t2
```

```sql type="table"
ungroup t1 ij `sym xgroup t2
```

# uj - Union Join

**uj - Union-Join all rows/columns from two tables, upserting when keyed, appending when unkeyed or no existing match found.**

```
trades uj fbTrades
```

Notice: Columns with common names are now one column in the result. However columns that occurred in only one table are included in the result, however where no values existed they are filled with nulls. 













        