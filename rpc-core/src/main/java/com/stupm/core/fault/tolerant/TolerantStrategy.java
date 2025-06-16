package com.stupm.core.fault.tolerant;

import com.stupm.core.model.RpcResponse;

import java.util.Map;

public interface TolerantStrategy {
    RpcResponse doTolerant(Map<String , Object> context , Exception e);
}
