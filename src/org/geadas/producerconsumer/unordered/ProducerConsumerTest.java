/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geadas.producerconsumer.unordered;

import static java.lang.Thread.sleep;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author geadas
 */
public class ProducerConsumerTest {

    public static void main(String[] args) throws InterruptedException {
        ProductQueue c = new ProductQueue();
        Producer p1 = new Producer(c, 1);
        Producer p2 = new Producer(c, 2);
        Consumer c1 = new Consumer(c, 1);
        Consumer c2 = new Consumer(c, 2);
        p1.start();
        p2.start();
        c1.start();
        c2.start();

        p1.join();
        p2.join();
        c1.join();
        c2.join();

        System.out.println("Unordered Producer/Consumer test ended.");
    }
}

class ProductQueue {

    volatile boolean canProduce;
    public Queue<Integer> contents;
    int limit = 5;
    public volatile int producerFinished = 0;
    volatile int i = 0;

    public ProductQueue() {
        contents = new LinkedList<>();
        this.canProduce = true;
    }

    public synchronized int get(int workerNumber) {

        while (canProduce && producerFinished < 2) {
            try {
                System.out.println("Consumer #" + workerNumber +" is waiting for data...");
                wait(500);
            } catch (InterruptedException e) {
                System.out.println("Got interrupted: " + e.getMessage());
            }
        }
        int item = contents.remove();
        System.out.println("Consumer #" + workerNumber + " got: " + item);

        if (contents.isEmpty()) {
            canProduce = true;
            notifyAll();
        }

        return item;
    }

    public synchronized boolean put(int value, int workerNumber) throws InterruptedException {

        while (!canProduce) {
            try {
                System.out.println("Producer #" + workerNumber + " waiting for Consumer...");
                wait();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted: " + e.getMessage());
            }
        }

        System.out.println("Producer #" + workerNumber + " put: " + value);
        contents.add(value);

        if (contents.size() == limit) {
            canProduce = false;
            notifyAll();
        }

        return true;
    }

}

class Consumer extends Thread {

    private final ProductQueue prodQueue;
    private final int number;

    public Consumer(ProductQueue c, int number) {
        prodQueue = c;
        this.number = number;
    }

    @Override
    public void run() {
        while (prodQueue.contents.size() > 0 || prodQueue.producerFinished < 2) {
            prodQueue.get(this.number);
            try {
                sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                System.out.println("Got interrupted: " + e.getMessage());
            }
        }
    }
}

class Producer extends Thread {

    private final ProductQueue prodQueue;
    private final int number;

    public Producer(ProductQueue c, int number) {
        prodQueue = c;
        this.number = number;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                sleep((int) (Math.random() * 500));
            } catch (InterruptedException e) {
               System.out.println("Got interrupted: " + e.getMessage());
            }
            try {
                prodQueue.put(prodQueue.i++, this.number);
            } catch (InterruptedException e) {
               System.out.println("Got interrupted: " + e.getMessage());
            }
        }
        prodQueue.producerFinished++;
    }
}
