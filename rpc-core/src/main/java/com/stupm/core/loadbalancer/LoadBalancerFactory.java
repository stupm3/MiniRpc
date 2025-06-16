package com.stupm.core.loadbalancer;

import com.stupm.core.utils.SpiLoader;

public class LoadBalancerFactory {
    static{
        SpiLoader.load(LoadBalancer.class);
    }

    private static final LoadBalancer DEFAULT = new RoundRobinLoadBalancer();

    public static LoadBalancer getInstance(String key){
        return SpiLoader.getInstance(LoadBalancer.class , key);
    }
}
