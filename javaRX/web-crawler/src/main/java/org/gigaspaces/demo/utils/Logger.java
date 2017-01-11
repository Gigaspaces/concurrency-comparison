package org.gigaspaces.demo.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class Logger {
    private static final AtomicInteger counter = new AtomicInteger();
    public static void log(String s) {
        //String id = "#" + counter.incrementAndGet() + " ";
        String message = /*id +*/ " [" + Thread.currentThread().getName() + "] " + s;
        System.out.println(message);
    }
}
