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
	go func() { worklist <- flag.Args() }()

	// Crawl the web concurrently.
	seen := make(map[string]bool)
	for ; n > 0; n-- {
		list := <-worklist
		for _, link := range list {
			if !seen[link] {
				seen[link] = true
				n++
				go func(link string) {
					worklist <- crawl(link)
				}(link)
			}
		}
	}
	elapsed := time.Since(start)

	log.Printf("%d links processed in %s\n", len(seen), elapsed)
}