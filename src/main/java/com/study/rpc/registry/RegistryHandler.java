package com.study.rpc.registry;

import com.study.rpc.core.msg.InvokerMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHandler extends ChannelInboundHandlerAdapter {

    public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String, Object>();

    private final static String CLASS_PATH = "com.study.rpc.provider";

    private List<String> classCache = new ArrayList<String>();

    public RegistryHandler() {
        scanClass(CLASS_PATH);
        doRegister();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerMsg request = (InvokerMsg) msg;
        if (registryMap.containsKey(request.getClassName())) {
            Object clazz = registryMap.get(request.getClassName());
            Method m = clazz.getClass().getMethod(request.getMethodName(), request.getParameters());
            result = m.invoke(clazz, request.getValues());
        }
        ctx.writeAndFlush(result);
        ctx.close();
    }

    // 约定，provider包下的所有类都是对外提供的服务实现类
    // com.study.rpc.provider

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    // 简单实现IOC
    private void scanClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file: dir.listFiles()) {
            if(file.isDirectory()) {
                scanClass(packageName + "." + file.getName());
            }else {
                classCache.add(packageName + "." +file.getName().replace(".class", "").trim());
            }
        }
    }

    // 把扫描到class实例化，放到map中，这就是注册过程
    // 注册的服务名字，叫接口名字
    private void doRegister() {
        if (classCache.size() == 0) {
            return;
        }
        for (String className: classCache) {
            try {
                Class<?> clazz = Class.forName(className);
                // 未考虑实现多个接口，和接口有多个实现类
                Class<?> interfaces = clazz.getInterfaces()[0];
                registryMap.put(interfaces.getName(), clazz.newInstance());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

}
