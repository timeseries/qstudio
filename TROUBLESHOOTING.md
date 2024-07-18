# TROUBLESHOOTING

Tips and hints for troubleshooting problems with qStudio:

1. Double-clicking the `.jar` file starts the GUI. It doesn't provide
much diagnostic information.

2. The **Console** window shows the SQL generated from the PRQL query.
It also shows any error messages from parsing the query or executing it.

3. To get more information, launch qStudio from the command line.
The terminal session will display a lot of logging information.

   ```
   cd directory-containing-qstudio.jar
   java -jar qstudio.jar
   ```
   
