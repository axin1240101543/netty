package com.darren.service.netty.reconnect;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * <h3>netty</h3>
 * <p>Netty客户端</p>
 *
 * @author : Darren
 * @date : 2021年05月25日 08:16:36
 * 断线重连(启动)
 **/
public class NettyClient {

    private String host;
    private int port;
    private EventLoopGroup group = null;
    private Bootstrap bootstrap = null;

    public static void main(String[] args) throws InterruptedException {
        NettyClient nettyClient = new NettyClient("localhost", 8080);
        nettyClient.connect();
    }

    public NettyClient(){

    }

    public NettyClient(String host, int port){
        this.host = host;
        this.port = port;
        init();
    }

    public void init() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new NettyClientHandler(NettyClient.this));
                    }
                });
    }

    public void connect() throws InterruptedException {
        System.out.println("netty client start");
        ChannelFuture cf = bootstrap.connect(host, port);
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()){
                    future.channel().eventLoop().schedule(() -> {
                        System.out.println("重连服务器");
                        try {
                            connect();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }, 3000, TimeUnit.MICROSECONDS);
                }else{
                    System.out.println("重连服务器成功");
                }
            }
        });
        cf.channel().closeFuture().sync();
    }

    static class NettyClientHandler extends ChannelInboundHandlerAdapter {

        private NettyClient nettyClient;

        public NettyClientHandler(){

        }

        public NettyClientHandler(NettyClient nettyClient){
            this.nettyClient = nettyClient;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ByteBuf byteBuf = Unpooled.copiedBuffer("HelloServer", CharsetUtil.UTF_8);
            ctx.writeAndFlush(byteBuf);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.err.println("运行中断线重连");
            nettyClient.connect();
        }

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

