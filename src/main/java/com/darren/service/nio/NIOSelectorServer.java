package com.darren.service.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * <h3>netty</h3>
 * <p>使用Selector的NIO服务端</p>
 *
 * NIO有三大核心组件：Channel(通道)，Buffer(缓冲区)，Selector(多路复用器)
 * 1、channel类似于流，每个channel对应一个buffer缓冲区，buffer底层就是个数组
 * 2、channel会注册到selector上，由selector根据channel读写事件的发生将其交由某个空闲的线程处理
 * 3、NIO的Buffer和channel都是既可以读也可以写
 *
 * NIO底层在JDK1.4版本是用linux的内核函数select()或poll()来实现，跟上面的NioServer代码类似，selector每次都会轮询所有的
 * sockchannel看下哪个channel有读写事件，有的话就处理，没有就继续遍历，JDK1.5开始引入了epoll基于事件响应机制来优化NIO。
 *
 * 总结：NIO整个调用流程就是Java调用了操作系统的内核函数来创建Socket，获取到Socket的文件描述符，再创建一个Selector
 * 对象，对应操作系统的Epoll描述符，将获取到的Socket连接的文件描述符的事件绑定到Selector对应的Epoll文件描述符上，进
 * 行事件的异步通知，这样就实现了使用一条线程，并且不需要太多的无效的遍历，将事件处理交给了操作系统内核(操作系统中断
 * 程序实现)，大大提高了效率。
 *
 * @author : Darren
 * @date : 2021年05月23日 18:37:27
 **/
public class NIOSelectorServer {

    public static void main(String[] args) throws IOException {
        //创建NIO ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        //配置ServerSocketChannel为非阻塞
        serverSocketChannel.configureBlocking(false);

        //打开Selector处理Channel，即创建epoll
        Selector selector = Selector.open();
        //把ServerSocketChannel注册到selector上，并且selector对客户端accept连接操作感兴趣
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务启动成功");

        while (true){
            //阻塞等待需要处理的事件发生
            selector.select();

            //获取selector中注册的全部事件的SelectorKey实例
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            //遍历SelectionKey对事件进行处理
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();

                //如果是OP_ACCEPT事件，则进行连接获取和事件注册
                if (key.isAcceptable()){
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel clientSocketChannel = server.accept();
                    clientSocketChannel.configureBlocking(false);
                    //这里只注册的读事件，如果需要给客户端发送数据可以注册写事件
                    clientSocketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("客户端连接成功...");
                 }else if (key.isReadable()){
                    SocketChannel clientSocketChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                    int length = clientSocketChannel.read(byteBuffer);
                    //如果有数据，把数据打印出来
                    if (length > 0){
                        System.out.println("接收到消息：" + new String(byteBuffer.array()));
                    }else if (length == -1){
                        //如果客户端断开连接，关闭Socket
                        System.out.println("客户端断开连接...");
                        clientSocketChannel.close();
                    }
                }
                //从事件集合中删除本次处理的key，防止下次select重复处理
                iterator.remove();
            }
        }
    }

}

