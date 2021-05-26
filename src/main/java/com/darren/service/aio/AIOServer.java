package com.darren.service.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * <h3>netty</h3>
 * <p>AIO服务端</p>
 *
 * 异步非阻塞， 由操作系统完成后回调通知服务端程序启动线程去处理， 一般适用于连接数较多且连接时间较长的应用
 * 应用场景：
 * AIO方式适用于连接数目多且连接比较长(重操作)的架构，JDK7 开始支持
 *
 * @author : Darren
 * @date : 2021年05月23日 21:19:35
 **/
public class AIOServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        final AsynchronousServerSocketChannel severSocketChannel = AsynchronousServerSocketChannel
                .open()
                .bind(new InetSocketAddress(8080));
        severSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel clientSocketChannel, Object attachment) {
                try {
                    System.out.println("客户端连接成功...ThreadName2:" + Thread.currentThread().getName());
                    //在此接收客户端连接，如果不写这行代码后面的客户端连接不上服务器
                    severSocketChannel.accept(attachment, this);
                    System.out.println("RemoteAddress:" + clientSocketChannel.getRemoteAddress());
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    clientSocketChannel.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer buffer) {
                            System.out.println("ThreadName3:" + Thread.currentThread().getName());
                            buffer.flip();
                            System.out.println("接收到客户端的消息：" + new String(buffer.array(), 0, result));
                            clientSocketChannel.write(ByteBuffer.wrap("HelloClient".getBytes()));
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();
            }
        });
        System.out.println("服务启动成功...ThreadName1:" + Thread.currentThread().getName());

        Thread.sleep(Integer.MAX_VALUE);
    }

}

