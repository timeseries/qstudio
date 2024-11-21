# QuestDB trades demo notebook


* **QuestDB** is a fast open source time-series database with SQL extensions designed for finance.
* **qStudio** has [excellent support for QuestDB](https://www.timestored.com/qstudio/database/questdb)
* The dataset shown in this notebook, with live demo queries, can be found at https://demo.questdb.io

## The trades dataset

The trades dataset tracks trades for different symbols using the OKX API. There are typically several
entries per second. 
 
This query shows the 20 most recent trades at the moment of taking the data snapshot for this notebook:
 
```sql type="grid" server="qdb"
SELECT * from trades limit -20;   
```

The demo dataset has over 1 billion records, but in this notebook we are exploring data only since
yesterday. There are 7,334,791 rows for yesterday and today (until the snapshot was taken)


```sql type="bar" server="qdb"
SELECT timestamp, count() 
FROM trades  
WHERE timestamp IN yesterday() OR timestamp in today()
SAMPLE BY 1d;   
```

We can use the `LATEST` syntax to find the most recent row for each symbol

```sql type="grid" server="qdb"
SELECT * from trades LATEST ON timestamp PARTITION BY symbol;   
```


## OHLC with 15-minute candles

By using QuestDB `SAMPLE BY` SQL extension, we can easily sample data at
any desired interval and calculate, for example, the OHLC at 15 minutes 
intervals.

```sql type="candle" server="qdb"

SELECT 
    timestamp, symbol,
    first(price) AS open,
    last(price) AS close,
    min(price) as low,
    max(price) as high,
    sum(amount) AS volume
FROM trades
WHERE symbol = 'BTC-USDT' 
AND timestamp >=  interval_start(yesterday())
SAMPLE BY 15m;
```

## Calculating VWAP 

Since QuestDB supports standard SQL plus extensions, we can combine sampling
into 10-minute buckets with cumulative operations using window functions to get,
for example, the cumulative VWAP for yesterday's trades of 'BTC-USDT', 
taken at 10-minute intervals.

```sql type="timeseries" server="qdb"
WITH btc_usdt AS (
    SELECT 
          timestamp,  symbol, 
          SUM(amount) AS volume, 
          SUM(price * amount) AS traded_value
     FROM trades
     WHERE timestamp IN yesterday()
     AND symbol = 'BTC-USDT'
     SAMPLE BY 10m
), cumulative AS ( 
     SELECT timestamp, symbol, 
           SUM(traded_value)
                OVER (ORDER BY timestamp) AS cumulative_value,
           SUM(volume)
                OVER (ORDER BY timestamp) AS cumulative_volume
     FROM btc_usdt
)
SELECT timestamp, cumulative_value/cumulative_volume AS vwap FROM cumulative;    
```


