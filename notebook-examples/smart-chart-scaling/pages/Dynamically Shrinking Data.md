# Dynamically Shrinking Big Data using time-series database kdb+

Below is a chart sampled with 39, 63, 97, 157, 309 and 1022 data points. 
<br />How much difference can you see?

```sql type='timeseries' server='kdbserver' height='300px'
tt:(uj/) {1!(`time,`$"close",string x) xcol exec flip `time`price!rdpRecur[x;date;close] from btc} each {x!x} reverse 100 200 300 400 500 600 800 1000;
`time xdesc (select time from tt),'flip cols[value tt]!(1+til[count a]*4000)+a:value flip value tt 
```

This article was written by [Sean Keevey](https://www.linkedin.com/in/sean-keevey-672bb112/) and [Kevin Smyth](https://www.linkedin.com/in/kevin-smyth-5a6a731a/)
, it demonstrates smart data sampling to reduce the size of data required to represent time-series data while maintaining visually perceived accuracy. 
It uses BTCUSDT data from Kraken. It was originally published as part of the [KX whitepaper series](https://code.kx.com/q/wp/)
 and has been updated with new data and adapted to [sql notebooks format](https://www.timestored.com/sqlnotebook/).

# Intro

It is often the case that when dataset visualizations are focused on trends or spikes occurring across relatively longer time periods, 
a full tick-by-tick account is not desired; in these circumstances it can be of benefit to apply a simplification algorithm to reduce the
 dataset to a more manageable size while retaining the key events and movements.

This paper will explore a way to dynamically simplify financial time series within kdb+ in preparation for export and consumption by 
downstream systems. We envisage the following use cases, among potentially many others. 
Key to our approach is avoiding distortion in either the time or the value domain, which are inevitable with bucketed summaries of data.

# Background

The typical way to produce summaries of data sets in kdb+ is by using bucketed aggregates. Typically these aggregates are applied across time slices.

## Bucketing

Bucketing involves summarizing data by taking aggregates over time windows. This results in a loss of a significant amount of resolution.

``select avg 0.5 * bid + ask by 00:01:00.000000000 xbar time from quote where sym=`ABC``

The avg function has the effect of attenuating peaks and troughs, features which can be of particular interest for analysis in certain applications.
A common form of bucket-based analysis involves taking the open, high, low and close (OHLC) price from buckets. These are plotted typically on a candlestick or line chart.

```sql showcodeonly
select o:first price, h:max price, l:min price, c:last price 
   by 00:01:00.000000000 xbar time from trade where sym=`ABC
```

OHLC conveys a lot more information about intra-bucket price movements than a single bucketed aggregate, and will preserve the magnitude of
 significant peaks and troughs in the value domain, but inevitably distorts the time domain – all price movements in a time interval are 
 summarized in 4 values for that interval.
 
## A New Approach

The application of line and polygon simplification has a long history in the fields of cartography, robotics and geospatial data analysis. 
Simplification algorithms work to remove redundant or unnecessary data points from the input dataset by calculating and retaining prominent
 features. They retain resolution where it is required to preserve shape but aggressively remove points where they will not have a material
  impact on the curve, thus preserving the original peaks, troughs, slopes and trends resulting in minimal impact on visual perceptual quality.


Throughout the following sections we will use the method devised by (Ramer, 1972) and (Douglas & Peucker, 1973) to conduct our line-simplification analysis. 
The [Ramer-Douglas-Peucker method](https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm) 
may be described as a recursive divide-and-conquer algorithm whereby a line is divided into smaller 
pieces and processed. Recursive algorithms are subject to stack-overflow problems with large datasets and so in our analysis we 
present both the original recursive version of the algorithm as well as an iterative, non-recursive version which provides a
 more robust and stable method.


# Implementation

We initially present the original recursive implementation of the algorithm, for simplicity, as it is easier understood.

## Recursive implementation

```sql showcodeonly
// perpendicular distance from point to line
pDist:{[x1;y1;x2;y2;x;y]
  slope:(y2 - y1)%x2 - x1;
  intercept:y1 - slope * x1;
  abs ((slope * x) - y - intercept)%sqrt 1f + slope xexp 2f }

