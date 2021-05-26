package com.darren.service.netty.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

/**
 * <h3>netty</h3>
 * <p>Netty客户端</p>
 *
 * @author : Darren
 * @date : 2021年05月25日 08:16:36
 **/
public class NettyClient {

    public static void main(String[] args) throws InterruptedException {
        //客户端需要一个事件循环组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //创建客户端启动对象
            //注意客户端使用的不是ServerBootstrap，而是Bootstrap
            Bootstrap bootstrap = new Bootstrap();
            //设置相关参数
            bootstrap.group(group)//设置线程组
                    //使用NioSocketChannel作为客户端的通道实现
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    //加入处理器
                    socketChannel.pipeline().addLast(new NettyClientHandler());
                }
            });
            System.out.println("netty client start");

            //启动客户端去连接服务端
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();

            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }
    }

    static class NettyClientHandler extends ChannelInboundHandlerAdapter {

        /**
         * 当客户端连接服务器完成就会触发该方法
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ByteBuf byteBuf = Unpooled.copiedBuffer("HelloServer", CharsetUtil.UTF_8);
            ctx.writeAndFlush(byteBuf);
        }

        /**
         * 当通道有读取事件时会除法，即服务器发送数据给客户端
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println("收到服务器的消息：" + byteBuf.toString(CharsetUtil.UTF_8));
            System.out.println("服务器的地址：" + ctx.channel().remoteAddress());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}

