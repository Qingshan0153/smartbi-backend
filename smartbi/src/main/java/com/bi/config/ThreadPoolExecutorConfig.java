package com.bi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程池设置
 *
 * @author mendax
 * @version 2023/9/25 14:19
 */

@Configuration
public class ThreadPoolExecutorConfig {


    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        /*
        public ThreadPoolExecutor(int corePoolSize,(核心线程数，正常情况下能同时工作的线程数，属于同时就绪的状态)
                              int maximumPoolSize,(最大线程数，极限情况下线程池最多有多少线程)
                              long keepAliveTime,(空闲线程存活时间，非核心线程在没有任务的情况下，过多久要删除)
                              TimeUnit unit, (时间单位,分钟、秒)
                              BlockingQueue<Runnable> workQueue,(工作队列，用于存放给线程执行的任务，设置队列最大长度)
                              ThreadFactory threadFactory,(线程工厂，控制每个线程的生成、线程的属性（比如线程名）)
                              RejectedExecutionHandler handler (拒绝策略，任务队列满的时候，我们采取什么措施，比如抛异常、不抛异常、自定义策略)
                              )
                              注：资源隔离策略：比如重要的任务(VIP任务)一个队列，普通任务一个队列，保证这两个队列互不干扰。
         */

        return new ThreadPoolExecutor(2, 4, 50, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20), Executors.defaultThreadFactory());

    }

}
