package com.stupm.core.registry;

import com.stupm.core.utils.SpiLoader;

public class RegistryFactory {

    private static boolean flag = false;


    private static final Registry DEFAULT_REGISTRY = new ZookeeperRegistry();

    public static Registry getRegistry(String key) {
        if(!flag){
            SpiLoader.load(Registry.class);
        }
        return SpiLoader.getInstance(Registry.class , key);
    }

}
