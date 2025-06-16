package com.stupm.provider;

import com.stupm.common.service.UserService;
import com.stupm.core.RpcApplication;

import com.stupm.core.config.RegistryConfig;
import com.stupm.core.config.RpcConfig;
import com.stupm.core.model.ServiceMetaInfo;
import com.stupm.core.registry.LocalRegistry;
import com.stupm.core.registry.Registry;
import com.stupm.core.registry.RegistryFactory;
import com.stupm.core.server.HttpServer;
import com.stupm.core.server.http.VertxHttpServer;
import com.stupm.core.server.tcp.NettyRpcServer;
import com.stupm.core.server.tcp.VertxTcpServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ProviderStarter {
    public static void main(String[] args) {

        RpcApplication.init();

        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName , UserServiceImpl.class);

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try{
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        NettyRpcServer nettyRpcServer = new NettyRpcServer(RpcApplication.getRpcConfig().getServerPort());

//        VertxTcpServer httpServer = new VertxTcpServer();
//        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }


}
