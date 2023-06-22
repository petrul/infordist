# Steps to compute similarity metrics between English vocabulary words.

### Download the wikipedia XML dump:

```
$ wget -c http://mirror.accum.se/mirror/wikimedia.org/dumps/enwiki/20230601/enwiki-20230601-pages-articles.xml.bz2
$ bunzip2 enwiki-20230601-pages-articles.xml.bz2
```

### Build the software  

$ ./gradle build  

Locate the distribution package in build/lib/indexer_app-VERSION.zip, get it, unzip it. It has a bin/ 
subdirectory with some useful tools in it. Most of them have a help -h switch which prints out available CLI
options.

### Run WikipediaIndexer

```
$ cd indexer_app-1.1-SNAPSHOT/bin
$ ./wikipedia-indexer -x enwiki-20230601-pages-articles.xml
```

The run may take a while, the end is marked by printing something like: 

```indexed 20 million pages.```

### Apply NGD:

One of the options is -n which specifies for how many words of the English vocabulary
you want the computation to be done. Use something between 30000 - 50000 knowing that the more
words you need the more time and memory will be needed for the computation to finish.

Run, for example, like this:

```bash
$ ./ExtractTermFrequenciesMatrixFromPositionalIndex -n 40000 -i index-enwiki -o ngd-40k 
``` 
This command will generate you a term matrix which you can inspect to get NGD-based similarity
semantic neighbourhoods.

The options mean:

* -n 40000 : take into account no more than 40k words of the English vocabulary
* -i index-enwiki: the positional indexed realized by the previous step
* -o the location of the output binary cooccurrence matrix.

### NCD

To apply NCD (I called it nld, normalized 'lucene' distance), first get a directory full of gists
(that is windows of meaning for each term)

```
$ ./RetrieveGistsFromPositionalIndex
```

it will output something like:

```
    got 60001 terms, will store them in /Users/petru/gists.bz2...
    calculating gist for refer(5368835) ...
    calculating gist for name(3589360) ...
    calculating gist for also(3149067) ...
    calculating gist for which(2434352) ...
    retrieving gist of refer from index...
    retrieving gist of name from index...
    retrieving gist of which from index...
    calculating gist for other(2407251) ...
    retrieving gist of other from index...
    term which appeared 8084038 times in 2434352 docs, has a gist size in contexts of 8084038, gist retrieval took 0:27:18.450
    term other appeared 6367092 times in 2407251 docs, has a gist size in contexts of 6367092, gist retrieval took 0:26:58.600
```

then run ncd:

$ groovy ncd_for_dir -i ~/Desktop/gists.bz2/

the script may take a long time.

### BUGS
If you get 

```
[Fatal Error] :60490871:185: JAXP00010004: The accumulated size of entities is "50,000,001" that exceeded the "50,000,000" limit set by "FEATURE_SECURE_PROCESSING".
Exception in thread "main" java.lang.RuntimeException: org.xml.sax.SAXParseException; lineNumber: 60490871; columnNumber: 185; JAXP00010004: The accumulated size of entities is "50,000,001" that exceeded the "50,000,000" limit set by "FEATURE_SECURE_PROCESSING".
	at wiki.indexer.cli.WikipediaIndexer.run(WikipediaIndexer.java:126)
	at wiki.indexer.cli.WikipediaIndexer.main(WikipediaIndexer.java:205)
Caused by: org.xml.sax.SAXParseException; lineNumber: 60490871; columnNumber: 185; JAXP00010004: The accumulated size of entities is "50,000,001" that exceeded the "50,000,000" limit set by "FEATURE_SECURE_PROCESSING".
	at java.xml/com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser.parse(AbstractSAXParser.java:1243)
	at wiki.indexer.cli.WikipediaIndexer.run(WikipediaIndexer.java:114)
```
Fix it like this :
https://stackoverflow.com/questions/42991043/error-xml-sax-saxparseexception-while-parsing-a-xml-file-using-wikixmlj

-DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000


# SPECS

NCD-Similarity

## API

Three components.

1. storage (a TermMatrix)
2. a repo ( a directory of bzipped files, more generally an iterator)
3. a compressor (bzip2, gzip etc)


So:
```
ncd_calculator = new BZip2 NcdCalculator

storage = ... TermMatrix.getInstance(ncdcalculator)

repo = new NCDRepo( calculator, directory, matrix)

ncdrepo.getNeighbours(term)

ncdrepo.addItem(String name, InputStream inputstream)
ncdrepo.addItem(String name, File file)
ncdrepo.addItem(String item)


ncdrepo.addItems(new DirectoryOfBzipped(...))

ncdrepo.getDistance(term1, term2)

ncdrepo.getDistances(term1)
ncdrepo.getNearestNeighbours(term1, 20)

ncrepo.computeAllDistances()


String s1 = ...

String s2 = ...


ncdcalculator.computeNCD(s1, s2)

ncdrepo.getNCD(s1, s2)
```