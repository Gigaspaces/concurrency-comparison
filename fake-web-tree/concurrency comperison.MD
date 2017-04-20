# Concurrency Comparison
   All resources mentioned in this blog can be accessed from our repository: 
   <https://github.com/Gigaspaces/concurrency-comparison>
## The Problem Domain
   We will write the same app in a variety of programming languages so we can compare and contrast different implementations of the same program.
  
   The program we chose is a web crawler, its purpose is to go through an html page and for every link in the page get the linked page and go through it and so on. The most potentially time consuming part of the execution is the http get request for a given page, also the task of parsing and extracting the links from the html page is worth paralleling to increase our performance.
   
## The Testing Tool
   In order to test the results of each implementation we created the fake web tree, a go web server that generates a tree of pages of a requested depth on demand.
   Deploying the fake web tree:
   In order to run the tool you need to have the go language installed on your computer, then build the project by running fake-web-tree/build.sh and run fake-web-tree/bin/fake-web-tree.sh -depth=16
   (we use depth 16 for our benchmarking but you can create a graph of any size) 
   To start a docker with the fake-web-tree running on it, run the script  ./fake-web-tree/docker/graph.sh.

## The Basic java implementation:
This implementation uses a thread pool to execute the task of extracting links  from a given page and the ‘seen’ set is used for memoization.


```java
public class Crawler {
private void handle(final String link) {        
   if (seen.containsKey(link))
       return;
   seen.put(link, true);
   pending.incrementAndGet();
   executorService.execute(new Runnable() {
       public void run() {
           List<String> links = getLinksFromUrl(link);
           for (String link : links) {
               handle(link);
           }
           pending.decrementAndGet();
           if (pending.get() == 0) {
               synchronized (lock) {
                   lock.notify();
               }
           }
       }
   });
}
}
public class Main{

public static void main(String[] args) throws InterruptedException {
    if (args.length != 2) {
        System.err.println("Invalid syntax: <baseUrl> <numOfThreads>");
        System.exit(1);
    }

    String baseUrl = args[0];
    int numOfThreads = Integer.parseInt(args[1]);
    System.out.println("Using " + numOfThreads + " threads to process " + baseUrl);

    Crawler crawler = new Crawler(baseUrl, numOfThreads);
    long start = System.currentTimeMillis();
    crawler.start();
    crawler.join();
    long totalTimeInMS = System.currentTimeMillis() - start;
    crawler.shutdown();

    System.out.println(crawler.getSeenLinks() + " links processed in " + (double) totalTimeInMS / 1000 + "s");
}
}
```
    output: 
       Using 20 threads to process http://localhost:8080
       65537 links processed in 6.584s


The java implementation is straightforward and dose'nt involve learning any new technology, however, 
the code is verbose and  every time we write synchronized code the chances of bugs increases.


## The RxJava implementation

RxJava is a Java VM implementation of ReactiveX : 
a library for composing asynchronous and event-based programs by using observable sequences.

In ReactiveX an observer subscribes to an Observable. 
Then that observer reacts to whatever item or sequence of items the Observable emits.
This pattern facilitates concurrent operations because it does not need to block while waiting 
for the Observable to emit objects, but instead it creates a guard in the form of an observer that stands ready 
to react appropriately at whatever future time the Observable does so.

from the application context we use it like this:

```java
public class CrawlersDemo {
    
private static void demoWebCrawler(String url) throws InterruptedException {
   Crawler crawler = new WebCrawler(url);
   CrawlerClient crawlerClient = new CrawlerClient(crawler, "/" , 20);
   crawlerClient.waitForCompletion();
}
}
```
   output:
       [main] *** Testing WebCrawler with http://localhost:8080 *** 
       [Crawler-12] Completed - duration = 6.43s
       
The client subscribe to the observable and blocks on  the completionLatch until completion

```java

public class CrawlerClient {
  
   public CrawlerClient(Crawler crawler, String url, int numOfThreads) {
       this.observable = ObservableCrawler.create(crawler, url, numOfThreads);
       observable.subscribe(this::onNext, this::onError, this::onCompleted);
   }

   public void waitForCompletion() throws InterruptedException {
       completionLatch.await();
   }
  
}
```

