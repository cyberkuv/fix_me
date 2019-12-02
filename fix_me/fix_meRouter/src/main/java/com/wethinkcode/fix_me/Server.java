package com.wethinkcode.fix_me;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Router(5000));
        executorService.execute(new Router(5001));
        executorService.shutdown();
    }
}