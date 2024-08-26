package com.example.friendship.once;

import com.example.friendship.model.User;
import com.example.friendship.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class InsertUsers {
    @Resource
    UserService userService;

    final int INSERT_NUM = 50000;

    private ExecutorService executorService = new ThreadPoolExecutor(60, 1000,
            10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

//    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void insertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<User> userList = new ArrayList<>(15);
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int j = 0; j < 10; j++) {
            userList.clear();
            for (int i = 0; i < INSERT_NUM; i++) {
                User user = new User();
                user.setUsername("米哈游原神");
                user.setUserAccount("mihoyo");
                user.setAvatarUrl("https://img1.baidu.com/it/u=1614944575,1449735529&fm=253&app=120&size=w931&n=0&f=JPEG&fmt=auto?sec=1721322000&t=bfe47f782d8dbecd10b8192bb0961f32");
                user.setGender(0);
                user.setProfile("喜欢二次元");
                user.setPhone("12345678901");
                user.setEmail("123@qq.com");
                user.setUserStatus(0);
                user.setUserPassword("12345678");
                user.setUserRole(0);
                user.setPlanetCode("1111");
                user.setTags("[\"二次元\"]");
                userList.add(user);
            }
            //从我们定义的线程池中取出多个线程来执行插入数据操作
            //如果不指定要使用的线程池，默认会使用本机的cpu核来进行线程分配插入数据操作，如果本机电脑为16核，那么能够提供的线程数就是15个
            CompletableFuture<Void> Future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, INSERT_NUM);
            },executorService);
            futureList.add(Future);
        }
        //使用join方法，只有这个集合中的所有线程全部执行完毕才会执行接下来的方法
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        //批量插入 20s 10w条数据
//        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
