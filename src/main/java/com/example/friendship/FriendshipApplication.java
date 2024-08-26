package com.example.friendship;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//如果没有在UserMapper与TagMapper中添加@Mapper注解
//那么要在这个启动类前面添加一个@MapperScan注解设置实体类对应的属性扫描的路径
@SpringBootApplication
@EnableScheduling  //开启定时任务注解
@MapperScan(value = "com.example.friendship.mapper")
public class FriendshipApplication {

    public static void main(String[] args) {
        SpringApplication.run(FriendshipApplication.class, args);
    }

}
