package com.darren.service.netty.codec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * <h3>netty</h3>
 * <p>Netty服务端</p>
 *
 * @author : Darren
 * @date : 2021年05月24日 08:08:31
 **/
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
                            pipeline.addLast(new NettyServerHandler());
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

     static class NettyServerHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("客户端连接通道建立完成...");
        }

         @Override
         public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
             System.out.println("服务器读取线程：" + Thread.currentThread().getName());
             ByteBuf byteBuf = (ByteBuf) msg;
             byte[] bytes = new byte[byteBuf.readableBytes()];
             byteBuf.readBytes(bytes);
             System.out.println("客户端发送的消息是：" + ProtostuffUtil.deserializer(bytes, User.class));
         }

         @Override
         public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
             ByteBuf byteBuf = Unpooled.copiedBuffer(ProtostuffUtil.serializer(new User(1L, "server", 18)));
             ctx.writeAndFlush(byteBuf);
         }

         @Override
         public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
             ctx.close();
         }
     }

}

