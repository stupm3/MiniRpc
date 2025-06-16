package com.stupm.core.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import com.stupm.core.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiLoader {
    private static final Map<String , Map<String , Class<?>>> loaderMap = new ConcurrentHashMap<>();

    private static final Map<String , Object> instanceCache = new ConcurrentHashMap<>();

    private static  final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    private static  final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    private static final String[] SCAN_DIRS = {RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};


    public static Map<String , Class<?>> load(Class<?> clazz){
        log.info("加载类型为:{}的SPI",clazz.getName());
        Map<String , Class<?>> keyMap = new HashMap<>();
        for(String dir : SCAN_DIRS){
            List<URL> resources = ResourceUtil.getResources(dir + clazz.getName());
            for(URL resource : resources){
                try {
                    InputStreamReader reader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    while((line = bufferedReader.readLine() )!= null){
                        String[] str = line.split("=");
                        if(str.length > 1){
                            String key = str[0];
                            String spi = str[1];
                            keyMap.put(key, Class.forName(spi));
                        }
                    }
                } catch (Exception e) {
                    log.error("spi load error", e);
                }
            }

        }
        loaderMap.put(clazz.getName(), keyMap);
        return keyMap;
    }

    public static <T> T getInstance(Class<T> clazz , String key){
        String name = clazz.getName();
        Map<String, Class<?>> keyMap = loaderMap.get(name);
        if(keyMap == null)
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", clazz.getName()));
        if(!keyMap.containsKey(key)){
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key=%s 的类型", clazz, key));
        }
        Class<?> implClass = keyMap.get(key);
        String implClassName = implClass.getName();
        if(!instanceCache.containsKey(implClassName)){
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMsg = String.format("%s 类实例化失败", implClassName);
                throw new RuntimeException(errorMsg, e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }


}
