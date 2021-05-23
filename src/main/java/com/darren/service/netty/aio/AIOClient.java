package com.darren.service.netty.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * <h3>netty</h3>
 * <p>AIO客户端</p>
 *
 * @author : Darren
 * @date : 2021年05月23日 21:43:12
 **/
public class AIOClient {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080)).get();
        socketChannel.write(ByteBuffer.wrap("Hello Server".getBytes()));
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        Integer length = socketChannel.read(byteBuffer).get();
        if (length != -1){
            System.out.println("客户端接收信息:" + new String(byteBuffer.array(), 0, length));
        }

        Thread.sleep(Integer.MAX_VALUE);
    }

}

