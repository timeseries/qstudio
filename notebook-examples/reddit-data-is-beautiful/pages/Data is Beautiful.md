# r/dataisbeautiful

r/dataisbeautiful, also known as Data Is Beautiful, is a subreddit dedicated to aesthetically pleasing works of data visualization.
It was created in 2012; as of January 2022, it has over 20 million members.

## Search Trends

Based by this [posting](https://www.reddit.com/r/dataisbeautiful/comments/zqzqfw/2022_in_search_trends_oc/).

```sql server='QDUCKDB' type='timeseries' height='400px' overrideJson={{grid:{top:70}}}
SELECT * FROM READ_PARQUET('https://www.timestored.com/data/sample/pq/search_trends.parquet') ORDER BY Week DESC; 
```



## Japan's Population Problem, Visualized

```sql server='QDUCKDB' type='timeseries'
SELECT * FROM READ_PARQUET('https://www.timestored.com/data/sample/pq/japan_births_deaths.parquet') ORDER BY Year DESC; 
```

Based by this [posting](https://www.reddit.com/r/dataisbeautiful/comments/zqzqfw/2022_in_search_trends_oc/).

What is the « fire horse » superstition ?


It’s a belief that’s been going on since the late Edo period. There’s a story of this girl who fell in love and went crazy by starting a fire. She was burned at the stake for her crimes. There’s a memorial for her in Tokyo so she is an ongoing figure in folklore.

Well, she was born during the year of the fire horse which occurs once every 60 years.

Combine that with a few other stories over the years about fires that happened during “fire horse” years and you got yourself a long standing superstition.

Birth rates drop specifically on that year because the belief is that girls born during the fire horse will have bad luck and even be compelled to burn things or kill their husbands.



```sql server='QDUCKDB' type='stack_horizontal'  overrideJson={{grid:{top:70}}}  height='400px'
SELECT Age,-Males,Females FROM READ_PARQUET('https://www.timestored.com/data/sample/pq/japan_population.parquet'); 
```



##  More than half of Niger's population is aged under 15. 

```sql server='QDUCKDB' type='stack_horizontal'  overrideJson={{grid:{top:70}}} height='400px'
SELECT Age,-Males,Females FROM READ_PARQUET('https://www.timestored.com/data/sample/pq/niger_population.parquet'); 
```

Based by this [posting](https://www.reddit.com/r/dataisbeautiful/comments/10tmsve/more_than_half_of_nigers_population_is_aged_under/).

If you like to look at pyramids like this, https://www.populationpyramid.net/ is a good resource, rather than relying on wikipedia's file uploads. 
As for Niger, it's similar to many other countries that are growing in population, this will eventually form the top of the pyramid you see today for many developed countries.

Check out [Cambodia](https://www.populationpyramid.net/cambodia/2022/). 
There's a huge drop off at the age of 40-49. That's not due to the adults who died during during Cambodian Genocide in the 1970s (they're the 50+ age range), rather, it's the babies who weren't born because women weren't healthy enough to conceive under the Khmer Rouge.
You can also see the massive difference in men vs. women 50+. While about 25% of Cambodians died in the genocide, it was 15% of the women and 33% of the men.


##  The cost of Christmas 

Based by this [posting](https://www.reddit.com/r/dataisbeautiful/comments/ztbovn/oc_the_cost_of_christmas_varies_widely_across_the/).

```sql server='QDUCKDB' type='stack_horizontal' height='500px'
SELECT Country,CostOfChristmas,-CostOfChristmas,Trunk,-Trunk,Star,-Star FROM READ_PARQUET('https://www.timestored.com/data/sample/pq/christmas_cost.parquet'); 
```


## Size of Bank Failures

Originally inspired by this [article](https://www.reddit.com/r/dataisbeautiful/comments/11p3555/oc_size_of_bank_failures_since_2000/).

```sql server='QDUCKDB' type='treemap'
SELECT * FROM READ_PARQUET('https://www.timestored.com/data/sample/pq/bank_failures.parquet'); 
```
