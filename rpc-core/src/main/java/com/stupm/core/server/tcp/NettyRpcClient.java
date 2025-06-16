package com.stupm.core.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.stupm.core.RpcApplication;
import com.stupm.core.constant.ProtocolConstants;
import com.stupm.core.handler.MessageCodecSharable;
import com.stupm.core.handler.RpcClientHandler;
import com.stupm.core.model.PingMessage;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.model.ServiceMetaInfo;
import com.stupm.core.protocol.ProtocolMessage;
import com.stupm.core.protocol.ProtocolMessageTypeEnum;
import com.stupm.core.protocol.ProtocolSerializerEnum;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.CompletableFuture;

public class NettyRpcClient {
    private static Channel channel = null;
    public static  void bind(ServiceMetaInfo service) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new MessageCodecSharable());
                            channel.pipeline().addLast(new RpcClientHandler());
                        }
                    });
            System.out.println("Tcp Connect Succeed");
            channel = bootstrap.connect(service.getServiceHost(), service.getServicePort()).sync().channel();
            channel.closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    public static  RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo service) throws InterruptedException {
        bind(service);
        ProtocolMessage<RpcRequest> request = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(ProtocolConstants.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstants.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        header.setRequestId(IdUtil.getSnowflakeNextId());
        request.setHeader(header);
        request.setBody(rpcRequest);
        channel.writeAndFlush(request);
        return channel.pipeline().get(RpcClientHandler.class).getResponse();
    }
}
