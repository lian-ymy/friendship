package com.example.friendship.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson配置
 */
@Configuration
//要读取的配置文件前缀
@ConfigurationProperties(prefix = "spring.data.redis")
//自定义的返回方法类中如果有自定义的属性值，最好添加getter与setter方法进行返回
@Data
public class RedissonConfig {
    //在类中定义配置前缀后面对应的具体属性进行赋值
    private String port;
    private String host;
    private String password;

    //添加@Bean注解后，其它地方就可以通过@Resource使用该对象
    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建redisson的相关配置
        Config config = new Config();
        //这里将redis的配置地址写死了，实际上并不推荐这么做
//        String redisAddress = "redis://127.0.0.1:6379";
        String redisAddress = String.format("redis://%s:%s",host,port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(1).setPassword(password);
        // 2、创建实例
        return Redisson.create(config);
    }
}
