package com.darren.service.netty.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <h3>netty</h3>
 * <p></p>
 *
 * @author : Darren
 * @date : 2021年05月26日 07:19:54
 **/
public class ChatServer {

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
                            //加入特殊分隔符分包解码器
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //责任链设计模式
                            /*pipeline.addLast(new DelimiterBasedFrameDecoder(
                                    1024, Unpooled.copiedBuffer("_".getBytes())));*/
                            //向pipeline加入解码器
                            pipeline.addLast("decoder", new StringDecoder());
                            //向pipeline加入编码器
                            pipeline.addLast("encoder", new StringEncoder());
                            //加入自己的业务处理handler
                            pipeline.addLast(new ChatServerHandler());
                        }
                    });
            System.out.println("聊天室server启动...");
            ChannelFuture channelFuture = bootstrap.bind(8080).sync();
            //关闭通道
            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    /**
     * 业务处理handler
     */
    static class ChatServerHandler extends SimpleChannelInboundHandler<String> {

        //GlobalEventExecutor.INSTANCE 是全局的事件执行器，是一个单例
        private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        /**
         * 表示channel处于就绪状态，表示上线
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            //将该客户加入聊天的信息推送给其他在线的客户端
            //该方法会将channelGroup中所有的channel遍历，并发送消息
            channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + "上线了 "
                    + sdf.format(new Date()) + "\n");
            channelGroup.add(channel);
            System.out.println(ctx.channel().remoteAddress() + "上线了" + "\n");
        }

        /**
         * 表示channel处于不活动状态，提示离线了
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            //将客户离开信息推送给当前在线的客户
            channelGroup.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + " 下线了" + "\n");
            System.out.println(ctx.channel().remoteAddress() + " 下线了" + "\n");
            System.out.println("channelGroup size=" + channelGroup.size());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            //获取到当前的channel
            Channel channel = ctx.channel();
            //这是我们遍历channelGroup，根据不同的请求，回送不同的消息
            channelGroup.forEach(ch -> {
                if (channel != ch){//不是当前的channel，转发消息
                    ch.writeAndFlush("[客户端]"
                            + channel.remoteAddress() + " 发送了消息：" + msg + "\n");
                }else {//回显自己发送的消息给自己
                    ch.writeAndFlush("[自己]发送了消息：" + msg + "\n");
                }
            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}

