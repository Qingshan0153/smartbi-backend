package com.bi.manager;

import com.bi.common.ErrorCode;
import com.bi.exception.ThrowUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * redis限流服务
 *
 * @author mendax
 * @version 2023/9/24 21:34
 */


@Component
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作(使用令牌桶算法)
     *
     * @param key 区分不同限流器，比如不同用户的 id 应该分别统计
     */
    public void doRateLimit(String key) {
        // 获取限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // 初始化
        // 最大流速 = 每1秒钟产生10个令牌
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        // 每当一个操作来了后，请求一个令牌（实用点：会员与普通用户调用次数限制）
        boolean isSuccess = rateLimiter.tryAcquire(1);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.TO_MANY_REQUEST,"请求过于频繁");

    }

}
