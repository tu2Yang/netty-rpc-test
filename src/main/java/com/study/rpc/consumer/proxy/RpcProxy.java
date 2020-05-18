package com.study.rpc.consumer.proxy;
import java.lang.reflect.Proxy;

public class RpcProxy {

    public static <T> T create(Class<?> clazz) {

        MethodProxy methodProxy = new MethodProxy(clazz);
        T result = (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, methodProxy);
        return result;
    }

}
