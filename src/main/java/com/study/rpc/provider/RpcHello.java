package com.study.rpc.provider;

import com.study.rpc.api.IRpcHello;

public class RpcHello implements IRpcHello {

    @Override
    public String hello(String msg) {
        String result = "服务端收到: " + msg;
        System.out.println(result);
        return result;
    }

}
