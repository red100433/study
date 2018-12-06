package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Loadtest {
    static AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8080/callable?idx={idx}";

        CyclicBarrier barrier = new CyclicBarrier(101);


        for (int i = 0; i < 100; i++) {
            es.submit(
                    () -> {
                int idx = counter.addAndGet(1);

                barrier.await();
                //log.info("Thread : {} ", idx);

                StopWatch ew = new StopWatch();

                ew.start();

                String res = rt.getForObject(url, String.class, idx);

                ew.stop();


                //log.info("Elapsed : {} {} / {}", idx, ew.getTotalTimeMillis(), res);

                return null;
            }
//                    new Callable<Object>() {
//                        @Override
//                        public Object call() throws Exception {
//                            return null;
//                        }
//                    }
//
//                    new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    }
            );

        }

        barrier.await();

        StopWatch main = new StopWatch();
        main.start();

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        main.stop();

        //log.info("total Time : {}", main.getTotalTimeMillis());
    }
}
