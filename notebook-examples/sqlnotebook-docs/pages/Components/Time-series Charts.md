---
server: c:\temp\duckdb.duckdb
---

# Time-series Charts

Time-series Charts are the best way to visualize time-series data. They can render as lines, points or bars. 

Our SQLNotebooks work with <a href='/Data%20Sources'>30+ databases</a> , the examples below use this example [duckdb-demo.duckdb](https://www.timestored.com/sqlnotebook/files/duckdb-demo.duckdb) database** 
you can download it and change the server name in the header of the file to render the examples on your own machine.

## Single Time-Series

A result with a single time column and a single numeric column are rendered as a single area chart with latest , maximum and minimum values shown by default.

```sql type='timeseries'
SELECT time,gold FROM gold_vs_bitcoin ORDER BY time ASC
```

## Multiple Time-Series

A tabular result with multiple numeric columns is shown as one series line per numeric column. The latest values are shown as labels on the right hand side.

```sql type='timeseries'
SELECT * FROM gold_vs_bitcoin ORDER BY time ASC
```

# Time-Series TAQ Chart

Pulse has particularly good support for finance charts. The below image shows a time-series chart displaying:
See the [TAQ docs](Time-series%20TAQ) for more information.

```sql type='timeseries' overrideJson={{"custom":{"dataZoom":{"show":true}}, grid:{bottom:70}}}
SELECT * FROM taq ORDER BY time ASC
```

# OverrideJSON - eCharts

You can specify an ``overrideJSON`` argument to configure charts. For example setting custom.dataZoom.show:true would add a data control as shown above in the TAQ chart.

## dataZoom Control

````
```sql type='timeseries' overrideJson={{"custom":{"dataZoom":{"show":true}}, grid:{bottom:70}}}
SELECT * FROM taq ORDER BY time ASC
```
````


# colConfig - Column Configuration

You can specify an ``colConfig`` argument to configure how a column is displayed within a chart.

## Dual-Axis 

````
```sql type='timeseries' colConfig={ {bitcoin:{itemStyle:{color:'#888822' }, axisChoice:"rightax"}}}
SELECT * FROM gold_vs_bitcoin ORDER BY time ASC
```
````

```sql type='timeseries' colConfig={ {bitcoin:{itemStyle:{color:'#888822' }, axisChoice:"rightax"}}}
SELECT * FROM gold_vs_bitcoin ORDER BY time ASC
```

## Combined Bar and Line Charts

````
```sql type='timeseries' colConfig={ {bitcoin:{itemStyle:{color:'#661111' }, type:"bar"}}}
SELECT * FROM gold_vs_bitcoin ORDER BY time ASC
```
````

```sql type='timeseries' colConfig={ {bitcoin:{itemStyle:{color:'#661111' }, type:"bar"}}}
SELECT * FROM gold_vs_bitcoin ORDER BY time ASC
```

# SQL Based Configuration

```sql type='timeseries' colConfig={ {bitcoin:{itemStyle:{color:'#661111' }, type:"bar"}}}
SELECT * FROM japan_births_deaths ORDER BY Year
```

```sql type='grid' colConfig={ {bitcoin:{itemStyle:{color:'#661111' }, type:"bar"}}}
SELECT * FROM japan_births_deaths ORDER BY Year
```


You can configure the appearance of a column by adding an _SD_FORMATTER at the end of the column name.
For example if a column was call itemPrice, you could name it itemPrice_SD_CIRCLE to show the chart without a line and instead showing circle markers. Additionally you could add a column named: itemPrice_SD_SIZE to set the size of the circle/symbol.

<table class="bp4-html-table bp4-html-table-bordered bp4-html-table-condensed bp4-html-table-striped">
	<thead>
		<tr>
			<th>Area</th>
			<th>Example</th>
			<th>Options</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<th>Shape</th>
			<td>_SD_<b><i>CIRCLE</i></b></td>
			<td>CIRCLE, RECT, ROUNDRECT, TRIANGLE, DIAMOND, PIN, ARROW, NONE</td>
			<td>The shape to use for displaying points in the chart.</td>
		</tr>
		<tr>
			<th>Shape Size</th>
			<td>_SD_<b><i>SIZE</i></b></td>
			<td>Number 1-99</td>
			<td>The size of the shape to use for displaying points in the chart. You MUST have set an SD_SHAPE first.</td>
		</tr>
		<tr>
			<th>Point Marker</th>
			<td>_SD_<b><i>MARKPOINT</i></b></td>
			<td>String</td>
			<td>Adds a labeled point to the chart at the corresponding point.</td>
		</tr>
		<tr>
			<th>Point Line</th>
			<td>_SD_<b><i>MARKLINE</i></b></td>
			<td>String</td>
			<td>Adds a labeled vertical line to the chart at the x-axis/time location.</td>
		</tr>
		<tr>
			<th>Point Line</th>
			<td>_SD_<b><i>MARKAREA</i></b></td>
			<td>String</td>
			<td>Adds a shaded area to the chart between when the label starts and finishes.</td>
		</tr>
	</tbody>
</table>
