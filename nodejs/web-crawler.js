var http = require('http');
var cheerio = require('cheerio');


var visitedUrls = new Set()
var maxInProcessRequests = 20;
var inProcessRequests = 0;
var pendingUrls = []


function visit(url, k){
    inProcessRequests +=1
    http.get({
        host: 'localhost',
        port: 8080,
        path: url
    }, function(response) {
	parse(url, response, k)
    });
}

function parse(url, response, k){
    if( visitedUrls.has(url) ){
	inProcessRequests -= 1;
	k([]);
        return
    }
    visitedUrls.add(url)
    var body = '';
    response.on('data', function(chunk) {
        body += chunk;
    });
    response.on('end', function() {
	var links = []
        var linkElements = cheerio.load(body)('a')
        for (var index = 0; index < linkElements.length; index++) {
            var href = linkElements[index].attribs.href
            if( !visitedUrls.has(href) ){
                links.push(href)
            }
        }
	inProcessRequests -= 1;
	k(links)
    });
}

function kontinue(links){    
    Array.prototype.push.apply(pendingUrls, links);
    process.nextTick(function(){
	while ((0 < pendingUrls.length) && (inProcessRequests < maxInProcessRequests)){
	    url = pendingUrls.pop();
	    visit(url, kontinue);
	}
    });
    if (pendingUrls.length == 0 && inProcessRequests == 0){
	console.info("Done visitedUrls:", visitedUrls.size , "took:", Date.now() - startTime, "milliseconds");
    }
}

var startTime = Date.now()

visit("/0/index.html", kontinue)
