package org.gigaspaces.demo;

import org.gigaspaces.demo.utils.Logger;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class RxDemo {
    private final String[] data;
    private int count = 0;

    public static void main(String[] args) throws InterruptedException {
        RxDemo p = new RxDemo(new String[] {"one", "two", "three", "four", "five" });
        //p.observableFromArray();
        //p.observableFromArrayWithLifecycle();
        p.observableFromArrayWithOperators();
        //p.customObservable();
    }

    private RxDemo(String[] data) {
        this.data = data;
    }

    private void observableFromArray() {
        System.out.println("Demo: Observable.from");
        Observable.from(data).subscribe(this::onNext);
    }

    private void observableFromArrayWithLifecycle() {
        System.out.println("Demo: Observable.from with error and complete");
        Observable.from(data).subscribe(this::onNext, this::onError, this::onComplete);
    }

    private void observableFromArrayWithOperators() {
        System.out.println("Demo: Observable.from with error and complete");
        Observable.from(data)
                .skip(2)
                .take(2)
                .subscribe(this::onNext, this::onError, this::onComplete);
    }

    private void customObservable() {
        ScheduledThreadPoolExecutor e = new ScheduledThreadPoolExecutor(2);
        Observable<String> o = Observable.create(subscriber -> {
            for (String s : data) {
                subscriber.onNext(s);
            }
            subscriber.onCompleted();
        });
        o.observeOn(Schedulers.from(e)).subscribe(this::onNext, this::onError, this::onComplete);
        e.shutdown();
    }

    private void onNext(String s) {
        Logger.log("onNext: " + s);
        count++;
    }

    private void onError(Throwable e) {
        Logger.log("onError: " + e);
    }

    private void onComplete() {
        Logger.log("onComplete - count = " + count);
    }
}
