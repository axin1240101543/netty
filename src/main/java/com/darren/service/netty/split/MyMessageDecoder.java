package com.darren.service.netty.split;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * <h3>netty</h3>
 * <p></p>
 *
 * @author : Darren
 * @date : 2021年08月28日 16:32:24
 **/
public class MyMessageDecoder extends ByteToMessageDecoder {

    int length = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("MyMessageDecoder#decode invoked...");
        //一个int 四个字节
        //long double 8 float int 4 short char 2 boolean byte 1
        if (in.readableBytes() >= 4){
            if (length == 0){
                length = in.readInt();
            }
            if (in.readableBytes() < length){
                System.out.println("当前可读数据不够，继续等待！！！");
                return;
            }
            byte[] content = new byte[length];
            if (in.readableBytes() >= length){
                in.readBytes(content);

                //封装成MyMessageProtocol对象，传递到下一个handler业务处理
                MyMessageProtocol myMessageProtocol = new MyMessageProtocol();
                myMessageProtocol.setLen(length);
                myMessageProtocol.setContent(content);
                out.add(myMessageProtocol);
            }
        }
        length = 0;
    }
}

