# Implementing trend indicators in kdb+

This article written by [James Galligan](https://www.linkedin.com/in/james-galligan-50629997/) demonstrates how to create trend indicators in kdb+. 
It uses BTCUSDT data from Kraken [Quarterly ZIP Files](https://support.kraken.com/hc/en-us/articles/360047124832-Downloadable-historical-OHLCVT-Open-High-Low-Close-Volume-Trades-data).
It was originally published as part of the [KX whitepaper](https://code.kx.com/q/wp/trend-indicators/) series and has 
been updated with new data and adapted to [sql notebooks](https://www.timestored.com/sqlnotebook/) format.

## Technical analysis

Technical analysis is the process of identifying trading opportunities based on past price movements using different stock charts. Trend/technical traders use a combination of patterns and indicators from price charts to help them make financial decisions. Investors analyze price charts to develop theories about what direction the market is likely to move. Commonly used in technical analysis tools are the Candlestick chart, Moving Average Convergence Divergence and Relative Strength Index.


# Load Data

Load the data accounting for the unix timestamp ([thanks Terry](https://stackoverflow.com/questions/67545086/how-does-one-parse-an-arbitrary-unix-timestamp-to-a-datetime-in-kdb)).

```sql showcodeonly
t:flip `date`open`high`low`close`volume`trades!("JFFFFFJ";",") 0: `:XBTUSDT_60.csv;
bitcoinKraken:update date:1970.01.01+0D00:00:01*date from t;
```

Each candle shows the high/open/close/low and if our security closed higher than the open. This can be useful in predicting short term price movements.

```sql type='candle' server='kdbserver'
bitcoinKraken
```

# Simple Moving Averages

The price of a security can be extremely volatile and large price movements can make it hard to pinpoint the general trend. 
Moving averages ‘smooth’ price data by creating a single flowing line. The line represents the average price over a period of time. 
Which moving average the trader decides to use is determined by the time frame in which he or she trades.

There are two commonly used moving averages: Simple Moving Average (SMA) and Exponential Moving Average (EMA). 
EMA gives a larger weighting to more recent prices when calculating the average. 
Below you can see the 10-day moving average and 20-day moving average along with the close price.

```sql showcodeonly
-150 sublist select date,close,sma10:mavg[10;close],sma20:mavg[20;close] from bitcoinKraken
```

```sql type='timeseries' server='kdbserver'
-150 sublist select date,close,sma10:mavg[10;close],sma20:mavg[20;close] from bitcoinKraken
```

# Moving Average Convergence Divergence

Moving Average Convergence Divergence (MACD) is an important and popular analysis tool. It is a trend indicator that shows the relationship between two moving averages of a securities price. MACD is calculated by subtracting the long-term EMA (26 periods) from the short-term EMA (12 periods).

A period is generally defined as a day but shorter/longer timespans can be used. Throughout this paper we will consider a period to be one day.

EMAs place greater weight and significance on the more recent data points and react more significantly to price movements than SMA. The 9-day moving average of the MACD is also calculated and plotted. This line is known as the signal line and can be used to identify buy and sell signals.

The code for calculating the MACD is very simple and exploits kdb+/q’s built-in function ema. An example of how the code is executed, along with a subset of the output is displayed.


```sql showcodeonly
// tab-table input
// id-ID you want `ETH_USD/BTC_USD
// ex-exchange you want
// output is a table with the close,ema12,ema26,macd,signal line calculated
macd:{[tab]
  macd:{[x] ema[2%13;x]-ema[2%27;x]}; /macd line
  signal:{ema[2%10;x]}; /signal line
  res:select date, close, 
      ema12:ema[2%13;close],
      ema26:ema[2%27;close],
      macd:macd[close] 
    from tab;
  update signal:signal[macd] from res }
```

```sql type='timeseries' server='kdbserver'
macd:{[tab]
  macd:{[x] ema[2%13;x]-ema[2%27;x]}; /macd line
  signal:{ema[2%10;x]}; /signal line
  res:select date, close, 
      ema12:ema[2%13;close],
      ema26:ema[2%27;close],
      macd:macd[close] 
    from tab;
  update signal:signal[macd] from res };
delete signal,macd from 40 sublist macd[bitcoinKraken]
```

```sql type='timeseries' server='kdbserver' height='150px'
select date,signal,macd from 40 sublist macd[bitcoinKraken]
```

From the above graph, you can see how the close price interacts with the short and long EMA and how this then 
impacts the MACD and signal-line relationship. There is a buy signal when the MACD line crosses over the signal 
line and there is a short signal when the MACD line crosses below the signal line.


# Relative Strength Index

Relative Strength Index (RSI) is a momentum oscillator that measures the speed and change of price movements. 
It oscillates between 0-100. It is said that a security is overbought when above 70 and oversold when below 30. 
It is a general trend and momentum indicator. The default period is 14 days. This can be reduced or increased – 
the shorter the period, the more sensitive it is to price changes. Short-term traders sometimes look at [2-day](https://school.stockcharts.com/doku.php?id=technical_indicators:relative_strength_index_rsi) 
RSIs for overbought readings above 80 and oversold ratings below 20.

```sql showcodeonly
//Relative strength index - RSI
//close-close price
//n-number of periods
relativeStrength:{[num;y]
  begin:num#0Nf;
  start:avg((num+1)#y);
  begin,start,{(y+x*(z-1))%z}\[start;(num+1)_y;num] };

rsiMain:{[close;n]
  diff:-[close;prev close];
  rs:relativeStrength[n;diff*diff>0]%relativeStrength[n;abs diff*diff<0];
  rsi:100*rs%(1+rs);
  rsi };
update rsi:rsiMain[close;14] from bitcoinKraken
```


```sql type='timeseries' server='kdbserver' height='150px'
-50 sublist select date,close from bitcoinKraken
```
```sql type='timeseries' server='kdbserver' height='150px'
//Relative strength index - RSI
//close-close price
//n-number of periods
relativeStrength:{[num;y]
  begin:num#0Nf;
  start:avg((num+1)#y);
  begin,start,{(y+x*(z-1))%z}\[start;(num+1)_y;num] };

rsiMain:{[close;n]
  diff:-[close;prev close];
  rs:relativeStrength[n;diff*diff>0]%relativeStrength[n;abs diff*diff<0];
  rsi:100*rs%(1+rs);
  rsi };
update up:70.,low:30. from -50 sublist select date,rsi:rsiMain[close;14] from bitcoinKraken
```


<math xmlns="http://www.w3.org/1998/Math/MathML" display="block">
  <mi>RSI</mi>
  <mo>=</mo>
  <mn>100</mn>
  <mo>&#x2212;</mo>
  <mfrac>
    <mn>100</mn>
    <mrow>
      <mn>1</mn>
      <mo>+</mo>
      <mi>RS</mi>
    </mrow>
  </mfrac>
</math>

<math xmlns="http://www.w3.org/1998/Math/MathML" display="block">
  <mi>RS</mi>
  <mo>=</mo>
  <mfrac>
    <mrow> <mi>AverageGain</mi> </mrow> <mrow> <mi>AverageLoss</mi> </mrow>
  </mfrac>
</math>


 - The first calculation of the average gain/loss are simple 14-day averages.
 - First Average Gain: sum of Gains over the past 14 days/14
 - First Average Loss: sum of Losses over the past 14 days/14
 - The subsequent calculations are based on the prior averages and the current gain/loss.
 
 <math xmlns="http://www.w3.org/1998/Math/MathML" display="block">
  <mi>A</mi>
  <mi>v</mi>
  <mi>g</mi>
  <mi>G</mi>
  <mi>a</mi>
  <mi>i</mi>
  <mi>n</mi>
  <mo>=</mo>
  <mfrac>
    <mrow>
      <mo stretchy="false">(</mo>
      <mi>prevAvgGain</mi>
      <mo stretchy="false">)</mo>
      <mo>&#xD7;</mo>
      <mn>13</mn>
      <mo>+currentGain</mo>
    </mrow>
    <mn>14</mn>
  </mfrac>
</math>

# Money Flow Index

Money Flow Index (MFI) is a technical oscillator similar to RSI but which instead uses price and volume for identifying overbought
 and oversold conditions. This indicator weighs in on volume and not just price to give a relative score. 
 A low volume with a large price movement will have less impact on the relative score compared to a high volume move with a lower price move.

You see new highs/lows and large price swings but also if there is a price swing whether there is any volume behind the move
 or if it is just a small trade. The market will generally correct itself. It can be used to spot divergences that warn traders 
 of a change in trend. MFI is known as the [volume-weighted RSI](https://school.stockcharts.com/doku.php?id=technical_indicators:money_flow_index_mfi).

We use the ``relativeStrength`` function as in the RSI calculation above.

```sql showcodeonly
mfiMain:{[h;l;c;n;v]
  TP:avg(h;l;c);                    / typical price
  rmf:TP*v;                         / real money flow
  diff:deltas[0n;TP];               / diffs
  /money-flow leveraging func for RSI
  mf:relativeStrength[n;rmf*diff*diff>0]%relativeStrength[n;abs rmf*diff*diff<0];
  mfi:100*mf%(1+mf);                /money flow as a percentage
  mfi };
update mfi:mfiMain[high;low;close;14;volume] from bitcoinKraken
```


```sql server='kdbserver' type='timeseries'
-100 sublist delete volume,trades from bitcoinKraken
```
```sql server='kdbserver' type='timeseries' height='150px'
mfiMain:{[h;l;c;n;v]
  TP:avg(h;l;c);                    / typical price
  rmf:TP*v;                         / real money flow
  diff:deltas[0n;TP];               / diffs
  /money-flow leveraging func for RSI
  mf:relativeStrength[n;rmf*diff*diff>0]%relativeStrength[n;abs rmf*diff*diff<0];
  mfi:100*mf%(1+mf);                /money flow as a percentage
  mfi };
-100 sublist select date,mfi from update mfi:mfiMain[high;low;close;14;volume] from bitcoinKraken
```
### RSI vs MFI
Analysts use both RSI and MFI together to see whether a price move has volume behind it.
```sql server='kdbserver' type='timeseries' height='150px'
-100 sublist select date,rsi:rsiMain[close;6],mfi:mfiMain[high;low;close;6;volume] from bitcoinKraken
```


# Commodity Channel Index

The Commodity Channel Index (CCI) is another tool used by technical analysts. Its primary use is for spotting new trends. 
It measures the current price level relative to an average price level over time. The CCI can be used for any market, 
not just for commodities. It can be used to help identify if a security is approaching overbought and oversold levels. 
Its primary use is for spotting new trends. This can help traders make decisions on trades whether to add to position,
 exit the position or take no part.

When CCI is positive it indicates it is above the historical average and when it is negative it indicates it is below
 the historical average. Moving from negative ratings to high positive ratings can be used as a signal for a possible 
 uptrend. Similarly, the reverse will signal downtrends. CCI has no upper or lower bound so finding out what typical 
 overbought and oversold levels should be determined on each asset individually looking at its [historical CCI levels](https://www.tradingview.com/wiki/Commodity_Channel_Index_(CCI)).
 
 
 <math xmlns="http://www.w3.org/1998/Math/MathML" display="block">
  <mi>CCI</mi>
  <mo>=</mo>
  <mfrac>
    <mrow>
      <mi>TypicalPrice</mi>
      <mo>&#x2212;</mo>
      <mi>MovingAverage</mi>
    </mrow>
    <mrow>
      <mn>.015</mn>
      <mo>&#xD7;</mo>
      <mi>MeanDeviation</mi>
    </mrow>
  </mfrac>
</math>
 
 <math xmlns="http://www.w3.org/1998/Math/MathML" display="block">
  <mi>TypicalPrice</mi>
  <mo>=</mo>
  <mfrac>
    <mrow>
      <mi>high+low+close</mi>
    </mrow>
    <mn>3</mn>
  </mfrac>
</math>


To calculate the Mean Deviation, a helper function called maDev (moving-average deviation).

This was calculated by subtracting the Moving Average from the Typical Price for the last n periods, summing the absolute values of these figures and then dividing by n periods.

```sql showcodeonly
maDev:{[tp;ma;n]
  ((n-1)#0Nf),
    {[x;y;z;num] reciprocal[num]*sum abs z _y#x}'
    [(n-1)_tp-/:ma; n+l; l:til count[tp]-n-1; n] }
    
CCI:{[high;low;close;ndays]
  TP:avg(high;low;close);
  sma:mavg[ndays;TP];
  mad:maDev[TP;sma;n];
  reciprocal[0.015*mad]*TP-sma }
```

```sql server='kdbserver' type='timeseries'  
-100 sublist delete volume,trades from bitcoinKraken
```
```sql server='kdbserver' type='timeseries' height='150px'
maDev:{[tp;ma;n]
  ((n-1)#0Nf),
    {[x;y;z;num] reciprocal[num]*sum abs z _y#x}'
    [(n-1)_tp-/:ma; n+l; l:til count[tp]-n-1; n] };
    
CCI:{[high;low;close;ndays]
  TP:avg(high;low;close);
  sma:mavg[ndays;TP];
  mad:maDev[TP;sma;ndays];
  reciprocal[0.015*mad]*TP-sma };
-100 sublist select date,cci:CCI[high;low;close;14] from bitcoinKraken
```


# Bollinger Bands

[Bollinger Bands](https://www.investopedia.com/articles/technical/102201.asp) are used in technical analysis 
for pattern recognition. They are formed by plotting two lines 
that are two standard deviations from the simple moving-average price, one in the negative direction and one positive.

Standard deviation is a measure of volatility in an asset, so when the market becomes more volatile the bands
 widen. Similarly, less volatility leads to the bands contracting. If the prices move towards the upper band 
 the security is seen to be overbought and as the prices get close to the lower bound the security is 
 considered oversold. This provides traders with information regarding price volatility. 90% of
  price action occurs between the bands. A breakout from this would be seen as a major event. 
  The breakout is not considered a trading signal. Breakouts provide no clue as to the 
  direction and extent of future price movements.

```sql showcodeonly
//tab-input table
//n-number of days
//ex-exchange
//id-id to run for
bollB:{[tab;n;ex;id]
  tab:select from wpData where sym=id,exch=ex;
  tab:update sma:mavg[n;TP],sd:mdev[n;TP] from update TP:avg(high;low;close) from tab;
  select date,sd,TP,sma,up:sma+2*sd,down:sma-2*sd from tab}
```
```sql server='kdbserver' type='timeseries'  
bollB:{[tab;n]
  tab:update sma:mavg[n;TP],sd:mdev[n;TP] from update TP:avg(high;low;close) from tab;
  select date,sd,TP,sma,up:sma+2*sd,down:sma-2*sd from tab};
-100 sublist delete sd from bollB[bitcoinKraken;20]
```



# Ease of Movement Value

Ease of Movement Value (EMV) is another technical indicator that combines momentum and volume information into one value. 
The idea is to use this value to decide if the prices are able to rise or fall with little resistance in directional movement.


```sql showcodeonly
//Ease of movement value -EMV
//h-high
//l-low
//v-volume
//s-scale
//n-num of periods
emv:{[h;l;v;s;n]
  boxRatio:reciprocal[-[h;l]]*v%s;
  distMoved:deltas[0n;avg(h;l)];
  (n#0nf),n _mavg[n;distMoved%boxRatio] }
```

```sql server='kdbserver' type='candle' height='200px'  
-100 sublist bitcoinKraken
```
```sql server='kdbserver' type='timeseries' height='150px'
emv:{[h;l;v;s;n]
  boxRatio:reciprocal[-[h;l]]*v%s;
  distMoved:deltas[0n;avg(h;l)];
  (n#0nf),n _mavg[n;distMoved%boxRatio] };
  
-100 sublist select date,EMV:emv[high;low;volume;1000000;14]  from bitcoinKraken
```


# Rate of Change

The [Rate of Change (ROC)](https://therobusttrader.com/rate-of-change-indicator-roc/) indicator measures the percentage change in the close price over a specific period of time.

<math xmlns="http://www.w3.org/1998/Math/MathML" display="block">
  <mi>ROC</mi>
  <mo>=</mo>
  <mfrac>
    <mrow>
      <mi>Close</mi>
      <mo>&#x2212;</mo>
      <mi>CloseNDaysago</mi>
    </mrow>
    <mrow>
      <mi>CloseNDaysago</mi>
    </mrow>
  </mfrac>
  <mo>&#x2217;</mo>
  <mn>100</mn>
</math>


```sql showcodeonly
//Price Rate of change Indicator (ROC)
//c-close
//n-number of days prior to compare
roc:{[c;n]
  curP:_[n;c];
  prevP:_[neg n;c];
  (n#0nf),100*reciprocal[prevP]*curP-prevP }
```

```sql server='kdbserver' type='candle' height='200px'  
-100 sublist bitcoinKraken
```
```sql server='kdbserver' type='timeseries' height='150px'
roc:{[c;n]
  curP:_[n;c];
  prevP:_[neg n;c];
  (n#0nf),100*reciprocal[prevP]*curP-prevP };
  
-100 sublist update zero:0 from select date,ROC:roc[close;10]  from bitcoinKraken
```



# Aroon Oscillator

The Aroon Indicator is a technical indicator used to identify trend changes in the 
price of a security and the strength of that trend, which is used in the [Aroon Oscillator](https://www.investopedia.com/terms/a/aroonoscillator.asp). 
An Aroon Indicator has two parts: and , which measure the time between highs and
 lows respectively over a period of time , generally 25 days. The objective of the 
 indicator is that strong uptrends will regularly see new highs and strong downtrends 
 will regularly see new lows. The range of the indicator is between 0-100.
 



```sql showcodeonly
 //Aroon Indicator
aroonFunc:{[c;n;f]
  m:reverse each a _'(n+1+a:til count[c]-n)#\:c;
  #[n;0ni],{x? y x}'[m;f] }

aroon:{[c;n;f] 100*reciprocal[n]*n-aroonFunc[c;n;f]}

/- aroon[tab`high;25;max]-- aroon up
/- aroon[tab`low;25;max]-- aroon down
aroonOsc:{[h;l;n] aroon[h;n;max] - aroon[l;n;min]}
```

```sql server='kdbserver' type='timeseries' height='150px'
aroonFunc:{[c;n;f]
  m:reverse each a _'(n+1+a:til count[c]-n)#\:c;
  #[n;0ni],{x? y x}'[m;f] };
aroon:{[c;n;f] 100*reciprocal[n]*n-aroonFunc[c;n;f]};
aroonOsc:{[h;l;n] aroon[h;n;max] - aroon[l;n;min]};

-100 sublist select date,aroonUp:aroon[high;25;max],
    aroonDown:aroon[low;25;min], aroonOsc:aroonOsc[high;low;25]
        from bitcoinKraken
```

Aroon Oscillator subtracts from making the range of the oscillator between -100 and 100.

# Conclusion

This touches the tip of the iceberg of what can be done in analytics and emphasizes the power of kdb+ in a
 data-analytics solution. Libraries of custom-built analytic functions can be created with ease, and in a 
 short space of time applied to realtime and historical data.
 
 
# Author

[James Galligan](https://www.linkedin.com/in/james-galligan-50629997/) is a kdb+ expert who has designed and developed data-capture and data-analytics platforms 
for trading and analytics across multiple asset classes in multiple leading financial institutions. 

This article was originally published as part of the [KX whitepaper](https://code.kx.com/q/wp/trend-indicators/) series and has 
been updated with new data and adapted to [sql notebooks](https://www.timestored.com/sqlnotebook/) format.
This notebook is a snapshot which allows statically capturing a notebook to stored JSON rather than querying live.
IF you would like to create one similar please see https://www.timestored.com/sqlnotebook/