Here we are creating an Observable using a constructor which receives an implementation of Observable.OnSubscribe interface.
This implementation defines what action will be taken when a subscriber subscribes to the Observable, in our action,
if numOfThreads > 0, the processAsync method will be called.
When processAsync is called for some url a new task is submitted, the task calls the webCrawler crawl method which extracts 
the links and calls back processAsync for every such url.

```java
public class ObservableCrawler {
  
   private Subscriber<? super String> subscriber;
  
   public static Observable<String> create(Crawler crawler, String url, int numOfThreads) {
       ObservableCrawler o = new ObservableCrawler(crawler, numOfThreads);
       return Observable.create(subscriber -> {
           o.subscriber = subscriber;
           if (o.executorService == null) {
               o.process(url);
               subscriber.onCompleted();
           } else {
               o.processAsync(url);
           }
       });
   }
 
   private void processAsync(String url) {
          pendingTasks.incrementAndGet();
          executorService.submit(() -> {
              // If item is not unique, skip processing
              boolean isFirstTime =  results.add(url);
              if (isFirstTime) {
                  subscriber.onNext(url);
                  crawler.crawl(url, this::processAsync);
              }
              if (pendingTasks.decrementAndGet() == 0) {
                  subscriber.onCompleted();
                  executorService.shutdown();
              }
          });
   }
}
```

This implementation performs as well as the java implementation, this make sense because both use Thread Pool for concurrency.
 However the rx library handles all synchronization behind the scenes and all you have to do is implement the onNext/ onError/ onCompleted 
 interface with your business logic which leads to last errors in code.
Additional benefits of observable:
* **Observables Are Composable:** It is difficult to compose  Futures into one asynchronous execution flows
 (or impossible, since latencies of each request vary at runtime). ReactiveX Observables, on the other hand, 
 are intended for composing flows and sequences of asynchronous data.
* **Observables Are Flexible:** Observable is a single abstraction that can be used for the emission of single scalar values 
(as Futures do), sequences of values or even infinite streams.
* **Observables Are Less Opinionated:** ReactiveX is not biased toward some particular source of concurrency or asynchronicity. 


## The go implementation

Go is a general-purpose language developed by google and it was designed to get the most out of multicore and networked machines. 
It is strongly typed and garbage-collected and has explicit support for concurrent programming like goroutines and channels. 
A goroutine is a lightweight thread managed by the Go runtime and channels are a typed conduit through which you can send 
and receive values, by default sends and receives block until the other side is ready and they can also be buffered.

To run this example, make sure you have go installed, you can find all you need here: <https://golang.org/dl/>.
Then use golang/web-crawler/build.sh to build the example and golang/web-crawler/bin/web-crawler to run it.

```go
package main

import (
	"log"
	"github.com/barakb/web-crawler/links"
	"runtime"
	"time"
	"net/http"
	"flag"
)

var tokens chan struct{}

func crawl(url string) []string {
	//log.Println(url)
	tokens <- struct{}{} // acquire a token
	list, err := links.Extract(url)
	<-tokens // release the token

	if err != nil {
		log.Print(err)
	}
	return list
}

func main() {
	goroutinesPtr := flag.Int("goroutines", 20, "number of concurrent goroutines")
	flag.Parse()
	http.DefaultTransport.(*http.Transport).MaxIdleConnsPerHost = 100
	runtime.GOMAXPROCS(runtime.NumCPU())
	tokens =  make(chan struct{}, *goroutinesPtr)
	log.Printf("using %d goroutines to process %q\n", *goroutinesPtr, flag.Args())
	worklist := make(chan []string)
	var n int // number of pending sends to worklist

	start := time.Now()

	// Start with the command-line arguments.
	n++
	go func() { worklist <- flag.Args() }() // goroutine that blocks until the first url is fetched 

	// Crawl the web concurrently.
	seen := make(map[string]bool)
	for ; n > 0; n-- {
		list := <-worklist
		for _, link := range list {
			if !seen[link] {
				seen[link] = true
				n++
				go func(link string) {     //the goroutine
					worklist <- crawl(link)
				}(link)
			}
		}
	}
	elapsed := time.Since(start)

	log.Printf("%d links processed in %s\n", len(seen), elapsed)
}
```
    output:
        using 20 goroutines to process ["http://localhost:8080"]
        65535 links processed in 4.002921664s

