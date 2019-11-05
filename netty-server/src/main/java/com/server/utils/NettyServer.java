package com.server.utils;

import com.server.handler.HeartBeatRequestHandler;
import com.server.handler.MessageRequestHandler;
import com.server.handler.IMIdleStateHandler;
import com.server.handler.PacketCodecHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * @ClassName NettyServer
 * @Description: TODO
 * @Author yanlin
 * @Date 2019/11/1
 * @Version V1.0
 **/
@Component
public class NettyServer {
    private static final int PORT = 8087;

    @PostConstruct
    public static void run() {
        NioEventLoopGroup boss = new NioEventLoopGroup();//表示监听端口,接受新连接线程，主要负责创建新连接
        NioEventLoopGroup worker = new NioEventLoopGroup();//负责读取每条数据的线程，主要用于读取数据以及业务逻辑处理
        final ServerBootstrap serverBootstrap = new ServerBootstrap();//创建了一个引导类，进行服务端启动工作
        serverBootstrap
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
                        // 空闲检测
                        ch.pipeline().addLast(new IMIdleStateHandler());
                        ch.pipeline().addLast(new Spliter());
                        ch.pipeline().addLast(PacketCodecHandler.INSTANCE);
                        ch.pipeline().addLast(MessageRequestHandler.INSTANCE);
                        ch.pipeline().addLast(HeartBeatRequestHandler.INSTANCE);
                    }
                });


        bind(serverBootstrap, PORT);
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println(new Date() + ": 端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
            }
        });
    }
}
