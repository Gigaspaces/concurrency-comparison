package org.gigaspaces.demo.rx;

import org.gigaspaces.demo.crawlers.Crawler;
import org.gigaspaces.demo.utils.Logger;
import rx.Observable;

import java.util.concurrent.CountDownLatch;

public class CrawlerClient {

    private final long startTime = System.currentTimeMillis();
    private long completeTime;
    private final CountDownLatch completionLatch = new CountDownLatch(1);
    private final Observable<String> observable;

    public CrawlerClient(Crawler crawler, String seed, int numOfThreads) {
        this.observable = ObservableCrawler.create(crawler, seed, numOfThreads);
        observable.subscribe(this::onNext, this::onError, this::onCompleted);
    }

    public void waitForCompletion() throws InterruptedException {
        completionLatch.await();
    }

    private void onNext(String s) {
        Logger.log(">>> " + s);
    }

    private void onError(Throwable e) {
        complete();
        Logger.log("Error: " + e);
    }

    private void onCompleted() {
        complete();
        Logger.log("Completed - duration = " + (completeTime - startTime));
    }

    private void complete() {
        completeTime = System.currentTimeMillis();
        completionLatch.countDown();
    }
}
