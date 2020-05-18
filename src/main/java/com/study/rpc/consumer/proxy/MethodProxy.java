package com.study.rpc.consumer.proxy;

import com.study.rpc.core.msg.InvokerMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MethodProxy implements InvocationHandler {

    private Class<?> clazz;

    public MethodProxy(Class<?> clazz) {
        this.clazz = clazz;
    }

    // 这里的代理invoke，实际上是网络远程调用，和本地代理不同（伪代理）
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 如果传进来的是一个已经实现的具体类（在本地，不需要远程调用）
        if(Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }else {
            // 如果传进来的是接口，则走远程调用
            return this.rpcInvoke(method, args);
        }
    }

    public Object rpcInvoke(Method method, Object[] args) {
        InvokerMsg msg = new InvokerMsg();

        msg.setClassName(this.clazz.getName());
        msg.setMethodName(method.getName());
        msg.setParameters(method.getParameterTypes());
        msg.setValues(args);

        EventLoopGroup group = new NioEventLoopGroup();

        final RpcProxyHandler handler = new RpcProxyHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,4, 0, 4));
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            pipeline.addLast("encoder", new ObjectEncoder());
                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(handler);
                        }
                    });
            ChannelFuture future = bootstrap.connect("localhost", 8080).sync();
            future.channel().writeAndFlush(msg).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

        return handler.getResult();
    }

}
