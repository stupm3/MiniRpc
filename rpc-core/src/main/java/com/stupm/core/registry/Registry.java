package com.stupm.core.registry;

import com.stupm.core.config.RegistryConfig;
import com.stupm.core.model.ServiceMetaInfo;

import java.util.List;

public interface Registry {
    void init(RegistryConfig registryConfig);

    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    void unRegister(ServiceMetaInfo serviceMetaInfo);

    void destroy();

    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    void hearBeat();

    void watch(String serviceNodeKey);
}
