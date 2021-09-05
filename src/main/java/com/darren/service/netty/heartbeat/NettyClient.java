package com.darren.service.netty.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Random;
import java.util.concurrent.TimeUnit;


public class NettyClient {

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());
                    pipeline.addLast(new HeartBeatClientHandler());
                }
            });
            System.out.println("netty client start");
//            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
//            channelFuture.channel().closeFuture().sync();
            Channel channel = bootstrap.connect("127.0.0.1", 8080).sync().channel();

            //模拟发送心跳包
            //[0,8)秒
            String heartBeatPackage = "I am is heart beat package !!!";
            Random random = new Random();
            if (channel.isActive()){
                int num = random.nextInt(8);
                TimeUnit.SECONDS.sleep(num * 1000);
                channel.writeAndFlush(heartBeatPackage);
            }


        }finally {
            group.shutdownGracefully();
        }
    }

    static class HeartBeatClientHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("client receive :" + msg);
            if (msg != null && msg.equals("idle close")){
                System.out.println("server close connect, client close connect to");
                ctx.channel().closeFuture();
            }
        }
    }
}

