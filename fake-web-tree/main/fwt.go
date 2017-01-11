package main

import (
	"fmt"
	"flag"
	"net/http"
	"regexp"
	"strconv"
	"html/template"
	"math"
	"time"
	"gopkg.in/tylerb/graceful.v1"
)

var re *regexp.Regexp
var treeTemplate *template.Template
var graphTemplate *template.Template
var graph  *bool
var depth *int
var port *int

func servTree(w http.ResponseWriter, r *http.Request) {
	if r.RequestURI == "/favicon.ico" {
		http.Error(w, `StatusNotFound`, http.StatusNotFound)
		return
	}
	var n int = 0
	n, _ = getRequestedNode(r.RequestURI)
	childrens := []int{2 * n, (2 * n) + 1}
	if 0 < *depth && *depth == int(math.Floor(math.Log2(float64(2 * n)))){
		childrens = []int{}
	}
	if *graph {
		graphTemplate.Execute(w, struct {
			Childes []int
			Current int
		}{childrens, n / 2})
	} else {
		treeTemplate.Execute(w, struct {
			Childes []int
			Current int
		}{childrens, n / 2})
	}
}

func getRequestedNode(url string) (int, error) {
	if url == "/index.html" {
		return 1, nil
	} else if match := re.FindStringSubmatch(url); 1 < len(match) {
		return strconv.Atoi(match[1])
	} else {
		return 1, nil
	}
}

func main() {
	graph = flag.Bool("graph", false, "create graph instead of tree")
	depth = flag.Int("depth", 0, "detairmain the max node id that should return")
	port = flag.Int("port", 8080, "http port")

	flag.Parse()
	if *graph {
		fmt.Println("graph mode is on")
	} else {
		fmt.Println("graph mode is off")
	}

	if 0 < *depth {
		fmt.Printf("max depth is %d\n", *depth)
	} else {
		fmt.Println("depth is unlimited")
	}
	fmt.Printf("configured port is %d\n", *port)

	re = regexp.MustCompile("/([0-9]+)/index.html")
	treeTemplate = template.New("tree template")
	treeTemplate.Parse(`
<html>
<head></head>
<body>
    <ul>
    {{range $index, $element := .Childes}}
    <li>
       <a href="/{{$element}}/index.html">{{$element}}</a>
    </li>
    {{end}}
    </ul>
</body>
</html>`)

	graphTemplate = template.New("graph template")
	graphTemplate.Parse(`
<html>
<head></head>
<body>
    <ul>
    {{range $index, $element := .Childes}}
    <li>
       <a href="/{{$element}}/index.html">{{$element}}</a>
    </li>
    {{end}}
    </ul>
     <a href="/{{.Current}}/index.html">back</a>
</body>
</html>`)
	fmt.Println("starting web server")
	mux := http.NewServeMux()
	mux.HandleFunc("/", servTree)
	graceful.Run(fmt.Sprintf(":%d", *port),10*time.Second,mux)
	//err := http.ListenAndServe(fmt.Sprintf(":%d", port), mux)
	//if err != nil {
	//	fmt.Printf("Error: %v\n", err)
	//	return
	//}

}
