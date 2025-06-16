package com.stupm.core.fault.retry;

import com.stupm.core.utils.SpiLoader;

public class RetryStrategyFactory {
    static{
        SpiLoader.load(RetryStrategy.class);
    }

    public static final RetryStrategy DEFAULT_STRATEGY = new NoRetryStrategy();

    public static RetryStrategy getInstance(String key){
        return SpiLoader.getInstance(RetryStrategy.class , key);
    }
}
