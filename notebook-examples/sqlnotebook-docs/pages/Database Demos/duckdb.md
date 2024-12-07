---
server:  c:\temp\duckdb.duckdb
---

 - **DuckDB** is an open-source column-oriented relational database management system (RDBMS) originally developed by Mark Raasveldt and Hannes Mühleisen at the Centrum Wiskunde & Informatica (CWI) in the Netherlands and first released in 2019.
 - **qStudio** has (excellent support for DuckDB)[https://www.timestored.com/qstudio/help/duckdb-sql-editor]
 - **Download the example [duckdb-demo.duckdb](https://www.timestored.com/sqlnotebook/files/duckdb-demo.duckdb) database** and change there server path to render the example charts.

# Grid - Latest Trade Prices

Table display can be <a href="/Components/grid">configured using column names</a>. 

```sql type="grid"
SELECT * FROM quotes;
```

# Time-series - Gold vs Bitcoin 2024

```sql type="timeseries"
SELECT * FROM gold_vs_bitcoin
```


## Search Trends 2024


```sql server='QDUCKDB' type='timeseries' overrideJson={{grid:{top:50}}}
SELECT * FROM search_trends
```

# Population Trends

## Niger Population

```sql type='stack_horizontal' server='c:\temp\duckdb.duckdb' 
/* https://www.census.gov/data-tools/demo/idb/ */
select 
 Age,
 CASE WHEN males<mini THEN males ELSE mini END as males ,
 CASE WHEN females<mini THEN -females ELSE -mini END as females,
 CASE WHEN females<mini THEN 0 ELSE -(females-mini) END as femalesSurplus ,
 CASE WHEN males<mini THEN 0 ELSE males-mini END as malesSurplus ,
 FROM 
(select *, CASE WHEN males<females THEN males ELSE females END as mini 
 from 
niger_population)
```

## Japan Population 

```sql type='stack_horizontal' server='c:\temp\duckdb.duckdb'
/* https://www.census.gov/data-tools/demo/idb/ */
select 
 Age,
 CASE WHEN males<mini THEN males ELSE mini END as males ,
 CASE WHEN females<mini THEN -females ELSE -mini END as females,
 CASE WHEN females<mini THEN 0 ELSE -(females-mini) END as femalesSurplus ,
 CASE WHEN males<mini THEN 0 ELSE males-mini END as malesSurplus ,
 FROM 
(select *, CASE WHEN males<females THEN males ELSE females END as mini 
 from 
japan_population)
```

## Japan Birth Deaths

```sql type='timeseries' server='c:\temp\duckdb.duckdb' 
select *,Births-Deaths as Delta from japan_births_deaths
```

Source: https://en.wikipedia.org/wiki/List_of_bank_failures_in_the_United_States_(2008%E2%80%93present)

```sql type='treemap' server='c:\temp\duckdb.duckdb' 
select Bank,"Assets ($mil.)" as Millions from bank_failures
```

# Cost of Christmas by Country

```sql type='stack_horizontal' server='c:\temp\duckdb.duckdb' 
select Country,CostOfChristmas,-CostOfChristmas,Trunk,-Trunk,Star,-Star from  christmas_cost
```

## Candlestick Chart


- The table should contain columns labelled open/high/low/close/volume.
- The table must atleast contain high/low or open/close to allow it to be drawn.


```sql type="candle"
SELECT * FROM candle
```

# Bar

- The first string columns are used as category labels.
- Whatever numeric columns appear after the strings represents a separate series in the chart.

```sql type="bar"
SELECT * FROM companies
```

# Bar Horizontal

```sql type="bar_horizontal"
SELECT * FROM companies
```

# Stack

- The first string columns are used as category labels.
- Whatever numeric columns appear after the strings represents a separate series in the chart.

```sql type="stack"
SELECT * FROM companies
```

# Bar Horizontal

```sql type="stack_horizontal"
SELECT * FROM companies
```

# Line

- The first string columns are used as category labels.
- Whatever numeric columns appear after the strings represents a separate series in the chart.

```sql type="line"
SELECT * FROM companies
```


# Area

- The first string columns are used as category labels.
- Whatever numeric columns appear after the strings represents a separate series in the chart.
    
```sql type="area"
SELECT * FROM companies
```

# Pie

 - Each numeric column represents one pie chart. The title of each pie chart will be the column title.
 - The segments of the pie chart use the string columns as a title where possible. If there are no string columns, row numbers are used.

```sql type="pie"
SELECT * FROM companies
```

# Hierarchical

## Tree

 - Starting from the left each string column is taken as one nesting level
 - The first numerical column will be taken as size.

```sql type="tree"
SELECT * FROM tree
```

## Sankey

 - Assuming string columns named S1,S2,S3 with a numeric column of value V.
 - Each row represents one flow from the top level S1 to the leaf node S3. S1->S2->S3->V
 - The first numeric column reprents the size of the flow between nodes.
 - Sizes are back-propagated to the top level.
 - Null can be used to represent either gaps or allow assigning value to a node that is neither an inflow nor outflow.


```sql type="sankey"
SELECT * FROM sankey
```


## Sunburst

 - Starting from the left each string column is taken as one nesting level
 - The first numerical column will be taken as size.


```sql type="sunburst"
SELECT * FROM country_stats_scatter
```


## Scatter


 - Two or more numeric columns are required.
 - The values in the first column are used for the X-axis.
 - The values in following columns are used for the Y-axis. Each column is displayed with a separate color.


```sql type="scatter"
FROM country_stats_scatter
```

## bubble

 - The first string columns are used as category labels.
 - There must then be 3 numeric columns which are used for x-coord, y-coord, size in that order.

```sql type="bubble"
FROM country_stats_scatter
```

## Heatmap

 - Each numerical column in the table becomes one column in the chart.
 - The numerical values represent the shading within the chart.

```sql type="heatmap"
FROM country_stats_scatter
```

## Radar

 - A radar chart requires 3 or more numeric columns to render sensibly.
 - Each numeric column represents one spoke in the radar. The column titles are used as spoke titles.
 - Each row in the data represents one circle withing the radar.

```sql type="radar"
FROM radar
```

## Calendar

 - The table should contain a date and atleast one numeric column.
 - The first numeric column will be used as the value for that date.
 - Dates should not be repeated. If they are the value selected is not guaranteed.


```sql type="calendar" height="500"
FROM calendar
```
 
## Boxplot

 - Each numerical column in the table becomes one boxplot item in the chart.
 - The min/max/median/Q1/Q3 are calculated from the raw data.
 - This is inefficient as a lot more data is being passed than needed but useful for toggling an existing data set view quickly.

```sql type="boxplot"
SELECT * FROM boxplot
```

## Metrics

 - Two or more numeric columns are required.
 - The values in the first column are used for the X-axis.
 - The values in following columns are used for the Y-axis. Each column is displayed with a separate color.


```sql type="metrics"
SELECT * FROM metrics
```

