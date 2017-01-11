package org.gigaspaces.demo.crawlers;

import java.util.function.Consumer;

public interface Crawler {
    void crawl(String item, Consumer<String> consumer);
}
