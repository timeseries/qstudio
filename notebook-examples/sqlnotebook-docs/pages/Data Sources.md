# Data Sources

SQLNotebooks supports any database with a JDBC driver, this includes kdb, postgresql, mysql, ms sql, clickhouse and [30+ databases](https://www.timestored.com/qstudio/database/).
Once a connection is created it can be used within all notebooks.

## Supported Databases

The driver for these databases are built into the qStudio download.

<style>
#datatab td,#datatab th,table#datatab,#datata td,#datata th,table#datata  { border:0px; padding:11px; }
</style>

<table id="datata" class="dsource" style="box-shadow:none; width:auto;">
	<tbody><tr>
	<td><a <a href="https://www.timestored.com/qstudio/database/kdb"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-kdb"><div>kdb+</div></a></td>
	<td><a <a href="https://www.timestored.com/qstudio/database/mysql"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-mysql"><div>mySQL</div></a></td>
	<td><a <a href="https://www.timestored.com/qstudio/database/questdb"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-questdb"><div>QuestDB</div></a></td>
	<td><a <a href="https://www.timestored.com/qstudio/database/postgres"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-postgres"><div>PostgreSQL</div></a></td>
	<td><a <a href="https://www.timestored.com/qstudio/database/../help/redis-sql-editor"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-redis"><div>Redis</div></a></td>
	</tr>
	<tr>
	<td><a <a href="https://www.timestored.com/qstudio/database/timescale"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-timescale"><div>TimeScale</div></a></td>
	<td><a <a href="https://www.timestored.com/qstudio/database/msserver"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-msserver"><div>Microsoft SQL Server</div></a></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-h2"><div>H2 Database</div></td>
	<td><a <a href="https://www.timestored.com/qstudio/help/duckdb-sql-editor"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-duckdb"><div>DuckDB</div></a></td>
	<td><a <a href="https://www.timestored.com/qstudio/database/oracle"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-oracle"><div>Oracle</div></a></td>
	</tr>
</tbody></table>



The driver for the below databases should automatically download on first usage.

<table id="datatab" class="dsource" style="box-shadow:none; border:0 !important; width:auto;">	
	<tbody><tr>
	<td><a href="https://www.timestored.com/qstudio/database/tdengine"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-tdengine"><div>TDengine</div></a></td>
	<td><a href="https://www.timestored.com/qstudio/database/dolphindb"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-dolphindb"><div>DolphinDB</div></a></td>
	<td><a href="https://www.timestored.com/qstudio/database/clickhouse"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-clickhouse"><div>Clickhouse</div></a></td>
	<td><a href="https://www.timestored.com/qstudio/database/mongodb"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-mongodb"><div>MongoDB</div></a></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-druid"><div>Druid</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-influxdb"><div>InfluxDB</div></td>
	<td><a href="https://www.timestored.com/qstudio/database/starrocks-client"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-starrocks" alt="starrocks database"><div>StarRocks</div></a></td>
	</tr>
	<tr>
	<td><a href="https://www.timestored.com/qstudio/database/redshift"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-redshift"><div>Redshift</div></a></td>
	<td><a href="https://www.timestored.com/qstudio/database/hsqldb"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-hsqldb_embedded"><div>HSQLDB</div></a></td>
	<td><a href="https://www.timestored.com/qstudio/database/sqlite"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-sqlite_jdbc"><div>SQLite</div></a></td>
	<td><a href="https://www.timestored.com/qstudio/database/csvjdbc"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-csvjdbc"><div>CSV</div></a></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-msaccess_ucanaccess"><div>MS Access</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-jdbc"><div>JDBC</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-doris" alt="apache doris"><div>Apache Doris</div></td>
	</tr>
	<tr>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-apache_calcite_avatica"><div>Apache Calcite Avatica</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-snowflake"><div>Snowflake</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-elasticsearch"><div>Elastic Search</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-mariadb"><div>MariaDB</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-apache_kylin"><div>Apache Kylin</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-db2_iseries"><div>DB2</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-singlestore" alt="SingleStore"><div>SingleStore</div></td>
	</tr>
	<tr>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-teradata"><div>Teradata</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-cratedb"><div>CrateDB</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-nuodb"><div>NuoDB</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-sap_hana"><div>SAP HANA</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-gemfire_xd"><div>Gemfire XD</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-snappydata"><div>Snappy Data Tibco</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-databend" alt="DataBend"><div>DataBend</div></td>
	</tr>
	<tr>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-spark_hive"><div>Spark Hive</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-kyuubi_hive"><div>Kyubi Hive</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-yandex_clickhouse"><div>Yandex Clickhouse</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-derby"><div>Derby</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-presto"><div>Presto</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-trino"><div>Trino</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-volt" alt="Volt"><div>Volt Active Data</div></td>
	</tr>
	<tr>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-apache_solrj"><div>Apache Solr</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-apache_ignite"><div>Apache Ignite</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-omnisci"><div>Omnisci</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-informix"><div>Informix</div></td>
	<td><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-sqream"><div>Sqream</div></td>
	<td><a href="https://www.timestored.com/qstudio/database/mysql-aurora"><img src="http://www.timestored.com/img/t.gif" height="64" width="64" class="zu-database"><div>Aurora</div></a></td>
	</tr>
</tbody></table>