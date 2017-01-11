package com.gigaspaces.learning.webcrawler;

/**
 * @author Yohana Khoury
 * @since 12.1
 */
public class Launcher {

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 3) {
            System.out.println("<Main> <baseUrl> <numOfThreads>");
        }

        String runType = args[0];
        String baseUrl = args[1];
        String numOfThreads = args[2];
        if (runType.equals("v1")) {
            com.gigaspaces.learning.webcrawler.v1.Main.main(new String[]{baseUrl, numOfThreads});
        } else if (runType.equals("v2")){
            com.gigaspaces.learning.webcrawler.v2.Main.main(new String[]{baseUrl, numOfThreads});
        } else {
            System.out.println("Unknown runType " + runType);
        }
    }
}
