package com.gigaspaces.learning.webcrawler.v1;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Yohana Khoury
 */
public class App {
    private final String baseUrl;
    private final Set<String> seen;
    private final ExecutorService executorService;
    private final BlockingQueue<List<String>> queue;
    private final CloseableHttpClient client;


    public App(String baseUrl, int numOfThreads) {
        this.executorService = Executors.newFixedThreadPool(numOfThreads, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "Crawler-Worker");
            }
        });
        this.baseUrl = baseUrl;
        this.seen = new HashSet<String>();
        this.queue = new LinkedBlockingDeque<List<String>>();
        this.client = HttpClientBuilder.create().build();
    }

    public void start() throws InterruptedException {
        int n = 0;
        queue.add(Arrays.asList("/"));
        n++;
        for (; n > 0; n--) {
            final List<String> links = queue.take();
            for (final String link : links) {
                if (seen.contains(link)) {
                    continue;
                }
                seen.add(link);
                n++;
                executorService.execute(new Runnable() {
                    public void run() {
                        queue.add(getLinksFromUrl(link));
                    }
                });
            }
        }
    }

    public List<String> getLinksFromUrl(final String url) {
        Document doc = Jsoup.parse(getDataFromUrl(baseUrl + url));
        Elements re = doc.select("a");
        ArrayList<String> list = new ArrayList<String>(re.size());
        for (Element element : re) {
            list.add(element.attributes().get("href"));
        }
        return list;
    }

    public String getDataFromUrl(String url) {
        BufferedReader rd = null;
        try {
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Could not fetch data from " + url);
        } finally {
            if (rd != null)
                try {
                    rd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }


    public int getProcessedLinks() {
        return seen.size();
    }

    public void shutdown() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
