---
server: localhost:5000
---

# Keyed Tables

## Create a Keyed Table

A keyed table is a dictionary from one table of keys to another table of values with an equal number of rows. 
We can define it as such or we can use the shorter table syntax. Where we place the key columns inside square braces.

	
```sql showcodeonly
k:flip (enlist `id)!enlist `a`b`c`e
v:flip `name`employer`age!(`jane`jim`kim`john; `citi`citi`ms`ts; 11 22 33 55)
k!v
/ this syntax is easier to read.
kt:([id:`a`b`c`e]  name:`jane`jim`kim`john; employer:`citi`citi`ms`ts; age:11 22 33 55)
kt~k!v
/ multiple key columns
([id:`a`b`c`e;  name:`jane`jim`kim`john] employer:`citi`citi`ms`ts; age:11 22 33 55)
```
```sql
([id:`a`b`c`e;  name:`jane`jim`kim`john] employer:`citi`citi`ms`ts; age:11 22 33 55)
```

## Common Keyed Table Functions

The functions xkey and 0!,1!,2!.. allow setting and removing key columns:

```sql showcodeonly
q)keys kt
,`id

q)/ changing key columns
q)kt
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22
c | kim  ms       33
e | john ts       55
q)`id`name xkey kt
id name| employer age
-------| ------------
a  jane| citi     11
b  jim | citi     22
c  kim | ms       33
e  john| ts       55
q)
q)kt
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22
c | kim  ms       33
e | john ts       55
q)`id`name xkey `kt
`kt
q)kt
id name| employer age
-------| ------------
a  jane| citi     11
b  jim | citi     22
c  kim | ms       33
e  john| ts       55
q)() xkey kt
id name employer age
--------------------
a  jane citi     11
b  jim  citi     22
c  kim  ms       33
e  john ts       55
q)`id xkey `kt
`kt


q)ut:0!kt
q)ut
id name employer age
--------------------
a  jane citi     11
b  jim  citi     22
c  kim  ms       33
e  john ts       55
q)2!ut
id name| employer age
-------| ------------
a  jane| citi     11
b  jim | citi     22
c  kim | ms       33
e  john| ts       55
q)3!ut
id name employer| age
----------------| ---
a  jane citi    | 11
b  jim  citi    | 22
c  kim  ms      | 33
e  john ts      | 55
q)4!ut
'length
```


## Upserting Data into a Table

Whereas insert always appended data to unkeyed tables for keyed tables we use upsert. 
Upsert has two different behaviours, if there is an existing key -> update the values else if it's a new key -> insert.

	
```sql showcodeonly
q)kt:([id:`a`b`c`e]  name:`jane`jim`kim`john; employer:`citi`citi`ms`ts; age:11 22 33 5
q)kt
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22
c | kim  ms       33
e | john ts       55
q)nd:([id:`e`f] name:`dan`kate; employer:`walmart`walmart; age:200 200)
q)nd
id| name employer age
--| -----------------
e | dan  walmart  200
f | kate walmart  200
q)upsert[ kt; nd]
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22f
c | kim  ms       33
e | dan  walmart  200
f | kate walmart  200
```

You do not need to upsert all value columns.

 - If the keys do not already exist, any specified columns will be inserted. Unspecified columns will be filled with appropriate nulls.
 - If the keys already exist, specified columns will be overwritten with the new value. Other unspecified columns will remain unchanged.

	
```sql showcodeonly
q)upsert[ kt; ([id:`e`f] name:`dan`kate) ]
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22
c | kim  ms       33
e | dan  ts       55
f | kate
q)upsert[ kt; ([id:`e`f] name:`dan`kate; employer:`PPP`OOO) ]
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22
c | kim  ms       33
e | dan  PPP      55
f | kate OOO

q)/ upsert data must contain key columns
q)upsert[ kt; ([] name:`dan`kate; employer:`PPP`OOO) ]
'id
q)upsert[ kt; ([] id:`e`f; name:`dan`kate; employer:`PPP`OOO) ]
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22
c | kim  ms       33
e | dan  PPP      55
f | kate OOO

q)/ backtick needed to change underlying table
q)upsert[ `kt; ([] id:`e`f; name:`dan`kate; employer:`PPP`OOO) ]
`kt
q)kt
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22
c | kim  ms       33
e | dan  PPP      55
f | kate OOO

upsert[ kt; `id`name!`z`Alfred ]  / single item dictionary
 / single item list, must have all columns
upsert[ kt; (`z;`Alfred;`fedex;100) ] 
```

## Multiple Key Columns

Tables with more than one key column use compound keys for access and upserts. New Data must contain values for all key columns to allow upserts to succeed.


```sql showcodeonly
q)et:([employer:`kx`ms`ms; loc:`NY`NY`LONDON] size:10 2000 1000; area:0.9 15.1 11.2)
q)et
employer loc   | size area
---------------| ---------
kx       NY    | 10   0.9
ms       NY    | 2000 15.1
ms       LONDON| 1000 11.2
q)upsert[ et; ([employer:`kx`ms; loc:`NY`TURKEY] size:9 12) ]
employer loc   | size area
---------------| ---------
kx       NY    | 9    0.9
ms       NY    | 2000 15.1
ms       LONDON| 1000 11.2
ms       TURKEY| 12

