package com.bi.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author mendax
 * @version 2023/9/24 21:52
 */

@SpringBootTest
class RedisLimiterManagerTest {

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Test
    void doRateLimit() {

        String user = "1";
        for (int i = 0; i < 5; i++) {
            redisLimiterManager.doRateLimit(user);
            System.out.println("成功");
        }
    }
}