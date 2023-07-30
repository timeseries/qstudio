/**
 * Provides a GUI for interacting with KDB, combination of typical SQL
 * query console and notepad / programming environment.
 */
package com.timestored.qstudio;

//TODO allow user specific shortcuts with some defaults. Allow titles and search using ctrl+P
//TODO Add single instance as setting, so that people can call the software again to bring it to the front. Similar to what I do with the cmd for notepad++. Even if not opening a file.
//TODO sqldashboards recently opened file list

// TODO mac  
//TODO Dean Williams @ MAC - From Top Menu->File->Save As... Cannot find where to add new file name. Looks to be trying to use the Open File and Save File functionality.  Using Mac OSX 10.9.1
//TODO OSX - last character of shown data missing. e.g. a:11 shows 1 !!!
//TODO mac save as dialog doesnt allow specifying filename


//TODO Licensing! - test that it works as expected
//TODO add better adverts, graphics showing sqlDashboards

//TODO - improve text result - query text result poor - " .z.d-til 5" - without wrapping. It shows (2015-06-26d;2015-06-25d;2015-06-24d;2015-06-23d;2015-06-22d)

//TODO john@vaticlabs.com - See places that conn is set in QueryManager exception reported by john@vaticlabs.com caused by setServerName halfway through query causing NPE as watches are attempted to refresh. 
//TODO Ctrl + P
//TODO trim server names / ports when added - user typed server name with space and it just failed to run
//TODO faster ctrl+p - by speeding up DisplayQDocCommandProvider
//TODO faster ctrl+p - GotoDefinitionCommandProvider
//TODO faster ctrl+p - by making it async
//TODO ctrl+p - add recent files to command providers
//TODO add switch to tab to ctrl+p shortcut?

//TODO MOH check huge list of servers works when adding list, make faster if possible
//TODO qstudio - dashboard export from qStudio broken due to nested folders it seems
//TODO dragging servers between folders sometimes doesnt work
//TODO Default, remove wrapping?It prevents it working for some people. e.g. kodac users. (especially if we have popup cancel)
//TODO make result coding panel same color theme as code editor
//TODO  Allow converting tabs/spaces for formatting
//TODO BUG ERROR NPE ConnectionManager.getKdbConnection CsvLoaderModel.getCsvLoader(CsvLoaderModel.java:129) - odhow@hotmail.com  zjia97@gmail.com  omar@bainbridgepartners.com
//TODO BUG ERROR NPE SelectedServerObjectPanel.refreshGUI  SelectedServerObjectPanel.modelChanged(SelectedServerObjectPanel.java:104)  yoonjuni@gmail.com
//TODO On wrapped queries, if error shown.Suggest to user how they can turn off wrapping.
//TODO finish Add database reverse engineered documentation tool
//TODO duplicate ctrl+I entries - If you open files with the same name. Then press ctrl+I in one, you get entries from both.

//######### TOP PRIORITY #########
//TODO document mocking framework!!!!!
//TODO Allow option to not getTable list on a per server basis. Andrew in work had got an error - or even better only query current server..?
//TODO BUG bpisani@schonfeld.com IMPORTANT - qStudio freezes when previewing table from server tree and .Q.cn is not cached. Add test, fix by detecting cache...check tests pass.
//TODO keyword highlighting should be case sensitive.
//TODO BUG load csv is broken. Server field is missing, uploaded data just dissappeared!! - 1.Server should be currently connected one.   2.CSV/Pipe should be auto detected!   3.Upload should work!

//TODO sorting by row numbers, sorts by the strings!! - siddharth.verma@db.com
//TODO UTF-8 not good for everyone - Yen Symbol not showing in qStudio anymore - likely due to UTF-8 change. moussazeghida@mac.com. drzwz@qq.com mentoined issues as well
//TODO add chinese translations provided by drzwz@qq.com

