package com.stupm.core.model;

import com.stupm.core.constant.RpcConstant;
import com.stupm.core.constants.RpcConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    private String serviceVersion = RpcConstants.DEFAULT_SERVICE_VERSION;

    private String serviceName;

    private String methodeName;

    private String methodName;

    //参数类型列表
    private Class<?>[] parameterTypes;

    //参数列表
    private Object[] args;
}
