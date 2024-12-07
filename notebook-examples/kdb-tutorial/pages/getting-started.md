---
server: localhost:5000
---

# kdb+ Tutorials using SQL Notebooks

These tutorials are available free at: https://www.timestored.com/kdb-guides/

These particular pages were generated using [SQL Notebooks](https://www.timestored.com/sqlnotebook/) within [QStudio](https://www.timestored.com/qstudio/) to demonstrate how easy it is to create tutorials. 

[QStudio](https://www.timestored.com/qstudio/) is a free SQL Client.

# Getting Started

### type math expressions directly into kdb

```sql showcodeonly
10+3
900-88
13*2
90%20 // division is the percentage symbol
```

Expressions are evaluated right to left

```sql showcodeonly
100%10+10
```

```sql
100%10+10
```

## Defining a variable

```sql showcodeonly
a:13
a 
b:10*9
b
a+b
c:a+b
c
```

## Comments
 
comments must be space then slash then comment
within a script there's another format / on new line then closed later

```sql showcodeonly
b: 1 // comments
b:1/ error as similar to adjectives we will see later
b:    1 // whitespace doesn't matter in kdb
```

## Slash Commands

What objects exist on the server

```sql showcodeonly
\v // variables
\a // tables
system "v"
system "a"
```

Can alter settings e.g. Precision and Console size

```sql showcodeonly
\P
\c
\c 22 88

\l script.q
```

If the slash command isn't recognised as a kdb call
it's passed to the underlying OS, e.g. dos commands

```sql showcodeonly
\cd
\echo test
system "cd"
```

```sql showcodeonly
\\
exit 0;
```