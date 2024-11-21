# Day 10: Elves Look, Elves Say

[Question](https://adventofcode.com/2015/day/10)

Today, the Elves are playing a game called look-and-say. They take turns making sequences by reading aloud the previous sequence and using that reading as the next sequence. For example, 211 is read as "one two, two ones", which becomes 1221 (1 2, 2 1s).

Look-and-say sequences are generated iteratively, using the previous value as input for the next step. For each step, take the previous value, and replace each run of digits (like 111) with the number of digits (3) followed by the digit itself (1).

For example:

    1 becomes 11 (1 copy of digit 1).
    11 becomes 21 (2 copies of digit 1).
    21 becomes 1211 (one 2 followed by one 1).
    1211 becomes 111221 (one 1, one 2, and two 1s).
    111221 becomes 312211 (three 1s, two 2s, and one 1).

Starting with the digits in your puzzle input, apply this process 40 times. What is the length of the result?

## Load File

```sql server='kdbserver'
([] enlist "J"$ first read0 `:input/10.txt)
```

## Step By Step

```sql server='kdbserver'
res:{raze (1_deltas i,count x),'x i:where differ x}\[50;]10 vs "J"$ first read0 `:input/10.txt;
10 sublist ([] step:til 51; res:res)
```

## Solution 

```sql showcodeonly
res:{raze (1_deltas i,count x),'x i:where differ x}\[50;]10 vs "J"$ first read0 `:input/10.txt
count res 40
/252594
count res 50
/3579328
```