package com.stupm.core.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;


import java.io.IOException;

public class JsonSerializer implements Serializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        T obj  = OBJECT_MAPPER.readValue(data, clazz);
        if(obj instanceof RpcRequest){
            handleRequest((RpcRequest) obj , clazz);
        }else if(obj instanceof RpcResponse){
            handleResponse((RpcResponse) obj , clazz);
        }
        return obj;
    }

    //以下为防止反序列化时系统将map转换为linkedhashmap的处理
    public <T> T handleRequest(RpcRequest request, Class<T> clazz) throws IOException {
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] args = request.getArgs();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if(!parameterType.isAssignableFrom(RpcRequest.class)){
                byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(bytes, parameterType);
            }
        }
        return clazz.cast(request);
    }

    public <T> T handleResponse(RpcResponse response, Class<T> clazz) throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(response);
        response.setData(OBJECT_MAPPER.readValue(bytes, clazz));
        return clazz.cast(response);
    }
}
