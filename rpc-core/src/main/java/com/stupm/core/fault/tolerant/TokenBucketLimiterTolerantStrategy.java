package com.stupm.core.fault.tolerant;

import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class TokenBucketLimiterTolerantStrategy implements TolerantStrategy {

    // 每秒生成的令牌数（即最大请求数）
    private final long capacity;

    // 当前可用令牌数
    private final AtomicLong tokens;

    // 上一次补充令牌的时间戳（毫秒）
    private long lastRefillTimestamp;

    public TokenBucketLimiterTolerantStrategy(long capacity) {
        this.capacity = capacity;
        this.tokens = new AtomicLong(capacity);
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");

        boolean allowed = tryConsumeToken();

        if (!allowed) {
            log.warn("请求被限流，服务名: {}, 方法: {}", rpcRequest.getServiceName(), rpcRequest.getMethodName());
            return RpcResponse.builder()
                    .message("请求被限流")
                    .exception(e)
                    .build();
        }

        // 正常处理逻辑（如果需要绕过异常重试等）
        throw new RuntimeException(e); // 继续抛出异常或自定义处理
    }

    /**
     * 尝试消费一个令牌
     *
     * @return 是否允许请求通过
     */
    private boolean tryConsumeToken() {
        refillTokens();

        if (tokens.get() > 0) {
            return tokens.compareAndSet(tokens.get(), tokens.get() - 1);
        }

        return false;
    }

    /**
     * 根据时间间隔补充令牌
     */
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long timeElapsed = now - lastRefillTimestamp;

        // 每毫秒生成的令牌数
        double tokensToAdd = timeElapsed * ((double) capacity / 1000);

        if (tokensToAdd > 0) {
            long newTokens = Math.min(capacity, tokens.get() + (long) tokensToAdd);
            tokens.set(newTokens);
            lastRefillTimestamp = now;
        }
    }
}
