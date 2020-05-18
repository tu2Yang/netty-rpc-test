package com.study.rpc.core.msg;

import lombok.Data;

import java.io.Serializable;

@Data
public class InvokerMsg implements Serializable {

    private String className;       // 服务名称
    private String methodName;      // 方法名
    private Class<?>[] parameters;    // 参数列表
    private Object[] values;        // 实参列表

}
