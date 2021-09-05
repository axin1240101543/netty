package com.darren.service.netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


public class NettyServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            //IdleStateHandler的readerIdleTime参数指定超过3秒还没收到客户端的连接，
                            //会触发IdleStateEvent事件并且交给下一个handler处理，下一个handler必须
                            //实现userEventTriggered方法处理对应事件
                            pipeline.addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new HeartBeatServerHandler());
                        }
                    });
            System.out.println("netty server start...");
            ChannelFuture cf = bootstrap.bind(8080).sync();
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static int readIdleTimes = 0;

     static class HeartBeatServerHandler extends SimpleChannelInboundHandler<String>{
         @Override
         protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
             System.out.println("server receive :" + msg);
             if ("I am is heart beat package !!!".equals(msg)){
                 ctx.channel().writeAndFlush("ok");
             }else{
                 System.out.println("处理其他的msg！");
             }
         }

         @Override
         public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
             IdleStateEvent event = (IdleStateEvent) evt;
             String eventType = null;
             switch (event.state()) {
                 case READER_IDLE:
                     eventType = "read idle";
                     // 读空闲的计数加1
                     readIdleTimes++;
                     break;
                 case WRITER_IDLE:
                     eventType = "write idle";
                     break;
                 case ALL_IDLE:
                     eventType = "all idle";
                     break;
             }

             if (readIdleTimes > 3){
                 System.out.println("read idle 3 times, close connect!");
                 ctx.channel().writeAndFlush("idle close");
                 ctx.channel().close();
             }
         }
     }

}

