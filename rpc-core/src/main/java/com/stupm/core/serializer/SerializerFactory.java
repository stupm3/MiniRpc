package com.stupm.core.serializer;

import com.stupm.core.utils.SpiLoader;

import java.util.HashMap;

public class SerializerFactory {

    private static  volatile boolean flag = false;


    public static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    public static Serializer getInstance(String key) {
        if( !flag ) {
            flag = true;
            SpiLoader.load(Serializer.class);
        }
        return SpiLoader.getInstance(Serializer.class, key);
    }
}
