DROP TABLE IF EXISTS weather;
DROP TABLE IF EXISTS cities;
DROP TABLE IF EXISTS prices;
DROP TABLE IF EXISTS sales;

CREATE TABLE prices AS (
    SELECT '2001-01-01 00:16:00'::TIMESTAMP + INTERVAL (v) MINUTE AS ticker_time,
        v AS unit_price
    FROM range(0,5) vals(v)
);

create table sales(item text, sale_time timestamp, quantity int);
insert into sales values('a', '2001-01-01 00:18:00', 10);
insert into sales values('b', '2001-01-01 00:18:30', 20);
insert into sales values('c', '2001-01-01 00:19:00', 30);

CREATE TABLE weather (
    city           VARCHAR,
    temp_lo        INTEGER, -- minimum temperature on a day
    temp_hi        INTEGER, -- maximum temperature on a day
    prcp           REAL,
    date           DATE
);

INSERT INTO weather VALUES ('San Francisco', 46, 50, 0.25, '1994-11-27');
INSERT INTO weather VALUES ('San Francisco', 60, 70, 0.22, '2022-06-10');
INSERT INTO weather VALUES ('San Francisco', 44, 55, 0.33, '2023-01-01');
INSERT INTO weather (city, temp_lo, temp_hi, prcp, date)     VALUES ('San Francisco', 43, 57, 0.0, '1994-11-29');


CREATE TABLE cities(Country VARCHAR, Name VARCHAR, Year INT, Population INT);
INSERT INTO cities VALUES ('NL', 'Amsterdam', 2000, 1005);
INSERT INTO cities VALUES ('NL', 'Amsterdam', 2010, 1065);
INSERT INTO cities VALUES ('NL', 'Amsterdam', 2020, 1158);
INSERT INTO cities VALUES ('US', 'Seattle', 2000, 564);
INSERT INTO cities VALUES ('US', 'Seattle', 2010, 608);
INSERT INTO cities VALUES ('US', 'Seattle', 2020, 738);
INSERT INTO cities VALUES ('US', 'New York City', 2000, 8015);
INSERT INTO cities VALUES ('US', 'New York City', 2010, 8175);
INSERT INTO cities VALUES ('US', 'New York City', 2020, 8772);


/** SELECT * FROm information_schema.tables; **/
SELECT * FROM prices;
SELECT * FROM sales;


/** using ASOF, 18:30 "rounds down" to use the 18:00 unit_price **/
SELECT s.*, p.unit_price, s.quantity * p.unit_price AS total_cost
  FROM sales s ASOF LEFT JOIN prices p
    ON s.sale_time >= p.ticker_time;
    
/** PIVOTS *******************************************************/
SELECT * FROM Cities;
PIVOT Cities ON Year USING FIRST(Population) as POP,LIST(Population) as P;
PIVOT Cities on Country, Name USING SUM(Population);
PIVOT Cities ON Year USING SUM(Population) as total, MAX(Population) as max GROUP BY Country;

/** DUCKDB 0.10.0  -  Fixed length arrays *******************************************************/
CREATE TABLE vectors(v DOUBLE[3]);
INSERT INTO vectors VALUES ([1, 2, 3]);
SELECT array_cross_product(v, [1, 1, 1]) AS result FROM vectors;