q) / new data must have all key columns
q)upsert[ et; ([employer:`kx`ms] size:9 12) ]
'mismatch
q)upsert[ et; ([employer:`kx`ms; loc:`NY`TURKEY] size:9 12) ]
employer loc   | size area
---------------| ---------
kx       NY    | 9    0.9
ms       NY    | 2000 15.1
ms       LONDON| 1000 11.2
ms       

q) / backtick to actually alter table
q)upsert[ `et; ([employer:`kx`ms; loc:`NY`TURKEY] size:9 12) ]
`et
q)et
employer loc   | size area
---------------| ---------
kx       NY    | 9    0.9
ms       NY    | 2000 15.1
ms       LONDON| 1000 11.2
ms       TURKEY| 12

q)upsert[ et; ([] employer:`kx`ms; loc:`NY`TURKEY)] // no value columns
```	

## Selecting Data

Keyed tables can be accessed in many ways including: 


 - **qSQL** - it provides a universal wrapper for accessing keyed and unkeyed tables.
 - **Id Lookup**
   - Single - kt \`a
   - Multiple - kt[flip enlist \`c\`d]
 - **Table Lookup** - kt ([] id:\`a\`b )
 - Table #Take - ([] id:\`a\`b )#kt


```sql showcodeonly
q)kt
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22
c | kim  ms       33
e | dan  PPP      55
f | kate OOO

q)select from kt where employer=`citi
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22
q)kt `a
name    | `jane
employer| `citi
age     | 11
q)kt `aasd / non-existant = nulls
name    | `
employer| `
age     | 0N

q)kt `a`b
'length
q)kt[flip enlist `a`b]
name employer age
-----------------
jane citi     11
jim  citi     22


q)kt ([] id:`a`b)
name employer age
-----------------
jane citi     11
jim  citi     22
q)([] id:`a`b)#kt
id| name employer age
--| -----------------
a | jane citi     11
b | jim  citi     22



/ We can use the find operator ?
/ to lookup keys that match given values
kt?(`jane;`citi;11) / reverse lookup
```

Compound keys work similar to single keys, the table format retrieving values is recommended as being more clear in it's intent and easier for other developers to read.

```sql showcodeonly
q)et
employer loc   | size area
---------------| ---------
kx       NY    | 9    0.9
ms       NY    | 2000 15.1
ms       LONDON| 1000 11.2
ms       TURKEY| 12
q)et `ms`LONDON
size| 1000
area| 11.2
q)et (`ms`LONDON; `kx`NY)
size area
---------
1000 11.2
9    0.9
q)et ([] employer:`ms`kx; loc:`LONDON`NY)
size area
---------
1000 11.2
9    0.9
q)([] employer:`ms`kx; loc:`LONDON`NY)#et
employer loc   | size area
---------------| ---------
ms       LONDON| 1000 11.2
kx       NY    | 9    0.9
```

## Non-Unique Keys

One feature to be aware of is that key uniqueness is not enforced on table creation or when using xkey. 
Below we demonstrate how we can create a table with repeated keys and the behaviour of accessing such a table. 

```sql showcodeonly
q)lt:([a:1 2 2 3] val:`a`b`c`d)
q)lt
a| val
-| ---
1| a
2| b
2| c
3| d

q)/ only the firt match returned
q)lt 2
val| b


/ these functions do not force uniqueness
q)mt:0!lt
q)mt
a val
-----
1 a
2 b
2 c
3 d
q)`a xkey mt
a| val
-| ---
1| a
2| b
2| c
3| d
q)1!mt
a| val
-| ---
1| a
2| b
2| c
3| d
	
```