rdpRecur:{[tolerance;x;y]
  // Perpendicular distance from each point to the line
  d:pDist[first x;first y;last x;last y;x;y];
  // Find furthest point from line
  ind:first where d = max d;
  $[tolerance < d ind;
    // Distance is greater than threshold => split and repeat
    .z.s[tolerance;(ind + 1)#x;(ind + 1)#y],' 1 _/:.z.s[tolerance;ind _ x;ind _ y];
    // else return first and last points 
    (first[x],last[x];first[y],last[y])] }
```

It is easy to demonstrate that a recursive implementation in kdb+ is prone to throw a stack error
 for sufficiently jagged lines with a low input tolerance. For example, given a triangle wave function as follows:

```sql showcodeonly
q)triangle:sums 1,5000#-2 2
q)// Tolerance is less than distance between each point, 
q)// would expect the input itself to be returned
q)rdpRecur[0.5;til count triangle;triangle]
'stack
```

## Iterative implementation

Within the recursive version of the Ramer-Douglas-Peucker algorithm the subsections that have yet to be analyzed 
and the corresponding data points which have been chosen to remain are tracked implicitly and are handled in turn 
when the call stack is unwound within kdb+. To circumvent the issue of internal stack limits the iterative version 
explicitly tracks the subsections requiring analysis and the data points that have been removed. This carries a 
performance penalty compared to the recursive implementation.


```sql showcodeonly
rdpIter:{[tolerance;x;y]
  // Boolean list tracks data points to keep after each step
  remPoints:count[x]#1b;

  // Dictionary to track subsections that require analysis
  // Begin with the input itself
  subSections:enlist[0]!enlist count[x]-1;

  // Pass the initial state into the iteration procedure which will 
  // keep track of the remaining data points 
  res:iter[tolerance;;x;y]/[(subSections;remPoints)];

  // Apply the remaining indexes to the initial curve
  (x;y)@\:where res[1] }

iter:{[tolerance;tracker;x;y]
  // Tracker is a pair, the dictionary of subsections and 
  // the list of chosen datapoints
  subSections:tracker[0];
  remPoints:tracker[1];

  // Return if no subsections left to analyze
  if[not count subSections;:tracker];

  // Pop the first pair of points off the subsection dictionary 
  sIdx:first key subSections;
  eIdx:first value subSections;
  subSections:1_subSections;

  // Use the start and end indexes to determine the subsections 
  subX:x@sIdx+til 1+eIdx-sIdx;
  subY:y@sIdx+til 1+eIdx-sIdx;

  // Calculate perpendicular distances
  d:pDist[first subX;first subY;last subX;last subY;subX;subY]; 
  ind:first where d = max d;
  $[tolerance < d ind;

    // Perpendicular distance is greater than tolerance 
    // => split and append to the subsection dictionary
    [subSections[sIdx]:sIdx+ind;subSections[sIdx+ind]:eIdx];

    // else discard intermediate points
    remPoints:@[remPoints;1+sIdx+til eIdx-sIdx+1;:;0b]]; 

  (subSections;remPoints) }
```

Taking the previous triangle wave function example once again, it may be demonstrated that the iterative version of the algorithm is not similarly bound by the maximum internal stack size:

```sql showcodeonly
q)triangle:sums 1,5000#-2 2
q)res:rdpIter[0.5;til count triangle;triangle]
q)res[1]~triangle
1b
```

# Results

## Cauchy random walk

We initially apply our algorithms to a random price series simulated by sampling from the Cauchy distribution which will 
provide a highly erratic and volatile test case. Our data sample is derived as follows:

```sql showcodeonly
PI:acos -1f
// Cauchy distribution simulator
rcauchy:{[n;loc;scale]loc + scale * tan PI * (n?1.0) - 0.5}
n:20000 // Number of data points
// Trade table with Cauchy distributed price series
trade:([] time:09:00:00.000+asc 20000?(15:00:00.000-09:00:00.000);
          sym:`AAA;
          price:abs 100f + sums rcauchy[20000;0.0;0.001] )
```

```sql type='timeseries' server='kdbserver'
PI:acos -1f;
// Cauchy distribution simulator
rcauchy:{[n;loc;scale]loc + scale * tan PI * (n?1.0) - 0.5};
n:20000; // Number of data points
// Trade table with Cauchy distributed price series
trade:([] time:09:00:00.000+asc 20000?(15:00:00.000-09:00:00.000);
          price:abs 100f + sums rcauchy[20000;0.0;0.001] );
`time xdesc trade
```    

For our initial test run we choose a tolerance value of 0.005. This is the threshold for the algorithm – 
where all points on a line segment are less distant than this value, they will be discarded. 
The tolerance value should be chosen relative to the typical values and movements in the price series.


```sql showcodeonly
// Apply the recursive version of the algorithm
q)\ts trade_recur:exec flip `time`price!rdpRecur[0.005;time;price] from trade
53 1776400

