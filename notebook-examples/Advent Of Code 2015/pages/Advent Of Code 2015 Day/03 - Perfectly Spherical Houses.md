# Day 3: Perfectly Spherical Houses in a Vacuum

[Question](https://adventofcode.com/2015/day/3) Santa is delivering presents to an infinite two-dimensional grid of houses.

He begins by delivering a present to the house at his starting location, and then an elf at the North Pole calls him via radio and tells him where to move next. Moves are always exactly one house to the north (^), south (v), east (>), or west (<). After each move, he delivers another present to the house at his new location.

However, the elf back at the north pole has had a little too much eggnog, and so his directions are a little off, and Santa ends up visiting some houses more than once. How many houses receive at least one present?

For example:

    > delivers presents to 2 houses: one at the starting location, and one to the east.
    ^>v< delivers presents to 4 houses in a square, including twice to the house at his starting/ending location.
    ^v^v^v^v^v delivers a bunch of presents to some very lucky children at only 2 houses.

```sql showcodeonly
count distinct sums d:(0 1;0 -1;-1 0;1 0)"^v<>"?first read0 `:input/03.txt
/2572
count distinct raze sums 0N 2#d
/2631
```

## Santas Walk 
This scatter plot shows santas steps.

```sql type='scatter' server='kdbserver'
a:sums d:(0 1;0 -1;-1 0;1 0)"^v<>"?first read0 `:input/03.txt;
t:( [] x:a[;0]; y:a[;1]);
distinct t
```
Or with coloring to show repeats:
```sql type='scatter'  server='kdbserver'
tt:select c:count i by x,y from t;
f:{[v] (`x,`$"y",string v)xcol select x,y from tt where c=v};
((uj/) f each 1+til 10) 
```


```sql server='kdbserver'
a:count distinct sums d:(0 1;0 -1;-1 0;1 0)"^v<>"?first read0 `:input/03.txt;
/2572
b:count distinct raze sums 0N 2#d;
/2631
([] (a;b))
```
