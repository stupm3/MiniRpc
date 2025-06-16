package com.stupm.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.stupm.common.model.User;
import com.stupm.common.service.UserService;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.serializer.JdkSerializer;
import com.stupm.core.serializer.Serializer;

import java.util.ServiceLoader;


public class UserServiceProxy implements UserService {
    @Override
    public User getUser(User user) {
        Serializer serializer = null;
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
        for (Serializer service : serviceLoader) {
            serializer = service;
        }

        RpcRequest req = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodeName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try{
            byte[] bodyBytes = serializer.serialize(req);
            byte[] result;

            try(HttpResponse httpResponse = HttpRequest.post("localhost:8083").body(bodyBytes).execute()){
                result = httpResponse.bodyBytes();
            }
            RpcResponse deserialize = serializer.deserialize(result, RpcResponse.class);
            return (User) deserialize.getData();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
