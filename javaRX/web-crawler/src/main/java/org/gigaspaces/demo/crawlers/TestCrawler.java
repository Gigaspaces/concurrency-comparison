package org.gigaspaces.demo.crawlers;

import java.util.function.Consumer;

public class TestCrawler implements Crawler {
    @Override
    public void crawl(String item, Consumer<String> consumer) {
        if (item.length() > 1) {
            // head:
            consumer.accept(item.substring(0, item.length() - 1));
            // tail:
            consumer.accept(item.substring(1));
        }
    }
}
