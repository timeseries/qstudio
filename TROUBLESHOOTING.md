# TROUBLESHOOTING

Tips and hints for installing, using, and troubleshooting problems with qStudio:

1. Double-clicking the `.jar` file starts the GUI. It doesn't provide
much diagnostic information.

2. The **Console** window shows the SQL generated from the PRQL query.
It also shows any error messages from parsing the query or executing it.

3. To get more information, launch qStudio from the command line.
The terminal session displays a lot of logging information.

   ```
   cd directory-containing-qstudio.jar
   java -jar qstudio.jar
   ```
   
4. The `prqlc` binary must be installed and available to qStudio.
  * On macOS, the easiest way to install `prqlc` is with
  the [Homebrew package manager](https://brew.sh/).
  It will install the current binary and make it available on the PATH.
  (Homebrew is a terrific tool because it does not
  require any superuser permissions,
  and saves all its files in a few well-known directories.)
