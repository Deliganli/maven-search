Search packages in maven through console, sbt style dependency string can be copied to clipboard

```bash
  mvns cats
```

```text
$ java -jar maven-search.jar cats
 [1]  org.typelevel :          cats-tests_2.10 : 0.6.0-M1
 [2]  org.typelevel :          cats-tests_2.11 : 0.6.0-M1
 [3]  org.typelevel :   cats-tests_sjs0.6_2.10 : 0.6.0-M1
 [4]  org.typelevel :   cats-tests_sjs0.6_2.11 : 0.6.0-M1
 [5]  org.typelevel :           cats-docs_2.10 : 0.6.0-M1
 [6]  org.typelevel :           cats-docs_2.11 : 0.6.0-M1
 [7]  org.typelevel :          cats-bench_2.10 : 0.6.0-M1
 [8]  org.typelevel :          cats-bench_2.11 : 0.6.0-M1
 [9] dev.profunktor : redis4cats-log4cats_2.12 :   0.11.0
Page:1
Select a number to copy to clipboard (1 - 9, n:next page):
``` 
