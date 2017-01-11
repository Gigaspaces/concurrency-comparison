package org.gigaspaces.demo;

import org.gigaspaces.demo.crawlers.Crawler;
import org.gigaspaces.demo.crawlers.TestCrawler;
import org.gigaspaces.demo.crawlers.WebCrawler;
import org.gigaspaces.demo.rx.CrawlerClient;
import org.gigaspaces.demo.utils.Logger;

public class CrawlersDemo {
    public static void main(String[] args) throws InterruptedException {
        demoWebCrawler("http://localhost:8080");
        //demoTestCrawler();
    }

    private static void demoWebCrawler(String url) throws InterruptedException {
        Logger.log("*** Testing WebCrawler with " + url + " ***");
        Crawler crawler = new WebCrawler(url);
        CrawlerClient crawlerClient = new CrawlerClient(crawler, "/", 4);
        crawlerClient.waitForCompletion();
    }

    private static void demoTestCrawler() throws InterruptedException {
        String[] data = new String[] {"1", "12", "123", "12345", "1234567890"};
        Crawler crawler = new TestCrawler();
        for (String datum : data) {
            Logger.log("*** Testing " + datum + " ***");
            CrawlerClient crawlerClient = new CrawlerClient(crawler, datum, 4);
            crawlerClient.waitForCompletion();
        }
    }
}
