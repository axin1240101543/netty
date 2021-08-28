package com.darren.service.netty.split;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * <h3>netty</h3>
 * <p></p>
 *
 * @author : Darren
 * @date : 2021年08月28日 17:50:24
 **/
public class NettyClientHandler extends SimpleChannelInboundHandler<MyMessageProtocol> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i < 1000; i++) {
            String content = "你好，我是Darren";
            MyMessageProtocol myMessageProtocol = new MyMessageProtocol();
            myMessageProtocol.setLen(content.getBytes(CharsetUtil.UTF_8).length);
            myMessageProtocol.setContent(content.getBytes(CharsetUtil.UTF_8));
            ctx.writeAndFlush(myMessageProtocol);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MyMessageProtocol msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

