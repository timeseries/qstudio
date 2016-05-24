# Custom JDBC Driver and Authenticator Example

This repo contains 

1. A method of specifying a custom JDBC driver.

2. The interfaces required to create a username/password lookup service for when you
want to have third party authentication of a user connecting to KDB. 


## Running qStudio with custom authentication method

Start qStudio with the arguments:
```
java -cp "qstudio.jar;qstudioopen.jar" com.timestored.qstudio.QStudioLauncher -Djdbc.isKDB=true -Djdbc.dbRequired=false -Djdbc.driver=kx.jdbc -Djdbc.urlFormat="jdbc:q:@HOST@:@PORT@" -Djdbc.authenticator=com.timestored.qstudio.open.ExampleDatabaseAuthenticationService
```

This uses the existing kdb driver but with the added DatabaseAuthenticationService that prompts the user for a password every time a connection is attempted.
To implement your own user/password lookup create a class that implements DatabaseAuthenticationService and pass that class name as the jdbc.authenticator property.


## What the arguments mean:

* ``jdbc.isKDB=true`` - Whether this JDBC driver is for kdb or not. It controls whether it is displayed as an option in qStudio.
* ``jdbc.dbRequired=false`` - Controls whether the database is added to the JDBC URL. 
* ``jdbc.driver=kx.jdbc``  - The full class name of the JDBC driver.
* ``jdbc.urlFormat="jdbc:q:@HOST@:@PORT@"`` - the URL used for connecting. Special symbols @HOST@,@PORT@,@DATABASE@ are replaced with the actual server details.
* ``jdbc.authenticator=com.timestored.qstudio.open.ExampleDatabaseAuthenticationService`` - What class to use to lookup connection details. 


## sqlDashboards Custom JDBC drivers

*This is untested*. Start sqlDashboards with the arguments:

```
java -cp "sqldashboards.jar;qstudioopen.jar" com.timestored.sqldash.SqlDashboardLauncher -Djdbc.driver=XXXXXXX -Djdbc.urlFormat="URLPREFIX:@HOST@:@PORT@:@DATABASE@"
```