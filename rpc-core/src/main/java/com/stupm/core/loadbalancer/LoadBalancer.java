package com.stupm.core.loadbalancer;

import com.stupm.core.model.ServiceMetaInfo;

import java.util.Map;
import java.util.List;

public interface LoadBalancer {
    ServiceMetaInfo select(Map<String , Object> requiredParams , List<ServiceMetaInfo> serviceMetaInfoList);
}