flag is a support for basic command-line flag parsing, here we declare a int flag, goroutines, with a default value 20 and 
a short description.Once all flags are declared, call flag.Parse() to execute the command-line parsing and then use the 
flag directly.
_tokens_ is a channel that is intended to mutex the crawl method critical section and _worklist_ is a buffered channel
of lists of links to process. it also serves as a countdown latch that determines how many goroutines will be executed
simultaneously. The loop for link := range list receives values from the channel repeatedly until it is closed.

You can explore the links.Extract method in our project <https://github.com/Gigaspaces/concurrency-comparison>

So, we can see that channels allows goroutines to synchronize without explicit locks or condition variables and the performance 
of this implementation is significantly batter then the previous ones, that is because goroutines are much more efficient 
then java threads in memory consumption, setup,teardown and switching costs.

## The Node.js implementation

Node is an asynchronous event driven JavaScript runtime that is designed to build scalable network applications. 
Event-driven programming is an application flow that is determined by events or changes in state. 
Node is a single threaded runtime, it uses the EventEmitter as a central mechanism that listens for events and calls a 
callback function once an event has occurred and it is utilized in the 'http' module that we are using.

```javascript
var http = require('http');
var cheerio = require('cheerio');
var visitedUrls = new Set();
var maxInProcessRequests = 20;
var inProcessRequests = 0;
var pendingUrls = [];

function visit(url, k) {
    inProcessRequests += 1;
    http.get({
        host: 'localhost',
        port: 8080,
        path: url
    }, function (response) {
        parse(url, response, k)
    });
}

function parse(url, response, k) {
    if (visitedUrls.has(url)) {
        inProcessRequests -= 1;
        k([]);
        return
    }
    visitedUrls.add(url);
    var body = '';
    response.on('data', function (chunk) {
        body += chunk;
    });
    response.on('end', function () {
        var links = [];
        var linkElements = cheerio.load(body)('a');
        for (var index = 0; index < linkElements.length; index++) {
            var href = linkElements[index].attribs.href;
            if (!visitedUrls.has(href)) {
                links.push(href)
            }
        }
        inProcessRequests -= 1;
        k(links)
    });
}

function kontinue(links) {
    Array.prototype.push.apply(pendingUrls, links);
    process.nextTick(function () {
        while ((0 < pendingUrls.length) && (inProcessRequests < maxInProcessRequests)) {
            url = pendingUrls.pop();
            visit(url, kontinue);
        }
    });
    if (pendingUrls.length == 0 && inProcessRequests == 0) {
        console.info("Done visitedUrls:", visitedUrls.size, "took:", Date.now() - startTime, "milliseconds");
    }
}

var startTime = Date.now();
visit("/0/index.html", kontinue);
```
    Output:
        Done visitedUrls: 65536 took: 40103 milliseconds
        
 
The Kontinue method is the callback method for our http get requests, the Visit method helps us keep track of
open requests, while parse extracts the links and calls Kontinue.

This implementation performs a lot slower then all the others, this is because even though http requests are non blocking
(up to maxInProcessRequests) and the program can advanced in the meantime, all the parsing is performed single threaded and there for
cannot compete with the other implementation who utilize the quad core on my computer to complete the parsing task.

However, in contrast to thread-based networking which are very difficult to use, Node frees you from worries of dead-locks - there are no locks.


## The AKKA Implementation
In Akka each Actor  has an explicit message handler which does pattern matching to match on the incoming messages.

Dispatcher:
The event-based Dispatcher binds a set of Actors to a thread pool backed up by a BlockingQueue.

PinnedDispatcher:
Dedicates a unique thread for each actor passed in as reference. Served through its messageQueue.

each actor utilizes its dispatcher to handle the incoming requests.






actor, sender
router
dispatcher
forkjoin vs threadpool

## Final Thoughts

![final comperison](comperison results.png)





















































