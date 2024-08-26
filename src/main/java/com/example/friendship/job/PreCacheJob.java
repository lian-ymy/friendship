package com.example.friendship.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.friendship.model.User;
import com.example.friendship.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.friendship.constant.UserConstant.REDIS_LOCK;
import static com.example.friendship.constant.UserConstant.REDIS_PREFIX;

@Component
@Slf4j
public class PreCacheJob {

    List<Long> mainUserList = List.of(1L);

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    UserService userService;

    @Resource
    RedissonClient redissonClient;

    //如果项目被部署到了多台服务器上面，到了指定时间有可能会出现这些服务器同时进行缓存预热的情况
    //因此我们应该添加方法来使得每次进行缓存预热的时候只有一台服务器能够进入该程序进行执行

    /**
     * 缓存预热，使用基于redisson的分布式锁来解决多台服务器同时进行更新的问题
     */
    @Scheduled(cron = "0 55 23 * * *")
    public void doRecommendUserCache() {
        RLock lock = redissonClient.getLock(REDIS_LOCK + "lock");
        //这里设置第一个参数对应的的等待时间必须为0，因为缓存预热操作每天只会进行一次

        try {
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                //加载所有重要用户对应的推荐缓存列表
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String key = REDIS_PREFIX + userId;
                    try {
                        redisTemplate.opsForValue().set(key, userPage);
                    } catch (Exception e) {
                        log.error("redis error" + e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("获取分布式锁失败：" + e);
        } finally {
            //这段代码一定要放到finally里面执行，如果try模块中有代码出现错误，最后也一定会释放锁
            //任务结束之后要进行释放锁的操作让其他线程能够尽早获取锁完成后续任务
            if(lock.isHeldByCurrentThread()) {
                //防止删掉了别人的锁，只有这把锁是当前线程创建的，才去释放当前的锁
                lock.unlock();
            }
        }
    }
}
