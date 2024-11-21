# Day 14: Reindeer Olympics

[Question](https://adventofcode.com/2015/day/14) 
This year is the Reindeer Olympics! Reindeer can fly at high speeds, but must rest occasionally to recover their energy. Santa would like to know which of his reindeer is fastest, and so he has them race.

Reindeer can only either be flying (always at their top speed) or resting (not moving at all), and always spend whole seconds in either state.

For example, suppose you have the following Reindeer:

    Comet can fly 14 km/s for 10 seconds, but then must rest for 127 seconds.
    Dancer can fly 16 km/s for 11 seconds, but then must rest for 162 seconds.

After one second, Comet has gone 14 km, while Dancer has gone 16 km. After ten seconds, Comet has gone 140 km, while Dancer has gone 160 km. On the eleventh second, Comet begins resting (staying at 140 km), and Dancer continues on for a total distance of 176 km. On the 12th second, both reindeer are resting. They continue to rest until the 138th second, when Comet flies for another ten seconds. On the 174th second, Dancer flies for another 11 seconds.

In this example, after the 1000th second, both reindeer are resting, and Comet is in the lead at 1120 km (poor Dancer has only gotten 1056 km by that point). So, in this situation, Comet would win (if the race ended at 1000 seconds).

Given the descriptions of each reindeer (in your puzzle input), after exactly 2503 seconds, what distance has the winning reindeer traveled?

## Loading the Raw Data

```sql type='bar' server='kdbserver'
flip `n`s`t`r!("s  j  j      j";" ")0: `:input/14.txt
```

## Time-Series of each reindeer

```sql type='timeseries' server='kdbserver'
reindeert:flip `n`s`t`r!("s  j  j      j";" ")0: `:input/14.txt;
`time xdesc update time:08:00t+til count i from flip exec sums 2503#((t#s),r#0) by n from reindeert
```

```sql showcodeonly
t:flip `n`s`t`r!("s  j  j      j";" ")0: `:input/14.txt
max last f:flip value exec sums 2503#((t#s),r#0) by n from t
/2696
max sum each flip f=max flip f
/1084
```

