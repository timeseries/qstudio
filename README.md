# qStudio - Free SQL Analysis Tool

![Qstudio](qstudio/qstudio.png)

**qStudio is a free SQL GUI**, it allows running SQL scripts, easy browsing of tables, charting and exporting of results. 
It works on every operating system, with every database including mysql, postgresql, mssql, kdb.... 
For more info see [timestored.com/qstudio](http://timestored.com/qstudio "timestored.com/qstudio")

## Suports Every Database

kdb+, mySQL, QuestDB, PostgreSQL, Redis
TimeScale, Microsoft SQL Server, H2 Database, DuckDB, Oracle
TDengine, DolphinDB, Clickhouse, MongoDB, Druid, InfluxDB
Derby, HSQLDB, SQLite, CSV, MS Access, JDBC
Apache Calcite Avatica, Snowflake, Elastic Search, MariaDB, Apache Kylin, DB2, 
Teradata, CrateDB, NuoDB, SAP HANA, Gemfire XD, Snappy Data Tibco
Spark Hive, Kyubi Hive, Yandex Clickhouse, Neo4J, Presto, Trino
Apache Solr, Apache Ignite, Omnisci, Informix, Sqream, Aurora

## Features

 * **Server Browser** - Browse a server as a tree of objects with useful common tasks available from drop down menus.
 * **Built-in Charts** - Simply send the query you want and select the chart type wanted to draw a chart. 
 * **Syntax Highlighting**
 * **Code Completion**

 
### Pivot and Chart Data Easily
 
![QstudioPivot](qstudio/pivot-sql-query.gif)


### Powerful AI Assistant

*   **[Text2SQL](https://www.timestored.com/qstudio/help/ai-text2sql)** - Generates queries from plain English.
*   **[Explain-My-Query](https://www.timestored.com/qstudio/help/ai-explain-sql)** - Walks you through your code.
*   **[Explain-My-Error](https://www.timestored.com/qstudio/help/ai-sql-assistant)** - Ask AI why your code throw an error.

![QstudioAI](qstudio/ai-sql-query.gif)





## Changelog

2023-05-28 - 3.02   - Bugfixes and UI improvements. 
                    - Improve DolphinDB support. 
                    - Add PRQL Compilation Support.

2023-05-24 - 3.01   - AI - Generate SQL queries, ask for error help or explanations via OpenAI assistant.
					- Pivot - Perform excel like pivots within qStudio and have it generate the query for you.
					- BabelDB - Query any database and store it to a local duckdb instance.  
					- SQL - support significantly improved. Documentation added, highlighting improved, added code formatter. 
					- SQL - Added Ctrl+Q run current query support. Lower/uppercase commands.  
					- Parquet File viewer support.
					- Generate command line charts using sqlchart command 
					- Default theme changed to dark mode.  
					- UI Niceties - Added icons for charts / database types. Safer chart rendering. 
					- UI Niceties - Document tabs now allow mouse scrolling, added file options to menu. Fixed bugs. 
					- Remove - Legacy java licensing code.
					- DuckDB - Improved rendering of the various SQL types.

.....

2023-02-24 - 2.01   Version 2.0 
					
2013-01-24 - 1.20   - FIRST RELEASE.
