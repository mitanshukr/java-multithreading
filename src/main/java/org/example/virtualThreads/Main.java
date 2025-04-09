package org.example.virtualThreads;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private final static int NUM_OF_VIRTUAL_THREADS = 1000;

    public static void main(String[] args) throws InterruptedException {
//        Runnable runnable = () -> System.out.println("Inside Thread: " + Thread.currentThread());

//        Thread platformThread = new Thread(runnable);
//        Thread platformThread = Thread.ofPlatform().unstarted(runnable); // another way to create thread
//        platformThread.start();
//        platformThread.join();

        // virtual threads... //
//        Thread virtualThread = Thread.ofVirtual().unstarted(new BlockingTask());
//        virtualThread.start();
//        virtualThread.join();

        List<Thread> virtualThreads = new ArrayList<>();
        for (int i = 0; i < NUM_OF_VIRTUAL_THREADS; i++) {
            virtualThreads.add(
                    Thread.ofVirtual().unstarted(new BlockingTask())
            );
        }

        for (Thread virtualThread : virtualThreads) {
            virtualThread.start();
        }
        for (Thread virtualThread : virtualThreads) {
            virtualThread.join();
        }
    }

    private static class BlockingTask implements Runnable {
        @Override
        public void run() {
            System.out.println("Inside Thread: " + Thread.currentThread() + " before blocking call");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("after Thread: " + Thread.currentThread() + " after blocking call");

        }
    }
}
