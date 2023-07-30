/ taken from bench.q update when bench.q is updated.

/ run various sizes of inserts for an in memory table and return report table.
runThroughputBenchmark:{ n:15000000;
    while[(t:rand`3) in key `.];
	t set ([] t:09:30t; s:`a; p:1.; v:1 1);
	cases:{(y div z; z#value x)}[t;n] each {x!x}`int$10 xexp til 6;
	r:{![x;();0b;`symbol$()]; s:.z.t; do[y 0;x insert y 1];.z.t-s}[t;] each cases;
	![`.;();0b;enlist t];
    rt:([] s:key r; t:value r);
    select batchSize:s,insertsPerSecond:n%t*1000 from rt};