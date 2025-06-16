package com.stupm.core.handler;

import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.protocol.ProtocolMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private final Object responseLock = new Object();
    private RpcResponse resp;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        synchronized (responseLock) {
            if(msg instanceof ProtocolMessage<?>) {
                ProtocolMessage<?> response = (ProtocolMessage<?>)msg;
                resp = (RpcResponse) response.getBody();
            }
        }
    }

    public RpcResponse getResponse() throws InterruptedException {
        synchronized (responseLock) {
            if(resp == null) {
                responseLock.wait();
            }
            return resp;
        }
    }
}
