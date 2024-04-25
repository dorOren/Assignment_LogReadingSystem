package com.assignment.service;

import java.util.concurrent.*;

public class DbRequestQueue {
    private final ExecutorService executorService;

    public DbRequestQueue(int maxConcurrentTasks) {
        executorService = Executors.newFixedThreadPool(maxConcurrentTasks);
    }

    public <T> Future<T> submitDbOperation(Callable<T> dbOperation) {
        return executorService.submit(dbOperation);
    }


}
