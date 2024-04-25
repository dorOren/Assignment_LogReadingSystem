package com.tests;

import com.assignment.service.DbRequestQueue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class DbRequestQueueTests {

    @Test
    public void testSubmitDbOperation() throws Exception {
        DbRequestQueue dbRequestQueue = new DbRequestQueue(2);
        Callable<String> task = () -> "Done";

        Future<String> future = dbRequestQueue.submitDbOperation(task);

        assertEquals("Done", future.get()); // Verifying the task is executed and result is as expected
    }

    @Test
    public void testConcurrentDbAccessLimit() throws InterruptedException {
        int maxConcurrentTasks = 2;
        DbRequestQueue dbRequestQueue = new DbRequestQueue(maxConcurrentTasks);
        CountDownLatch latch = new CountDownLatch(maxConcurrentTasks);
        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < maxConcurrentTasks + 1; i++) {
            executor.execute(() -> {
                try {
                    dbRequestQueue.submitDbOperation(() -> {
                        latch.countDown();
                        Thread.sleep(1000); // Simulate a long-running task
                        return null;
                    }).get();
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue(latch.await(500, TimeUnit.MILLISECONDS),
                "Latch should count down twice within 500 milliseconds, indicating that only two tasks were started concurrently");
        executor.shutdownNow();
    }

    @Test
    public void testFifoOrdering() throws InterruptedException, ExecutionException {
        int maxConcurrentTasks = 2;
        DbRequestQueue dbRequestQueue = new DbRequestQueue(maxConcurrentTasks);
        List<Integer> taskExecutionOrder = Collections.synchronizedList(new ArrayList<>());

        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            int taskId = i;
            Future<Integer> future = dbRequestQueue.submitDbOperation(() -> {
                Thread.sleep(500 + taskId * 500); // Simulate processing time, and increase for each task
                taskExecutionOrder.add(taskId);
                return taskId;
            });
            futures.add(future);
        }

        for (Future<Integer> future : futures) {
            future.get(); // Wait for all tasks to complete
        }

        List<Integer> expectedOrder = Arrays.asList(0, 1, 2, 3, 4);
        assertEquals(expectedOrder, taskExecutionOrder, "Tasks should complete in FIFO order");
    }
}