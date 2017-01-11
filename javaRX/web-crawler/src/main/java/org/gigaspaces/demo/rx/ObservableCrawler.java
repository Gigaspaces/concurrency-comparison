package org.gigaspaces.demo.rx;

import org.gigaspaces.demo.crawlers.Crawler;
import rx.Observable;
import rx.Subscriber;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ObservableCrawler {
    private final Crawler crawler;
    private Subscriber<? super String> subscriber;
    private Set<String> results = ConcurrentHashMap.newKeySet();
    private final ExecutorService executorService;
    private final AtomicInteger pendingTasks = new AtomicInteger();
    private final AtomicInteger threadIdGenerator = new AtomicInteger();

    public static Observable<String> create(Crawler crawler, String seed, int numOfThreads) {
        ObservableCrawler o = new ObservableCrawler(crawler, numOfThreads);
        return Observable.create(subscriber -> {
            o.subscriber = subscriber;
            if (o.executorService == null) {
                o.process(seed);
                subscriber.onCompleted();
            } else {
                o.processAsync(seed);
            }
        });
    }

    private ObservableCrawler(Crawler crawler, int numOfThreads) {
        this.crawler = crawler;
        this.executorService = numOfThreads > 0 ?
                Executors.newFixedThreadPool(numOfThreads, r -> new Thread(r, "Crawler-" + threadIdGenerator.incrementAndGet()))
                : null;
    }

    private void process(String item) {
        // If item is not unique, skip processing
        if (!results.add(item))
            return;
        subscriber.onNext(item);
        crawler.crawl(item, this::process);
    }

    private void processAsync(String item) {
        pendingTasks.incrementAndGet();
        executorService.submit(() -> {
            // If item is not unique, skip processing
            boolean isFirstTime =  results.add(item);
            if (isFirstTime) {
                subscriber.onNext(item);
                crawler.crawl(item, this::processAsync);
            }
            if (pendingTasks.decrementAndGet() == 0) {
                subscriber.onCompleted();
                executorService.shutdown();
            }
        });
    }
}
