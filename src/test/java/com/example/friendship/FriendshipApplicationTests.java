package com.example.friendship;

import com.example.friendship.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static com.example.friendship.constant.UserConstant.REDIS_PREFIX;

@SpringBootTest
class FriendshipApplicationTests {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    UserService userService;

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    RedissonClient redissonClient;

//    @Test
//    void contextLoads() {
//    }

//    @Test
//    void testSelectUsersByTags() {
//        ArrayList<String> stringArrayList = new ArrayList<>();
//        stringArrayList.add("星穹铁道");
//        List<User> users = userService.searchUsersByTags(stringArrayList);
//        System.out.println(users);
//    }
//
//    @Test
//    void testRedisConnection() {
//        redisTemplate.opsForValue().set("lian",2.0);
//    }

    /**
     * 测试redisson的看门狗机制，使用redisson申请锁后，如果当前线程还没有手动释放锁，
     * redisson会自动更新过期时间，重新将过期时间设置为30s
     */
    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock("ymy");
        try {
            if(lock.tryLock(0,-1, TimeUnit.SECONDS)) {
                Thread.sleep(300000);
                //加载所有重要用户对应的推荐缓存列表
                String key = REDIS_PREFIX + "1";
                redisTemplate.opsForValue().set(key, "jingliu");
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
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
