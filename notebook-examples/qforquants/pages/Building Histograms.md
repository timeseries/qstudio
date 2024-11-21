# Building Histograms

For the full article see: https://nick.psaris.com/presentation/building-histograms/

 - Graphical presentation of a data set’s distribution
 - More descriptive than summary statistics
 - Chart granularity critically depends on the number of bins

There have been many attempts over the past 80 years to compute the optimal number of bins given a specific dataset. There are also many different ways to plot the histogram data. If we factor the code properly, we can compose a custom histogram function with exactly the properties we want.

```sql showcodeonly
\l qtips.q
q).util.use `.hist
`.
q)myhist:{([] k:string key x; v:value x)} chart[bar"*";30] count each bgroup[sturges]@
q)hist:{([] k:string key x; v:value 100*x%sum x)}count each bgroup[sturges]@
```

## Uniform Distribution

```sql server='kdbserver' type='bar'
hist 10000?1f
```

```sql server='kdbserver'
myhist 10000?1f
```

## Normal Distribution

```sql server='kdbserver' type='bar'
hist .stat.bm 10000?1f
```

```sql server='kdbserver' height='500px'
myhist .stat.bm 10000?1f
```