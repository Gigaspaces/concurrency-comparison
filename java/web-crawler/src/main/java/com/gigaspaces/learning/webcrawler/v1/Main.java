package com.gigaspaces.learning.webcrawler.v1;


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
        App app = new App(baseUrl, numOfThreads);
        long start = System.currentTimeMillis();
        app.start();
        long totalTimeInMS = System.currentTimeMillis() - start;
        app.shutdown();
        System.out.println(app.getProcessedLinks() + " links processed in " + (double) totalTimeInMS / 1000 + "s");

    }
}
