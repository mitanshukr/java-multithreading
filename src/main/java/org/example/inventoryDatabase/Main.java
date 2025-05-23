package org.example.inventoryDatabase;

import com.sun.source.tree.Tree;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
    public static final int HIGHEST_PRICE = 1000;

    public static void main(String[] args) throws InterruptedException {
        InventoryDatabase inventoryDatabase = new InventoryDatabase();

        Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE));
        }

        Thread writer = new Thread(() -> {
            while (true) {
                inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE));
                inventoryDatabase.removeItem(random.nextInt(HIGHEST_PRICE));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        });

        writer.start();

        int numberOfReaderThreads = 7;
        List<Thread> readers = new ArrayList<>();

        for (int readerIndex = 0; readerIndex < numberOfReaderThreads; readerIndex++) {
            Thread reader = new Thread(() -> {
                for (int i = 0; i < 100000; i++) {
                    int upperBoundPrice = random.nextInt(HIGHEST_PRICE);
                    int lowerBoundPrice = upperBoundPrice > 0 ? random.nextInt(upperBoundPrice) : 0;
                    inventoryDatabase.getNumberOfItemsInPriceRange(lowerBoundPrice, upperBoundPrice);
                }
            });

            readers.add(reader);
        }

        long startReadingTime = System.currentTimeMillis();
        for (Thread reader : readers) {
            reader.start();
        }

        for (Thread reader : readers) {
            reader.join();
        }
//        writer.join();

        long endReadingTime = System.currentTimeMillis();
        System.out.printf("Reading took %d ms%n", endReadingTime - startReadingTime);
    }

    public static class InventoryDatabase {
        private final TreeMap<Integer, Integer> priceToCountMap = new TreeMap<>();

        //        private ReentrantLock reentrantLock = new ReentrantLock();
        private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        private Lock readLock = readWriteLock.readLock();
        private Lock writeLock = readWriteLock.writeLock();


        public int getNumberOfItemsInPriceRange(int lowerBound, int upperBound) {
//            reentrantLock.lock();
            readLock.lock();
            try {
                Integer fromKey = priceToCountMap.ceilingKey(lowerBound);
                Integer toKey = priceToCountMap.floorKey(upperBound);

                if (fromKey == null || toKey == null) {
                    return 0;
                }

                NavigableMap<Integer, Integer> rangeOfPrices = priceToCountMap.subMap(fromKey, true, toKey, true);

                int sum = 0;
                for (int numberOfItemsForPrice : rangeOfPrices.values()) {
                    sum += numberOfItemsForPrice;
                }

                return sum;
            } finally {
//                reentrantLock.unlock();
                readLock.unlock();
            }

        }

        public void addItem(int price) {
//            reentrantLock.lock();
            writeLock.lock();
            try {
//                Integer numberOfItemsForPrice = priceToCountMap.get(price);
//                if (numberOfItemsForPrice == null) {
//                    priceToCountMap.put(price, 1);
//                } else {
//                    priceToCountMap.put(price, numberOfItemsForPrice + 1);
//                }
                // concise version (same-same)
                priceToCountMap.merge(price, 1, Integer::sum);
            } finally {
//                reentrantLock.unlock();
                writeLock.unlock();
            }
        }

        public void removeItem(int price) {
//            reentrantLock.lock();
            writeLock.lock();
            try {
                Integer numberOfItemsForPrice = priceToCountMap.get(price);
                if (numberOfItemsForPrice == null || numberOfItemsForPrice == 1) {
                    priceToCountMap.remove(price);
                } else {
                    priceToCountMap.put(price, numberOfItemsForPrice - 1);
                }
            } finally {
//                reentrantLock.unlock();
                writeLock.unlock();
            }

        }
    }
}