//TODO accept config from text file? server list from file? including custom decimalFormatter
//TODO  italic fonts cause hhighlight mis-alignment bug yinghui.wu@capitaledge.cn
//TODO disable re-opening of scripts in second instance of qStudio tlynch@schonfeld.com
//TODO 1) It will be awesome if the string columns are displayed differently from symbol columns in a table. doing meta all the time is inconvenient. siddharth.verma@db.com
//TODO 4) I am not fully sure, but I think this memory leak problem is still there... My comp becomes slow if I keep Q Studio on for 2 weeks or so..  siddharth.verma@db.com
//TODO 2) while writing code this hover feature sometimes becomes a nuisance. I want to select a word and this hover text repeatedly obstructs my view siddharth.verma@db.com
//TODO CARLOS - window size not saved when I close/open qStudio. keeps going to 100%
//TODO documentation hover documentation , that becomes perm with html from kx.com
//TODO bug query "([] a:10#"¥")" change Chart type in dropdown to/from timeseries, exception box pops up  

//TODO qStudio remote desktop error reports, do not show alerts for known problems e.g. mschwartz@fxcm.com

//TODO doc reopen should support multiple windows.
//TODO startup time faster!
//TODO nicety - show full cell values when mouse hovers over jtable cell in history / results / expressions...
//TODO FileWatcher - show in GUI when files were changed by another process - ram.pantangi@citi.com

//TODO OpenJDK 1.7.0 main window was not rendering though it was responding to keyboard events. ( similar problems with NetBeans) - bartosz.kaliszuk@gmail.com
//TODO - license expiry shows message that "feature not available in this version", doesnt make sense.
//TODO server tree panel - divider sometimes takes stupid positions

//TODO queries for candlestick without necessary coulms are giving an RS cannot update error. Should show instructions on how to form query!
//TODO  enter commands directly to Console pane - stuart.smith3@comcast.net

//EDITOR Issues
//TODO control-f to search , loses position when clicking
//TODO control-f to search , maintains last typed rather than last highlighted text.
//TODO undo has glitches.

// SETTINGS

//TODO Dialogs - AutoComplete and Outline, pageup pagedown doesnt work. Maybe more elegant to forward certain keywtrokes?
//TODO hover autocompletes taht can be clicked on! see {@link Expandable
//TODO code documentation / jump to def for items within the same namespace are broken
//TODO qs slow on very large files, 4000+ lines - danila.deliya@jpmorgan.com
//TODO refresh folder, does not refresh the parsed docs cache, autocomplete shows old file
//TODO CARLOS - When you double click on Add Expression +, it would be nice if it deleted what is in the box.


//######### FEATURE ######### 
//TODO table diff comparison utility

//######### BUGS ######### 
//TODO tabbedPaneRightCLickBLocker NullPointereException - fw emails about this, latest from jeyakumar.muthuraj@alliancebernstein.com
//TODO partitioned table with count below 200, .Q.ind causes error in PagingTablePanel
//TODO listener memory leak on {@link ServerDescriptionPanel}, adds itself to {@link ServerModel} as listener but never removed. click back forth fraom server many times, then check number of listeners on ServerModel.
// remote desktop error see mohammad


//######### UNLIKELY EVER ######### 

//TODO ServerSlashConfig - make setting values work
//TODO refresh autocomplete docs more often than on just save?
//TODO support ctrl+L loading of \d defined namespaces
//TODO ServerTree - Offer expand All - Collapse All - Options - ram.pantangi@citi.com
//TODO qDoc doesn't detect new lines in comments
//TODO qDoc rearranges the @params in the HTML page, must either detect function order or take params order so end user knows order.
//TODO subscribing apps
//TODO notepad||tyle icons for readonly + fileWAtcher to see if others saved file
//TODO customized colors for code/cursor/lineNumberMargin,Token etc.

//TODO related to possible jtree output, could have html output? special table?
//TODO in jtree when query is (`p`o; ([] a:1 2 3)) table could be shown nicer


//TODO LINUX watched Expression - right click to remove does not work.
//TODO LINUX autocomplete, once you press control-space you can no longer type (to do with reqestFocus vs requestFocusInWindow OS support I believe). unlike in windows. Only click an option
//TODO check size of add server list dialog on all platforms