// Apply the iterative version of the algorithm
q)\ts trade_iter:exec flip `time`price!rdpIter[0.005;time;price] from trade
141 1476352
q)trade_recur ~ trade_iter
1b
q)count trade_simp
4770
```

The simplification algorithm has reduced the dataset from 20,000 to 4,770, a reduction of 76%. The modified chart is plotted.

```sql type='timeseries' server='kdbserver'
trade_iter:exec flip `time`price!rdpIter[0.005;time;price] from trade;
`time xdesc trade_iter
```

## Bitcoin vs USD Price

Now we apply the same transform to a real data set using BTCUSDT data from Kraken [Quarterly ZIP Files](https://support.kraken.com/hc/en-us/articles/360047124832-Downloadable-historical-OHLCVT-Open-High-Low-Close-Volume-Trades-data).
There are **130982 data points**.

```sql type='timeseries' server='kdbserver'
btc:select date,close from flip `date`open`high`low`close`volume`trades!("JFFFFFJ";",") 0: `:XBTUSD_1.csv;
btc:12100 sublist `date xdesc update date:1970.01.01+0D00:00:01*date from btc;
btc
```

A tolerance value of 100 – results in a 95% reduction in the number of data points with a 300ms runtime cost. 
The result plotted below, compares very favorably with the plot of the raw data. There are almost no perceivable visual differences.


```sql type='timeseries' server='kdbserver'
exec flip `time`price!rdpRecur[100;date;close] from btc
```

## Example of Various Tolerances

```sql type='timeseries' server='kdbserver' height='500px'
testRuns:{1!(`time,`$"close",string x) xcol exec flip `time`price!rdpRecur[x;date;close] from btc} each {x!x} reverse 100 200 300 400 500 600 800 1000;
tt:(uj/) testRuns;
`time xdesc (select time from tt),'flip cols[value tt]!(1+til[count a]*4000)+a:value flip value tt
```

```sql server='kdbserver' 
reverse {([] tolerance_SD_NUMBER0:key x; dataPoints_SD_NUMBER0:value x)} count each testRuns
```

# Conclusion

In this paper we have presented two implementations of the Ramer-Douglas-Peucker algorithm for curve simplification, 
applying them to financial time-series data and demonstrating that a reduction in the size of the dataset to 
make it more manageable does not need to involve data distortion and a corresponding loss of information about its overall trends and key features.

This type of data-reduction trades off an increased runtime cost on the server against a potentially large 
reduction in processing time on the receiving client.

While for many utilities simple bucket-based summaries are more than adequate and undeniably more performant, 
we propose that for some uses a more discerning simplification as discussed above can prove invaluable. 
This is particularly the case with certain time-series and combinations thereof where complex and volatile behaviors must be studied.

# Authors

Thanks to:
 - [Sean Keevey](https://www.linkedin.com/in/sean-keevey-672bb112/) 
 - [Kevin Smyth](https://www.linkedin.com/in/kevin-smyth-5a6a731a/)


This [SQL notebook](https://www.timestored.com/sqlnotebook/) was created using [QStudio](https://www.timestored.com/qstudio/) 
a Free SQL client and notebook tool provided by TimeStored since 2013.
Please see the [help docs](https://www.timestored.com/qstudio/help/sqlnotebook) to get started creating your own.
