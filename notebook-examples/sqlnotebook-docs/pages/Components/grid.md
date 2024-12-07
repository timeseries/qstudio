# Grid Component

Both the ``grid`` and the ``table`` can display tabular data within notebooks. 
The grid is useful when showing data with a large number of rows as it has a fixed height and allows paging.
The table is useful for small tables as it displays all rows as a full size HTML table. It is easier styled for printing.

```sql server='c:\temp\duckdb.duckdb' type='grid'
SELECT time, status, instrument, symbol_SD_TAG, price_SD_CURUSD, bid, bid_SD_BG, bid_SD_CODE, percent_SD_PERCENT0, percbar_SD_DATABAR, bid_SD_FG FROM quotes ORDER BY time ASC
```

You can either configure:

 1. The appearance of a **column** adding an _SD_FORMATTER at the end of the column name.
 2. The appearance of a **row** by adding specially named columns, similar to the existing.
 3. By setting grid options.

# Simple Table

````
```sql server='c:\temp\duckdb.duckdb' type='grid'
SELECT Country, Population, GDP, GDPperCapita, exports, exportsPerCapita FROM country_stats_scatter
```
````

```sql server='c:\temp\duckdb.duckdb' type='grid'
SELECT Country, Population, GDP, GDPperCapita, exports, exportsPerCapita FROM country_stats_scatter
```

## Grid Configuration Options

| Flag    | Settings | Description |
| -------- | ------- | ------- |
| pager  | 'none', 'all' or a number    | The number of rows shown on each page. |
| autosizeColumns | true or false     | Expand the columns to fill the table width. |
| height  | '150px', '50%' or any valid CSS value  | The height of the grid shown.  |
| showFilters  | true or false  | Show data filters at the top of the grid.  |

````
```sql type='grid' pager='none' autosizeColumns={false} height="240px" showFilters={true}
FROM bank_failures
```
````

```sql server='c:\temp\duckdb.duckdb' type='grid' pager='none' autosizeColumns={false} height="240px" showFilters={true}
FROM bank_failures
```

# Column Formatters

Below is our per column formatters, by naming a column with the postfix on the end, you can select the formatter:

````
```sql server='c:\temp\duckdb.duckdb' type='grid'
SELECT Country AS Country_SD_TAG, Population AS Population_SD_NUMBER0, GDP AS GDP_SD_CURGBP, 
        GDPperCapita, exports, exportsPerCapita  AS exportsPerCapita_SD_PERCENT0 
        FROM country_stats_scatter
```
````

```sql server='c:\temp\duckdb.duckdb' type='grid'
SELECT Country AS Country_SD_TAG, Population AS Population_SD_NUMBER0, GDP AS GDP_SD_CURGBP, 
        GDPperCapita, exports, exportsPerCapita  AS exportsPerCapita_SD_PERCENT0
        FROM country_stats_scatter
```

## Formatter Postfixes

<table>
<thead>
<tr>
<th>Area</th>
<th>Example</th>
<th>Column Name Postfix</th>
<th>Options</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<th>Numbers</th>
<td>0.11</td>
<td>_SD_NUMBER<b><i>0</i></b></td>
<td>0-9 Decimal places shown</td>
<td>Display as a number with thousand separators and decimal places.</td>
</tr>

<tr>
<th>Percentages</th>
<td>50%</td>
<td>_SD_PERCENT<b><i>0</i></b></td>
<td>0-9 Decimal places shown</td>
<td>Display as a percentage % with thousand separators and decimal places.</td>
</tr>
<tr>
<th>Currencies</th>
<td>$1,000.01</td>
<td>_SD_CUR<b><i>USD</i></b></td>
<td>USD/GBP/CCY where CCY is an <a href="https://en.wikipedia.org/wiki/ISO_4217">ISO 4217 currency code</a>. </td>
<td>Display an amount in a given currency. Always showing decimal places as appropriate.</td>
</tr>
<tr>
<th>Coloured Tags</th>
<td><span class="bp4-tag bp4-intent-primary .modifier">London</span></td>
<td>_SD_TAG</td>
<td>No options.</td>
<td>Highlight the text with a randomly selected color based on the text. So that the same text generates the same color.
   For more custom highlighting use <a href="#rawhtml">raw html</a>.</td>
