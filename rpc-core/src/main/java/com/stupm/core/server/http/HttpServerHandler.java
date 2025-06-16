package com.stupm.core.server.http;


import com.stupm.core.RpcApplication;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.registry.LocalRegistry;
import com.stupm.core.serializer.Serializer;
import com.stupm.core.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        System.out.println("Received request:" + request.method() + " " + request.uri() );

        request.bodyHandler(body ->{
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = new RpcRequest();
            try{
                rpcRequest = serializer.deserialize(bytes , RpcRequest.class);
            }catch (Exception e){
                e.printStackTrace();
            }

            RpcResponse rpcResponse = new RpcResponse();
            if(rpcRequest == null){
                rpcResponse.setMessage("rpcRequest is null");
                doResponse(request , rpcResponse , serializer);
                return;
            }
            try{

                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodeName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                rpcResponse.setData(result);
                rpcResponse.setReturnType(method.getReturnType());
                rpcResponse.setMessage("OK");
            }catch (Exception e){
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            doResponse(request , rpcResponse , serializer);

        });

    }

    void doResponse(HttpServerRequest req , RpcResponse resp , Serializer serializer){
        HttpServerResponse response = req.response()
                .putHeader("content-type", "application/json; charset=utf-8");
        try{
            byte[] serialize = serializer.serialize(resp);
            response.end(Buffer.buffer(serialize));
        }catch (IOException e){
            e.printStackTrace();
            response.end(Buffer.buffer());
        }
    }
}
