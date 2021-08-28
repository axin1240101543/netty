package com.darren.service.netty.split;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * <h3>netty</h3>
 * <p></p>
 *
 * @author : Darren
 * @date : 2021年08月28日 16:30:15
 **/
public class MyMessageEncoder extends MessageToByteEncoder<MyMessageProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MyMessageProtocol msg, ByteBuf out) throws Exception {
        System.out.println("MyMessageEncoder#encode invoked...");
        out.writeInt(msg.getLen());
        out.writeBytes(msg.getContent());
    }
}

