package com.darren.service.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <h3>netty</h3>
 * <p>NIO服务端</p>
 *
 * 同步非阻塞，服务器实现模式为一个线程可以处理多个请求(连接)，客户端发送的连接请求都会注册到多路复用器selector上，多路复用
 * 器轮询到连接有IO请求就进行处理，JDK1.4开始引入。
 *
 * 缺点：
 * 1、外层循环会导致CPU100%
 * 2、如果连接数太多的话，会有大量的无效遍历，假如有10000个连接，其中只有1000个连接有写数据，但是由于其他9000个连接并
 * 没有断开，我们还是要每次轮询遍历一万次，其中有十分之九的遍历都是无效的，这显然不是一个让人很满意的状态。
 *
 *
 * 应用场景：
 * NIO方式适用于连接数目多且连接比较短（轻操作） 的架构， 比如聊天服务器， 弹幕系统， 服务器间通讯，编程比较复杂
 *

 * @author : Darren
 * @date : 2021年05月21日 22:13:55
 **/
public class NIOServer {

    private static List<SocketChannel> clientSocketList =  new ArrayList();

    public static void main(String[] args) throws IOException {
        //创建NIO ServerSocketChannel   和BIO的ServerSocket类似
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(8080));
        //配置ServerSocketChannel为非阻塞，如配置为阻塞，则和BIO一样
        serverSocket.configureBlocking(false);
        System.out.println("服务启动成功...");

        while (true){
            //非阻塞模式accept方法不会阻塞，否则会阻塞
            //NIO的非阻塞是有操作系统内部实现的，底层调用了linux内核的accept函数
            SocketChannel clientSocket = serverSocket.accept();
            if (clientSocket != null){
                System.out.println("连接成功...");
                //设置SocketChannel为非阻塞
                clientSocket.configureBlocking(false);
                //将客户端保存在List中
                clientSocketList.add(clientSocket);
            }

            //遍历所有客户端进行数据读取
            Iterator<SocketChannel> iterator = clientSocketList.iterator();
            while (iterator.hasNext()){
                SocketChannel sc = iterator.next();
                ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                //非阻塞模式read方法不会阻塞，否则会阻塞
                int length = sc.read(byteBuffer);
                //如果有数据，把数据打印出来
                if (length > 0){
                    System.out.println("接收到消息：" + new String(byteBuffer.array()));
                }else if (length == -1){
                    //如果客户端断开连接，把SocketChannel从集合中去掉
                    iterator.remove();
                    System.out.println("客户端断开连接...");
                }
            }
        }
    }

}

