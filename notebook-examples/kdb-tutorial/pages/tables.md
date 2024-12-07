---
server: localhost:5000
---
# Tables

## Create a kdb Table

In an earlier tutorial I said that lists and dictionaries were the only fundamental data structures in kdb that all others built on top of. 
A table is a specially formed dictionary from column names to equal length vectors of data. This simplicity is also powerful as it allows us 
to access and manipulate table data using all our previously learnt list/dictionary methods.

```sql showcodeonly
d:`company`employees!(`ford`bmw;300 100)
t:flip d
type d
type t
```

```sql type='table' width='100px'
flip `company`employees!(`ford`bmw;300 100)
```

Typically when defining a table we use the q language shorthand notation. Parentheses to contain our table, with vector data assigned to column names, separated by semi-colons. 
(The square bracket is for defining keyed tables and we will look at this further in [keyed tables](/markdown?sheet=kdb-tutorials%2Fkeyed-tables)).

```sql showcodeonly
t~([] company:`ford`bmw; employees:300 100)

/ must enlist data if creating one row table
([] company:`ford; employees:300)
([] company:enlist `ford; employees:enlist 300)


([] syms:`a`b`c; floats:1.1 2.2 3.3; strings:("bob";"jim";"john"))
([] syms:`a`b`c; chars:"aaa"; times:08:00 08:30 09:00)
/ atoms get expanded to fill columns
([] syms:`a`b`c; num:33)
```
```sql height='150px'
([] syms:`a`b`c; floats:1.1 2.2 3.3; strings:("bob";"jim";"john"))
```

## Defining Empty Tables

Normally when defining a table, you will define it as empty and insert data later, e.g. from a feedhandler. When defining a table the columns should be set to the correct type when possible as this allows type checking inserted data.


```sql showcodeonly
t:([] company:`ford`bmw; employees:300 100)
meta t
t:([] company:(); employees:())
meta t
t:([] company:`symbol$(); employees:`int$())
meta t
```
```sql height='120px'
t:([] company:`symbol$(); employees:`int$());
meta t
```

## Common Table Functions

The most common functions used with tables are shown below:

	
```sql showcodeonly
t:([] company:`ford`bmw; employees:300 100)
t
type t
count t // return number of rows
cols t  // retrieve symbol list of column names
meta t

/ family of xfunctions
`a`b xcols t // reorder columns
`employees xasc t // sort table by a column

/ List the tables that exist
\a .
system "a ."
tables `.
```

## Set Operations

The set functions that we previously used on lists also work on tables:

```sql showcodeonly
t:([] company:`ferrari`ford`rover; employees:3 66 200)
u:([] company:`ferrari`bmw`ford; employees:3 88 77)
distinct t
t union u
t except u
t inter u 
```

	
## Accessing a Table - qSQL

There are three methods for accessing an unkeyed table, qSQL, as a dictionary and as a list. qSQL is the most common method
 and we will look at it in much more detail later. Unlike standard SQL no * is needed to select all columns and some simple queries would include:
 
 
```sql showcodeonly
 t:([] company:`ferrari`ford`rover`bmw`AA; employees:3 66 200 88 1)

// qSQL
select from t 
select from t where company=`ford
select employees,eFactor:employees%100 from t where company=`ford	
```
 
```sql type='table'
 carst:([] company:`ferrari`ford`rover`bmw`AA; employees:3 66 200 88 1);
select employees,eFactor:employees%100 from carst where company=`ford	
```

 At the start we demonstrated a table is a dictionary from a list of column name symbols to vectors of data. We can use that 
 method of accessing a table, by supplying a column name as a lookup, we return that columns data as a list.

```sql showcodeonly
t[`company]
t[`employees]-:1000

// as a list
t[0 1 2]
-3#t
-2?t
```

Alternatively if we treat the table as a list of dictionaries, we can index into that list to retrieve multiple items. 
Other standard list functions work similarly, returning a number of rows from the table.

## Inserting Data into a Table

To insert data into an unkeyed table, we use the insert function to append new rows to the end of the table. 
Insert allows multiple formats including single lists, multiple batch lists and insertion of tables.

	
```sql showcodeonly
t:([] company:(); employees:())
meta t

insert[`t; (`ferrari;8)]
meta t
insert[`t; (`ferrari;8.22)] / this fails as wrong type
/ why you should specify type during creation
insert[`t; (`ferrari`mg;9 7)]
insert[`t; ([] company:`subaru`hyundai; employees:55 56)]
insert[`t; ([] company:`jeep`mercedes; employees:66 65.666)]

/ append using table joins (comma)
t:t,([] company:`bmw`skoda; employees:200 300)
```

### See Also:

 - Next Lesson: [keyed tables](/markdown?sheet=kdb-tutorials%2Fkeyed-tables)
 - [kdb+ Tutorials](https://www.timestored.com/kdb-guides/)










