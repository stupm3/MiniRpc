package com.stupm.core.handler;

import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.protocol.ProtocolMessage;
import com.stupm.core.protocol.ProtocolMessageTypeEnum;
import com.stupm.core.registry.LocalRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

public class RpcServerHandler extends SimpleChannelInboundHandler<ProtocolMessage<?>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage<?> message) throws Exception {
        RpcRequest request = (RpcRequest) message.getBody();
        RpcResponse resp = new RpcResponse();
        try{
            Class<?> implClass = LocalRegistry.get(request.getServiceName());
            Method method = implClass.getMethod(request.getMethodeName(), request.getParameterTypes());
            Object invoke = method.invoke(implClass.newInstance(), request.getArgs());
            resp.setData(invoke);
            resp.setMessage("ok");
            resp.setReturnType(method.getReturnType());
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        ProtocolMessage.Header header = message.getHeader();
        header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
        ProtocolMessage<RpcResponse> response = new ProtocolMessage<>(header, resp);
        ctx.writeAndFlush(response);
    }
}
