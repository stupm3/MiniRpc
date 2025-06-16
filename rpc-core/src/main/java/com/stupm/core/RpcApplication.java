package com.stupm.core;

import com.stupm.core.config.RegistryConfig;
import com.stupm.core.config.RpcConfig;
import com.stupm.core.constant.RpcConstant;
import com.stupm.core.registry.Registry;
import com.stupm.core.registry.RegistryFactory;
import com.stupm.core.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("RpcApplication init , rpcConfig = {}", rpcConfig);
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("  registry init , config = {}", registryConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(registry ::destroy));
    }

    public static void init(){
        RpcConfig newRpcConfig;
        try{
            newRpcConfig = ConfigUtils.loadConfig(RpcConstant.DEFAULT_SERVICE_PREFIX , RpcConfig.class);
        }catch (Exception e){
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    public static RpcConfig getRpcConfig() {
        if(rpcConfig == null){
            synchronized (RpcApplication.class){
                if(rpcConfig == null){
                    init();
                }
            }
        }
        return rpcConfig;
    }



}
