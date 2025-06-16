package com.stupm.core.proxy;

import com.stupm.core.config.RpcConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
public class MockServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();
        log.info("mock invoke {}",returnType.getName());
        return getDefaultObject(returnType);
    }

    public Object getDefaultObject(Class<?> type){
        if(type.isPrimitive()){
            if(type == boolean.class){
                return false;
            }else if(type == int.class){
                return 1;
            }else if(type == long.class){
                return 2L;
            }else if(type == short.class){
                return (short)0;
            }
        }
        return null;
    }
}