</tr>
<tr>
<th>Status Flags</th>
<td><span class="bp4-tag bp4-intent-success .modifier">Done</span></td>
<td>_SD_STATUS</td>
<td>No options.</td>
<td>Highlight the text with an appropriate color based on the text content assuming the text represents a task. e.g.
<ul>
<li>Blue = new, open, created, ready, starting</li>
<li>Amber = runnable, waiting, partial, blocked, flagged, suspended, paused, stopping</li>
<li>Red = removed, cancelled, rejected, stopped</li>
<li>Green = terminated, resolved, closed, done, complete, filled, running</li>
</ul>
For more custom highlighting use <a href="#rawhtml">raw html</a>.
</td>
</tr>
<tr>
<th>HTML</th>
<td></td>
<td>_SD_HTML</td>
<td></td>
<td>Display the column content exactly as-is, rendering any HTML tags.</td>
</tr>
<tr>
<th>Databars</th>
<td><span class="databar" title="100%" style="color: green;">██████████</span></td>
<td>_SD_DATABAR</td>
<td>&nbsp;</td>
<td>Given a value between 0-1 i.e. a ratio or percent, draw it as a bar with size proportional to percentage.</td>
</tr>
<tr>
<th>Play Sound</th>
<td><span class="bp4-tag bp4-intent-success .modifier">alarm</span></td>
<td>_SD_SOUND</td>
<td>No options.</td>
<td>Play a sound or read the text out loud.
<ul>
<li><b>Pre-installed sounds</b> include: alarm, bell, buzzer, kaching, sheep, siren, stockbell, trumpet, uhoh.</li>
<li><b>URLs</b> beginning with http will be played if possible. e.g. https://www.timestored.com/files/goodresult.mp3</li>
<li><b>Text</b> will be read out loud using the system Text To Speech</li>
</ul>
For more information see <a href="#rawhtml">raw html</a>.
</td>
</tr>
</tbody>
</table>


# Sparklines

```sql server='localhost:5000' type='grid' height='150px'
select v,vl_sd_sparkline:v , vb_sd_sparkbar:v ,
        vd_sd_sparkdiscrete:v , vbl_sd_sparkbullet:v ,
  vpie_sd_sparkpie:v , vbox_sd_sparkboxplot:v  from 
  ([] a:1 2 3; v:(asc 9?1 2 3 1 9 8 7 3 -10 -28 7 3 -10 -2; 15?4 27 34 52 54 59  -4 -30 -45 52 54 59 61 68 78 82 85 87 91 93 100 ;6 6 6 6 6 -6 2 2 0))
```

#### Kdb Sparklines Example Code

```sql showcodeonly
select v,vl_sd_sparkline:v , vb_sd_sparkbar:v ,
        vd_sd_sparkdiscrete:v , vbl_sd_sparkbullet:v ,
  vpie_sd_sparkpie:v , vbox_sd_sparkboxplot:v  from 
  ([] a:1 2 3; v:(asc 9?1 2 3 1 9 8 7 3 -10 -28 7 3 -10 -2; 15?4 27 34 52 54 59  -4 -30 -45 52 54 59 61 68 78 82 85 87 91 93 100 ;6 6 6 6 6 -6 2 2 0))
```

#### H2 Database Sparklines Example SQL

```sql showcodeonly
/* DROP table foo; */
create table foo (
	id identity not null primary key,
	nums  array
); 

INSERT INTO FOO  (id, nums) VALUES (1, ARRAY [1,2,3,2,-5,-6,3,1,2,5]);
INSERT INTO FOO  (id, nums) VALUES (2, (4,5,6,8,9,14,18,25,10,0,-3));
SELECT ID,NUMS AS N_SD_SPARKLINE FROM FOO; 
```


# Highlighting and Formatting by Row:

**Highlighting and styling relies on having an additional column named similarly to the column you want to affect.**
For example to style a column called **itemPrice**, you could add an additional column called **itemPrice_SD_CURUSD** to show the price as a currency in US Dollars.
This allows you to customize the foreground/background and style per row.

![Row Highlighting](https://www.timestored.com/pulse/help/img/table-example-pulse-highlight-rows.png)

<table class="bp4-html-table bp4-html-table-bordered bp4-html-table-condensed bp4-html-table-striped">
		<thead>
			<tr>
				<th>Area</th>
				<th>Column Name Postfix</th>
				<th>Example Value</th>
				<th>Description</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<th>Background Color</th>
				<td>_SD_BG</td>
				<td>#FF0000</td>
				<td>Set the background colour of the original column.
					<br>HTML Colors can be specified as <a href="https://www.w3schools.com/html/html_colors.asp">names</a> or <a href="https://www.geeksforgeeks.org/html-font-color-attribute/">values</a>.</td>
			</tr>
			<tr>
				<th>Foreground Color</th>
				<td>_SD_FG</td>
				<td>#FF0000</td>
				<td>Set the foreground colour of the original column.
					<br>HTML Colors can be specified as <a href="https://www.w3schools.com/html/html_colors.asp">names</a> or <a href="https://www.geeksforgeeks.org/html-font-color-attribute/">values</a>.</td>
			</tr>
			<tr>
				<th>CSS Style Name(s)</th>
				<td>_SD_CLASS</td>
				<td>sd_cell_red sd_cell_green</td>
				<td>Set the CSS class of the original column.</td>
			</tr>
			<tr>
				<th>Format Code</th>
				<td>_SD_CODE</td>
				<td>0.xXXx 0.xxXX 0.xXX</td>
				<td>Configure the number of decimal places displayed AND which of the digits are shown larger. This is useful for emphasising basis points for FX currencies etc.</td>
			</tr>
		</tbody>
	</table>