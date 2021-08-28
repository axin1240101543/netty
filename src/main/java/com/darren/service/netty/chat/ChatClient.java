package com.darren.service.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

/**
 * <h3>netty</h3>
 * <p></p>
 *
 * @author : Darren
 * @date : 2021年05月26日 07:34:58
 **/
public class ChatClient {

    public static void main(String[] args) throws Exception {
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
                            //加入特殊分隔符分包解码器
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            /*pipeline.addLast(new DelimiterBasedFrameDecoder(
                                    1024, Unpooled.copiedBuffer("_".getBytes())));*/
                            //向pipeline加入解码器
                            pipeline.addLast("decoder", new StringDecoder());
                            //向pipeline加入编码器
                            pipeline.addLast("encoder", new StringEncoder());
                            //加入自己的业务处理handler
                            pipeline.addLast(new ChatClientHandler());
                        }
                    });
            System.out.println("netty client start");
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            //得到channel
            Channel channel = channelFuture.channel();
            System.out.println("===========" + channel.localAddress() + "================");
            //客户端需要输入信息，创建一个扫描器
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()){
                String msg = scanner.nextLine();
                //通过channel发送到服务器端
                channel.writeAndFlush(msg);
            }
        }finally {
            group.shutdownGracefully();
        }
    }

    static class ChatClientHandler extends SimpleChannelInboundHandler<String>{
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
            System.out.println(msg.trim());
        }
    }

}

