---
server: localhost:5000
---

# Q-SQL

Q-SQL is the subset of sql that kdb provides to allow querying and manipulating tables. 
There are a number of specific fundamental concepts underlying q-sql that make it work differently to standard SQL. 
Unlike most databases, kdb is column oriented. A column is a list or vector and has order. 
This contrasts to row oriented standard sql databases that are based on the concepts of sets and have no order. 
The order and specific wording of the syntax is often different to standard sql. Q shortens standard sql syntax where possible.

## Query Formats

```sql showcodeonly
q)\l trades.q
(+`date`sym!(2013.09.21 2013.09.22 2013.09.23 2013.09.24 2013.09.25;`RBS`RBS`RBS`RBS
q)\a
`quote`stock`trade
```
```
q)select from trade where i<20
```

```
q)-20 sublist select from trade where date=2024.12.02
```

```
q)-20 sublist select from trade where date=2024.12.02,sym=`A
```

```sql height='100px'
q)select max price from trade where date=2024.12.02,sym=`A
```

## Compared to standard SQL:

You can see from the first line that we don't have "select *", in kdb if you want to select every column we just omit the column names 
(typical of kdb to favour brevity). On the second line date=2011.01.02,sym=`a In kdb comma is the preferred separator of conditional 
clauses for reasons we shall see later. Rather than standards sql's group by, we use select by in q-sql and the columns are in the
 output without having to repeat in the select.
 
 
# Select From Where

### select c1, c2 by c3, c4 from table where expression1, expression2

A select query contains 4 parts that are evaluated in the order:

1. Table
2. Where clauses
3. By Aggregates
4. Column / Value selections

For a query such as: ``select max price by exchange from trade where date=2024.01.02,sym=`AA``

The steps are:

1. Starting from the leftmost where clause examing each in turn
   - Scan the date column entirely and find all positions that match the date 2011.01.02.
   - Take the positions that matched in the date column and examine the same positions in the sym column this time to match `AA
2. Take our final list of matching rows, scan the exchange column and group them in order by their exchange value. So that for each distinct exchange we get a list of indices.
3. Extract each list of matching indices from price column giving us a list of prices per sym.
4. Perform max on each list, meaning we now have one price per sym.

## Where Clause: Acts as a logical filter on rows.

 - Commas act as a logical "and", where each condition is evaluated in turn, left to right.
 - The OR logical operation is available however due to q's natural right-to-left evaluation, we must use parentheses: ``select from t where (a=1) or (b=2)``

```sql showcodeonly
q)\t do[80; select max price from trade where sym=`AAPL,cond="A"]
338
q)\t do[80; select max price from trade where cond="A",sym=`AAPL]
587

q)count select from trade where cond="A"
211597
q)count select from trade where sym=`AAPL
50106

q) / careful to use parentheses with and/or clauses
q)\t do[80; select max price from trade where (cond="A") and sym=`AAPL]
402
```

```
q)select max price from trade where sym=`AAPL,(cond="A") or cond="B"
```

```
q)-20 sublist select from trade where sym=`AAPL,(cond="A") or cond="B"
```
The below query is not giving the result  you would expect:
```
q)-20 sublist select from trade where sym=`AAPL,cond="A" or cond="B"
```

Notice also the timing and performance difference of the queries:

```sql showcodeonly
q)\t do[80; select max price from (select from trade where cond="A") where sym=`AAPL]
893

q)/ the comma filter in the correct order is fastest
q)\t do[80; a:select max price from trade where sym=`AAPL,cond="A"]
370
q)\t do[80; b:select max price from trade where cond="A",sym=`AAPL]
615
q)\t do[80; c:select max price from trade where (cond="A") and sym=`AAPL]
409
q)\t do[80; d:select max price from (select from trade where (cond="A")) where sym=`AAPL]
884
q)a~b
1b
q)b~c
1b
q)c~d
1b
```
 
# By Aggregates: 


 - Groups selected column values by an aggregate.
 - The aggregates become the keys of the keyed table.
 - Is often the most expensive part of a query.
 

### Special case that returns the last row

```
select by sym from trade where date=.z.d
```
 	
By, groups the select columns by the aggregates making the table keyed by them aggregates in order:

```
select 10 sublist price by sym from trade where date=.z.d
```

```sql showcodeonly
select price by sym from trade where date=.z.d
select price by sym, cond from trade where date=.z.d
// then by applying an aggregate function, we reduce each list to an atom
select max price by sym from trade where date=.z.d

// BY - is a costly operation, in time and memory
\ts do[20; select price by sym from trade where date=.z.d]
\ts do[20; select price by sym, cond from trade where date=.z.d]
\ts do[20; select price by sym, cond, date from trade where date=.z.d]
```

# Select Parameters:

 - Each entry becomes a column in the output table
 - If unspecified, the column name is taken from the last used underlying column.
 
	
Since vectors and ordered, some things are much easier in qSQL
```
q)select first price,first time by date from trade where sym=`AAPL
```
```
q)select last price,last time by date from trade where sym=`AAPL
```

### Open High Low Close = Candlestick
```
q)select open:first price, high:max price, low:min price, close:last price  by date from trade where sym=`AAPL
```	
```sql type='candle'
q)select open:first price, high:max price, low:min price, close:last price  by date from trade where sym=`AAPL
```	


## select in

in - checks to see if every item of its LHS argument occurs anywhere in its RHS argument, if so it returns true, otherwise false.
In a where clause this is useful for selecting a group of data, rather than specifying each using equals.
The below queries return the same result:
 
```
q)-20 sublist select from trade where (sym=`RBS) or (sym=`AAPL)
```
```
q)-20 sublist select from trade where sym in `AAPL`RBS
```


	 
 
 
 
 