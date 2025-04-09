package org.example.ioBoundApplication;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IoBoundApplication {
    private static final int NUMBER_OF_TASKS = 100_000;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Press enter to start");
        sc.nextLine();
        System.out.printf("Running %d tasks\n", NUMBER_OF_TASKS);

        long start = System.currentTimeMillis();
        performTask();
        System.out.printf("Tasks took %dms to complete\n", System.currentTimeMillis() - start);
    }

    private static void performTask() {
        // Both below method calls creates platform threads which is costly and memory bound.
        // Using newFixedThreadPool(x) will have performance issue if x is smaller.
        // Using newCachedThreadPool() creates a thread-per-task (reusing), but throws outOfMemory...
        // ...error with very high number of tasks (NUMBER_OF_TASKS).
//        Executors.newFixedThreadPool(100)
//        Executors.newCachedThreadPool()

        // so here comes a virtual thread (introduced in Java 21)
        // It solves all above-mentioned issues with platform thread.
        // Executors.newVirtualThreadPerTaskExecutor()
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executorService.submit(IoBoundApplication::blockingIoOperation);
            }
        }
    }


    private static void blockingIoOperation() {
        System.out.println("executing a blocking task for thread: " + Thread.currentThread());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
