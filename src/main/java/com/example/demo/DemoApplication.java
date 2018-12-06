package com.example.demo;

import org.apache.tomcat.util.net.NioChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@SpringBootApplication
@EnableAsync
public class DemoApplication {

    @RestController
    public static class MainController {
        static AsyncRestTemplate rt = new AsyncRestTemplate(
                new Netty4ClientHttpRequestFactory()); // default NioEventLoopGroup
        static final String url = "http://localhost:8081/service?req={req}";
        static final String url2 = "http://localhost:8081/service2?req={req}";

        @Autowired
        MyService myService;


        @GetMapping("/")
        public DeferredResult<String> hello(int idx) {
            DeferredResult<String> dr = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> fl = rt.getForEntity(url, String.class, "h" + idx);
            fl.addCallback(new SuccessCallback<ResponseEntity<String>>() {
                @Override
                public void onSuccess(ResponseEntity<String> s) {
                    ListenableFuture<ResponseEntity<String>> fl2 = rt.getForEntity(url2, String.class, s.getBody() );
                    fl2.addCallback(new SuccessCallback<ResponseEntity<String>>() {

                        @Override
                        public void onSuccess(ResponseEntity<String> s2) {
                            ListenableFuture<String> f3 = myService.work(s2.getBody());

                            f3.addCallback(new SuccessCallback<String>() {

                                @Override
                                public void onSuccess(String s) {
                                    dr.setResult(s);
                                }
                            }, new FailureCallback() {

                                @Override
                                public void onFailure(Throwable throwable) {
                                    dr.setErrorResult(throwable);
                                }
                            });
                        }
                    }, new FailureCallback() {

                        @Override
                        public void onFailure(Throwable throwable) {
                            dr.setErrorResult(throwable.getMessage());
                        }
                    });

                }
            }, new FailureCallback() {

                @Override
                public void onFailure(Throwable throwable) {
                    dr.setErrorResult(throwable.getMessage());
                }
            });

            return dr;
        }

    }


    @Service
    public static class MyService {
        @Async
        public ListenableFuture<String> work(String req) {
            return new AsyncResult<>("hello" + req);
        }
    }


    @Bean
    public ThreadPoolTaskExecutor myThreadPool() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(1);
        t.setQueueCapacity(1);
        t.setMaxPoolSize(1);
        t.initialize();
        return t;
    }


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
