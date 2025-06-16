package com.stupm.core.server.tcp;

import com.stupm.core.RpcApplication;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.protocol.ProtocolMessage;
import com.stupm.core.protocol.ProtocolMessageDecoder;
import com.stupm.core.protocol.ProtocolMessageEncoder;
import com.stupm.core.protocol.ProtocolMessageTypeEnum;
import com.stupm.core.registry.LocalRegistry;
import com.stupm.core.serializer.Serializer;
import com.stupm.core.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class VertxTcpHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
        TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            ProtocolMessage<RpcRequest> message;
            try{
                message = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议编解码错误");
            }
            RpcRequest request = message.getBody();
            RpcResponse response = new RpcResponse();
            try{
                Class<?> implClass = LocalRegistry.get(request.getServiceName());
                Method method = implClass.getMethod(request.getMethodeName(), request.getParameterTypes());
                Object invoke = method.invoke(implClass.newInstance(), request.getArgs());
                response.setData(invoke);
                response.setMessage("ok");
                response.setReturnType(method.getReturnType());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ProtocolMessage.Header header = message.getHeader();
            header.setType((byte)ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseMessage = new ProtocolMessage<>(header, response);
            try{
                Buffer encode = ProtocolMessageEncoder.encode(responseMessage);
                netSocket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("消息协议编码错误");
            }
        });
        netSocket.handler(tcpBufferHandlerWrapper);
    }
}
