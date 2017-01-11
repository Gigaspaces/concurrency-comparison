package com.gigaspaces.learning.webcrawler.v2;

/**
 * @author Yohana Khoury
 */
public class Main {

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
