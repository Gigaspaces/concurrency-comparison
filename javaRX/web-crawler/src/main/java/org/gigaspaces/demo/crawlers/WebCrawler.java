package org.gigaspaces.demo.crawlers;

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
import java.util.function.Consumer;

public class WebCrawler implements Crawler {
    private final String baseUrl;
    private final CloseableHttpClient client = HttpClientBuilder.create().build();

    public WebCrawler(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void crawl(String item, Consumer<String> consumer) {
        Document doc = Jsoup.parse(getDataFromUrl(baseUrl + item));
        Elements re = doc.select("a");
        for (Element element : re) {
            String link = element.attributes().get("href");
            consumer.accept(link);
        }
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
}
