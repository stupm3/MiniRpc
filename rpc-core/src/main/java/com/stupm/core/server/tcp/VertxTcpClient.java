package com.stupm.core.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.stupm.core.RpcApplication;
import com.stupm.core.constant.ProtocolConstants;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.model.ServiceMetaInfo;
import com.stupm.core.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VertxTcpClient {
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo service) throws InterruptedException, ExecutionException {
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        netClient.connect(service.getServicePort() , service.getServiceHost(),
                result -> {
                    if(result.succeeded()){
                        System.out.println("Tcp Connect Succeed");
                        NetSocket socket = result.result();
                        ProtocolMessage<RpcRequest> request = new ProtocolMessage<>();
                        ProtocolMessage.Header header = new ProtocolMessage.Header();
                        header.setMagic(ProtocolConstants.PROTOCOL_MAGIC);
                        header.setVersion(ProtocolConstants.PROTOCOL_VERSION);
                        header.setSerializer((byte) ProtocolSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                        header.setRequestId(IdUtil.getSnowflakeNextId());
                        request.setHeader(header);
                        request.setBody(rpcRequest);
                        try {
                            Buffer encode = ProtocolMessageEncoder.encode(request);
                            socket.write(encode);
                        } catch (IOException e) {
                            throw new RuntimeException("协议编码错误");
                        }
                        TcpBufferHandlerWrapper tcpBufferHandlerWrapper= new TcpBufferHandlerWrapper(buffer -> {
                            try{
                                ProtocolMessage<RpcResponse> response = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                responseFuture.complete(response.getBody());
                            } catch (Exception e) {
                                throw new RuntimeException("协议解码错误");
                            }
                        });
                        socket.handler(tcpBufferHandlerWrapper);
                    }
                    else{
                        throw new RuntimeException("Tcp Connect Failed");
                    }
                });
        RpcResponse rpcResponse = responseFuture.get();
        netClient.close();
        return rpcResponse;
    }
}
