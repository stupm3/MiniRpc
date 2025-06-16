package com.stupm.core.registry;

import com.stupm.core.model.ServiceMetaInfo;

import java.util.List;

public class RegistryServiceCache {
    private List<ServiceMetaInfo> serviceCache = null;

    void writeCache(List<ServiceMetaInfo> serviceCache) {
        this.serviceCache = serviceCache;
    }

    List<ServiceMetaInfo> readCache() {
        return serviceCache;
    }


    void clear(){
        this.serviceCache = null;
    }
}
