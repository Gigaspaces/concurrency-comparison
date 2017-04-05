# concurrency comperison
 
## The Problem Domain
   We will write the same app in a variety of programming languages so we can compare and contrast different implementations of the same program.
   The program we chose is a web crawler, its purpose is to go through an html page and for every link in the page get the linked page and go through it and so on and so on..
   All resources mentioned in this blog can be accessed from our repository: 
   https://github.com/Gigaspaces/concurrency-comparison


## The Testing Tool
   In order to test the results of each implementation we created the fake web tree, a go web server that generates a tree of pages of a requested depth on demand.
   Deploying the fake web tree:
   In order to run the tool you need to have the go language installed on your computer, 
   Then build the project by running fake-web-tree/build.sh
   Now run fake-web-tree/bin/fake-web-tree.sh -depth=16  
   (we use depth 16 for our benchmarking but you can create a graph of any size) 
   To start a docker with the fake-web-tree running on it run the script  ./fake-web-tree/docker/graph.sh

## The Basic java implementation:
This implementation uses a thread pool to execute the task of extracting links  from a given page and the ‘seen’ set is used for memoization.


```java
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
```

The java implementation is straightforward and you don’t have to handle any new technology. 
However the code is verbose and  every time we write a synchronized code the chances of bugs increases.
