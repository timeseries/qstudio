# Day 1: Not Quite Lisp

[Question:](https://adventofcode.com/2015/day/1) 

Santa was hoping for a white Christmas, but his weather machine's "snow" function is powered by stars, and he's fresh out! To save Christmas, he needs you to collect fifty stars by December 25th.

Collect stars by helping Santa solve puzzles. Two puzzles will be made available on each day in the Advent calendar; the second puzzle is unlocked when you complete the first. Each puzzle grants one star. Good luck!

Here's an easy puzzle to warm you up.

Santa is trying to deliver presents in a large apartment building, but he can't find the right floor - the directions he got are a little confusing. He starts on the ground floor (floor 0) and then follows the instructions one character at a time.

An opening parenthesis, (, means he should go up one floor, and a closing parenthesis, ), means he should go down one floor.

The apartment building is very tall, and the basement is very deep; he will never find the top or bottom floors.

For example:

    (()) and ()() both result in floor 0.
    ((( and (()(()( both result in floor 3.
    ))((((( also results in floor 3.
    ()) and ))( both result in floor -1 (the first basement level).
    ))) and )())()) both result in floor -3.

To what floor do the instructions take Santa?

```sql showcodeonly
last s:sums -1 1"("=first read0 `:input/01.txt
/232
1+s?-1
/1783
```

The -1 1 maps the () to increments and decrements, we can plot this as a line to show the sum effect of each step. The last step is the answer.

```sql type='line' server='kdbserver'
([] step:`$string til count t; t; s:sums -1 1"("=t:first read0 `:input/01.txt)
```

```sql server='kdbserver'
([] (1+s?-1; last s:sums -1 1"("=first read0 `:input/01.txt))
```