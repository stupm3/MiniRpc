package com.stupm.core.loadbalancer;

import com.stupm.core.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger currenIndex = new AtomicInteger(0);


    @Override
    public ServiceMetaInfo select(Map<String, Object> requiredParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList.isEmpty())
                return null;
        int size = serviceMetaInfoList.size();
        if(size == 1)
            return serviceMetaInfoList.get(0);
        int index = currenIndex.getAndIncrement() % size;
        return serviceMetaInfoList.get(index);
    }
}
