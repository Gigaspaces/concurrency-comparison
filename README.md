# concurrency-comparison

Content:

1. fake-web-tree -- an go web server that generate tree of pages on demand, this program is used to benchmark the crawlers.
2. golang/web-crawler -- a web crawler written in golang.
3. nodejs/web-crawler.js -- a web crawler written in nodejs.
4. akka/web-crawler -- the same web crawler written using akka actors.
5. javaRX/web-crawler -- the same web crawler written java reactive programming library.
6. java/web-crawler --  the same web crawler written java vanilla.

# How to build
From the cmd run the script ./build.sh


# How to run

* First you will need to run the web server that the banchmark is running against.
  Open shell in the `fake-web-tree/bin` directory and run the cmd `./fake-web-tree -depth=16`
  keep it running as long as you intend to run benchmakr against it.

* To benchmark the go crawler open shell in the directory `golang/web-crawler/bin` and run `./web-crawler  http://localhost:8080`
On my machine the output is:

```Bash

2017/01/12 09:52:40 using 20 goroutines to process ["http://localhost:8080"]
2017/01/12 09:52:44 65535 links processed in 3.867911046s

```
* To benchmark the nodejs crawler open shell in the directory `nodejs` and run `nodejs web-crawler.js`
On my machine the output is:

```Bash

Done visitedUrls: 65536 took: 30315 milliseconds

```


* To benchmark the Java crawler open shell in the directory `java/web-crawler` and run `./run20.sh`
On my machine the output is:

```Bash

Using 20 threads to process http://localhost:8080
65535 links processed in 5.076s

```
* To benchmark the Java RX crawler open shell in the directory `javaRX/web-crawler` and run `./run.sh`
On my machine the output is:

```Bash

 [org.gigaspaces.demo.CrawlersDemo.main()] *** Testing WebCrawler with http://localhost:8080 ***
 [Crawler-11] Completed - duration = 5001
 

```


```
* To benchmark the Akka crawler open shell in the directory `akka/web-crawler` and run `./run.sh`
On my machine the output is:

```Bas

 [org.gigaspaces.demo.CrawlersDemo.main()] *** Testing WebCrawler with http://localhost:8080 ***
 [Crawler-11] Completed - duration = 5001
 

```







