# Developer Manual Testing

These tests would be ran on all platforms Mac / Windows / Linux.


## Open new version with your old settings

1. Check all servers still visible.
2. Check all previously open files and unnamed files are still open.
3. Check your font / server colors / themes are unchanged.
4. Copy your server list to somewhere safe (for later restoration).

## Wipe all settings - Fresh install

### Editor

0. Add an SQL server.
1. Create a new file.
2. Write a query by typing each character.
3. Copy the query and edit the middle using copy-paste.
4. Try Control+Q to run only the current SQL query.
5. Try Control+E to run only the highlighted text.
6. Try Control+Enter to run the line.
7. Open an existing SQL file. Help->DuckDB demo SQL. Check highlighting is OK.
   "Save as" to somewhere else. Close file. Reopen by double clicking in OS.
7A. Type "list_" then press control+Space to check auto-complete.
7B. Hover over the completed function to check docs work.
8. Open an existing Q file. Help->Open Example Charts.q . Check highlighting is OK.
   "Save as" to somewhere else. Close file. Reopen by dropping the file onto qStudio.
8A. Type ".Q." and check autocomplete works.

### Charting

9. For every query in "Help->Open Example Charts.q" - check that chart type works.

### Other

10. Result Pane - Export table - using excel icon on result pane.
11. Result Pane - Export selected table by highlighting part of result and right-click->export
12. Result Pane - Click Duck to export ot local database.
13. Server Tree - Click within server tree to change server.
14. Server Tree - Click table and check shown in result pane.
15. Command Bar - Press Control+P to run commands e.g. Switch server.
16. Servers - Bulk add large number of servers. 
17. Servers - Delete some servers. Restart qStudio. 
18. Multi_Instance - Start multiple qStudios, add one server in each. Try overlap.
   
### Notebooks

 19. Start Notebook with no sqlnotebook folder defined. Is example created ok for kdb+.
 20. Is example created ok for non-kdb.
 21. Modify existng workbook.
 22. Inspect all existing charts, are they showing.
 23. Add new page.

### Extra

 24. Parquet - Download and double click on parquet file https://www.timestored.com/data/sample/parquet
 25. KDB - Check display of custom types - dict, nested lists, every data type.

