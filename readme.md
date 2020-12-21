Search packages in maven through console, sbt style dependency string can be copied to clipboard

## Installation

### Scoop

```bash
scoop install https://raw.githubusercontent.com/Deliganli/maven-search/master/scoop/mvns.json
```
### Binaries

https://github.com/Deliganli/maven-search/releases

latest can be downloaded and extracted it somewhere

## Usage

```bash
$ mvns cats
 [1] org.typelevel %%    cats-parse % 0.2-41-437af75
 [2] org.typelevel %%     cats-core %          2.3.1
 [3]  com.flowtick %%   graphs-cats %          0.5.0
 [4] org.typelevel %%  cats-testkit %          2.3.1
 [5] org.typelevel %%     cats-core %       2.3.0-M2
 [6] org.typelevel %%%   cats-tests %       0.6.0-M1
 [7] org.typelevel %%    cats-tests %       0.6.0-M1
 [8]    org.scodec %%   scodec-cats %       1.1.0-M4
 [9]    io.regadas %%     scio-cats %          0.1.3
Page:1
Select a number to copy to clipboard (1 - 9, n:next page):
``` 
