package org.example.stackDataStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class Main {
    public static void main(String[] args) throws InterruptedException {
//        StandardStack<Integer> stack = new StandardStack<>();
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        Random random = new Random();


        for (int i = 0; i < 100000; i++) {
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();

        int pushingThreads = 2;
        int poppingThreads = 2;

        for (int i = 0; i < pushingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.push(random.nextInt());
                }
            });

            threads.add(thread);
        }

        for (int i = 0; i < poppingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.pop();
                }
            });

            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        Thread.sleep(10000);

        System.out.printf("%,d operations were performed in 10 seconds %n", stack.getCounter());
        System.exit(0);
    }

    public static class LockFreeStack<T> {
        private AtomicReference<StackNode<T>> head = new AtomicReference<>();
        private AtomicInteger counter = new AtomicInteger(0);

        public void push(T value) {
            StackNode<T> newHeadNode = new StackNode<>(value);

            while (true) {
                StackNode<T> currHeadNode = head.get();
                newHeadNode.next = currHeadNode;
                if (head.compareAndSet(currHeadNode, newHeadNode)) {
                    break;
                } else {
                    LockSupport.parkNanos(1);
                }
            }
            counter.incrementAndGet();

        }

        public T pop() {
            StackNode<T> currHeadNode = head.get();
            StackNode<T> newHeadNode;

            while (currHeadNode != null) {
                newHeadNode = currHeadNode.next;
                if (head.compareAndSet(currHeadNode, newHeadNode)) {
                    break;
                } else {
                    LockSupport.parkNanos(1);
                    currHeadNode = head.get();
                }
            }
            counter.incrementAndGet();
            return currHeadNode != null ? currHeadNode.value : null;
        }

        public int getCounter() {
            return counter.get();
        }

    }

    public static class StandardStack<T> {
        private StackNode<T> head;
        private int counter = 0;

        public synchronized void push(T value) {
            StackNode<T> newHead = new StackNode<>(value);
            newHead.next = head;
            head = newHead;
            counter++;
        }

        public synchronized T pop() {
            if (head == null) {
                counter++;
                return null;
            } else {
                T val = head.value;
                head = head.next;
                counter++;
                return val;
            }
        }

        public int getCounter() {
            return counter;
        }
    }


    private static class StackNode<T> {
        public T value;
        public StackNode<T> next;

        public StackNode(T value) {
            this.value = value;
        }
    }
}
