# What is an SQLNotebook

SQLNotebooks are a code-driven method for building data applications with SQL. 
This includes reports, analysis, monitoring and embedded dashboards.

SQLNotebooks allow someone that knows SQL and a little markdown to create beatiful fully interactive data applications.

# What is qStudio / Pulse?

 - [qStudio](https://www.timestored.com/qstudio) is a Free SQL Client.
 - [Pulse](https://www.timestored.com/pulse) is a self-hostable platform for creating dashboards or SQLNotebooks
 - Both products are produced by [Pulse](https://www.timestored.com) a company that has been supplying data tools since 2013.

# How does it work?

1. A user creates a markdown document using SQLMarkdown syntax.
2. The engine allows a user to view the markdown as HTML in a browser.
3. The engine takes the SQL Queries defined in the markdown and runs them against its' Data Sources.
4. The results are rendered as Charts or other output components.
4. A user can provide input, for example by selecting options in dropdowns, these can change Variables in a query, which then refreshes the charts.
