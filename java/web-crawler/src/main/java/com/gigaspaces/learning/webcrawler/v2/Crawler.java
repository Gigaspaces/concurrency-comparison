package com.gigaspaces.learning.webcrawler.v2;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yohana Khoury
 * @since 12.1
 */
public class Crawler {
    private final CloseableHttpClient client;
    private final String baseUrl;
    private final ExecutorService executorService;
    private Object lock = new Object();
    private ConcurrentHashMap<String, Boolean> seen = new ConcurrentHashMap<String, Boolean>();
    private AtomicInteger pending = new AtomicInteger(0);

    public Crawler(String baseUrl, int numOfThreads) {
        this.client = HttpClientBuilder.create().build();
        this.baseUrl = baseUrl;
        this.executorService = Executors.newFixedThreadPool(numOfThreads, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "Crawler-Worker");
            }
        });
    }

    public int getSeenLinks() {
        return seen.size();
    }

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

    private List<String> getLinksFromUrl(final String url) {
        Document doc = Jsoup.parse(getDataFromUrl(baseUrl + url));
        Elements re = doc.select("a");
        ArrayList<String> list = new ArrayList<String>(re.size());
        for (Element element : re) {
            String link = element.attributes().get("href");
            list.add(link);
        }
        return list;
    }

    private String getDataFromUrl(String url) {
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

    public void start() {
        handle(baseUrl);
    }

    public void join() {
        synchronized (this.lock) {
            try {
                this.lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
