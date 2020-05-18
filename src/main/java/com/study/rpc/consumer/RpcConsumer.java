package com.study.rpc.consumer;

import com.study.rpc.api.IRpcHello;
import com.study.rpc.consumer.proxy.RpcProxy;

import java.lang.reflect.Proxy;

public class RpcConsumer {

    public static void main(String[] args) {

        IRpcHello rpcHello = RpcProxy.create(IRpcHello.class);

        String result = rpcHello.hello("小菜哥");

        System.out.println(result);

    }

}
