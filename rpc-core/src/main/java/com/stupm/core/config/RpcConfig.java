package com.stupm.core.config;

import com.stupm.core.fault.retry.RetryStrategyKeys;
import com.stupm.core.fault.tolerant.TolerantStrategyKeys;
import com.stupm.core.loadbalancer.LoadBalanceKeys;
import com.stupm.core.serializer.SerializerKey;
import lombok.Data;

@Data
public class RpcConfig {
    private String name = "rpc-core";

    private String version = "1.0";

    private String serverHost = "127.0.0.1";

    private Integer serverPort = 8080;

    private boolean mock = false;

    private String serializer = SerializerKey.JDK;

    private RegistryConfig registryConfig = new RegistryConfig();

    private String loadBalancer = LoadBalanceKeys.ROUND_ROBIN;

    private String retryStrategy = RetryStrategyKeys.NO;

    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;

}
