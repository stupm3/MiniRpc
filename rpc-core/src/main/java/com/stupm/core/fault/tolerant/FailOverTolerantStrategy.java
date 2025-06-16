package com.stupm.core.fault.tolerant;

import cn.hutool.core.collection.CollectionUtil;
import com.stupm.core.RpcApplication;
import com.stupm.core.config.RpcConfig;
import com.stupm.core.fault.retry.RetryStrategy;
import com.stupm.core.fault.retry.RetryStrategyFactory;
import com.stupm.core.loadbalancer.LoadBalancer;
import com.stupm.core.loadbalancer.LoadBalancerFactory;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.model.ServiceMetaInfo;
import com.stupm.core.server.tcp.NettyRpcClient;
import com.stupm.core.server.tcp.VertxTcpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FailOverTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        RpcRequest req = (RpcRequest) context.get("rpcRequest");
        List<ServiceMetaInfo> services = (List<ServiceMetaInfo>)context.get("services");
        ServiceMetaInfo selectedService = (ServiceMetaInfo) context.get("service");

        removeDeadNode(selectedService , services);

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName" , req.getMethodName());
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());

        RpcResponse response = null;
        while(!services.isEmpty() || response == null ){
            ServiceMetaInfo service = loadBalancer.select(requestParams, services);
            System.out.println("获取节点 :" +  service);
            try{
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
                response = retryStrategy.doRetry(() ->
                        NettyRpcClient.doRequest(req, service)
                );
                return response;
            } catch (Exception ex) {
                removeDeadNode(service , services);
            }
        }
        throw new RuntimeException(e);
    }

    private void removeDeadNode(ServiceMetaInfo service , List<ServiceMetaInfo> Nodes) {
            if(CollectionUtil.isNotEmpty(Nodes)) {
                for(ServiceMetaInfo node: Nodes) {
                    if(node .getServiceNodeKey().equals(service.getServiceNodeKey())) {
                        Nodes.remove(node);
                    }
                }
            }
    }
}
