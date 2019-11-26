package com.uber.profiling.examples;

public class AnotherExample {

    public AnotherExample(){

    }

    public void publicLoop5000() throws InterruptedException {
        long j = 0;
        Thread.sleep(5000);
        for (int i = 1; i < 5000; i++) {
            j += i;
        }
        System.out.println("Result" + j);
    }

    public void publicLoop100() throws InterruptedException {
        long j = 0;
        Thread.sleep(100);
        for (int i = 1; i < 100; i++) {
            j += i;
        }
        System.out.println("Result" + j);
        publicLoop5000();
    }

    public void publicLoop10000() throws InterruptedException {
        long j = 0;
        Thread.sleep(10000);
        for (int i = 1; i < 10000; i++) {
            j += i;
        }
        System.out.println("Result" + j);
    }
}
