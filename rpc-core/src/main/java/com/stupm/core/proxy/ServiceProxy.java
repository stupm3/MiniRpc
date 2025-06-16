package com.stupm.core.proxy;

import cn.hutool.core.collection.CollUtil;
import com.stupm.core.RpcApplication;
import com.stupm.core.config.RpcConfig;
import com.stupm.core.constant.RpcConstant;
import com.stupm.core.fault.retry.RetryStrategy;
import com.stupm.core.fault.retry.RetryStrategyFactory;
import com.stupm.core.fault.tolerant.TolerantStrategy;
import com.stupm.core.fault.tolerant.TolerantStrategyFactory;
import com.stupm.core.loadbalancer.LoadBalancer;
import com.stupm.core.loadbalancer.LoadBalancerFactory;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.model.ServiceMetaInfo;
import com.stupm.core.registry.Registry;
import com.stupm.core.registry.RegistryFactory;

import com.stupm.core.server.tcp.NettyRpcClient;
import com.stupm.core.server.tcp.VertxTcpClient;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodeName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getRegistry(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if(CollUtil.isEmpty(serviceMetaInfos)) {
                throw new RuntimeException("暂无服务地址");
            }

            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName" , rpcRequest.getMethodName());
            ServiceMetaInfo service = loadBalancer.select(requestParams, serviceMetaInfos);

        RpcResponse rpcResponse = null;
        try {
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                    NettyRpcClient.doRequest(rpcRequest , service)
            );
        } catch (Exception e) {
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            Map<String , Object> requestTolerantParams = new HashMap<>();
            requestTolerantParams.put("rpcRequest" , rpcRequest);
            requestTolerantParams.put("services" ,serviceMetaInfos);
            requestTolerantParams.put("service" , service);
            rpcResponse = tolerantStrategy.doTolerant(requestTolerantParams , e);
        }
        return  rpcResponse.getData();
    }
}