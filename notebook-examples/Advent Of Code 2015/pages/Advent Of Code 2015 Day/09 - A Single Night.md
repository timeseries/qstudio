# Day 9: All in a Single Night

[Question](https://adventofcode.com/2015/day/9) Every year, Santa manages to deliver all of his presents in a single night.

This year, however, he has some new locations to visit; his elves have provided him the distances between every pair of locations. He can start and end at any two (different) locations he wants, but he must visit each location exactly once. What is the shortest distance he can travel to achieve this?

For example, given the following distances:

London to Dublin = 464
London to Belfast = 518
Dublin to Belfast = 141

The possible routes are therefore:

Dublin -> London -> Belfast = 982
London -> Dublin -> Belfast = 605
London -> Belfast -> Dublin = 659
Dublin -> Belfast -> London = 659
Belfast -> Dublin -> London = 605
Belfast -> London -> Dublin = 982

The shortest of these is London -> Dublin -> Belfast = 605, and so the answer is 605 in this example.

What is the distance of the shortest route?

## Loading the Raw File

```sql type='sankey' server='kdbserver'
flip`f`t`d!("s s i";" ")0:`:input/09.txt
```

```sql type='grid' server='kdbserver'
flip`f`t`d!("s s i";" ")0:`:input/09.txt
```

```sql showcodeonly
r:flip`f`t`d!("s s i";" ")0:`:input/09.txt;
/combinations
c:{raze y,/:'x except/:y}[ix;]/[count[ix]-1;] ix:distinct (raze/)exec (f;t) from r;
/calculate all routes
min routes:{ sum {[x;y] first exec d from r where f in (x;y), t in (x;y) }.'-2#'prev\[count[x]-2;x] } each c
/251
max routes
/898
```

```sql server='kdbserver'
r:flip`f`t`d!("s s i";" ")0:`:input/09.txt;
c:{raze y,/:'x except/:y}[ix;]/[count[ix]-1;] ix:distinct (raze/)exec (f;t) from r;
([] enlist min routes:{ sum {[x;y] first exec d from r where f in (x;y), t in (x;y) }.'-2#'prev\[count[x]-2;x] } each c)
```
