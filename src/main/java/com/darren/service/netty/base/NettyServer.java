package com.darren.service.netty.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

/**
 * <h3>netty</h3>
 * <p>Netty服务端</p>
 *
 * NIO的类库和API繁杂，使用麻烦：需要熟练掌握Selector、ServerSocketChannel、SocketChannel、ByteBuffer等。
 * 开发工作量和难度都非常大：例如客户端面临断线重连、网络闪断、心跳处理、半包读写、网络拥塞和异常流的处理等等。
 *
 * Netty对JDK自带的NIO的API进行了良好的封装，解决了上述问题。且Netty拥有高性能、吞吐量更高，延迟更低，减少资源消耗，最小化不必要的内存复制等优点。
 * Netty现在都在用的是4.x，5.x版本已经废弃，Netty4.x需要JDK 6以上版本支持
 *
 * Netty的使用场景：
 * 1）互联网行业：在分布式系统中，各个节点之间需要远程服务调用，高性能的RPC框架必不可少，Netty作为异步
 * 高性能的通信框架，往往作为基础通信组件被这些RPC框架使用。典型的应用有：阿里分布式服务框架Dubbo的
 * RPC框架，使用Dubbo协议进行节点间通信，Dubbo协议默认使用Netty作为基础通信组件，用于实现各进程节
 * 点之间的内部通信。Rocketmq底层也是用的Netty作为基础通信组件。
 * 2）游戏行业：无论是手游服务端还是大型的网络游戏，Java语言得到了越来越广泛的应用。Netty作为高性能的基
 * 础通信组件，它本身提供了TCP/UDP和HTTP协议栈。
 * 3）大数据领域：经典的Hadoop的高性能通信和序列化组件Avro的RPC框架，默认采用Netty进行跨界点通
 * 信，它的NettyService基于Netty框架二次封装实现。
 * netty相关开源项目：https://netty.io/wiki/related-projects.html
 *
 * @author : Darren
 * @date : 2021年05月24日 08:08:31
 **/
public class NettyServer {

    public static void main(String[] args) throws InterruptedException {
        //创建两个线程组bossGroup和workerGroup，含有的子线程NioEventLoop的个数默认为cpu核数的两倍
        //bossGroup只是处理连接请求，真正的和客户端业务处理，会交给workerGroup完成
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);

        try {
            //创建服务器端的启动对象
            ServerBootstrap bootstrap = new ServerBootstrap();

            //使用链式编程来配置参数
            bootstrap.group(bossGroup, workerGroup)//设置两个线程组
                    .channel(NioServerSocketChannel.class)//使用NioServerSocketChannel作为服务器的通道实现
                    //初始化服务器连接队列大小，服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接。
                    //多个客户端同时来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //创建通道初始化对象，设置初始化参数
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //对workerGroup的SocketChannel设置处理器
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            System.out.println("netty server start...");

            //绑定一个端口并且同步，生成了一个ChannelFuture异步对象，通过isDone()等方法可以判断异步事件的执行情况
            //启动服务器（并绑定端口），bind是异步操作，sync方法是等待异步操作执行完毕
            ChannelFuture cf = bootstrap.bind(8080).sync();

            if (cf.isDone()){
                System.out.println("事件执行成功...");
            }

            //给cf注册监听器，监听我们关心的事件
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()){
                        System.out.println("监听端口8080成功...");
                    }else {
                        System.out.println("监听端口8080失败...");
                    }
                }
            });

            //对通道关闭进行监听，closeFuture是异步操作，监听通道关闭
            //通过sync方法同步等待通道关闭处理完毕，这里会阻塞等待通道关闭完成
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 自定义Handler需要继承netty绑定好的某个HandlerAdapter（规范）
     */
     static class NettyServerHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("客户端连接通道建立完成...");
        }

        /**
          * 读取客户端发送的数据
          * @param ctx 上下文对象，含有通道channel，管道pipeline
          * @param msg 客户端发送的数据
          * @throws Exception
          */
         @Override
         public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
             System.out.println("服务器读取线程：" + Thread.currentThread().getName());
//             Channel channel = ctx.channel();
//             ChannelPipeline pipeline = ctx.pipeline(); //本质上是一个双线链表，出栈入栈
             //将msg转成一个ByteBuf，类似NIO的ByteBuffer
             ByteBuf byteBuf = (ByteBuf) msg;
             System.out.println("客户端发送的消息是：" + byteBuf.toString(CharsetUtil.UTF_8));
         }

         /**
          * 数据读取完毕处理方法
          * @param ctx
          * @throws Exception
          */
         @Override
         public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
             ByteBuf byteBuf = Unpooled.copiedBuffer("HelloClient", CharsetUtil.UTF_8);
             ctx.writeAndFlush(byteBuf);
         }

         /**
          * 处理异常，一般是需要关闭通道
          * @param ctx
          * @param cause
          * @throws Exception
          */
         @Override
         public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
             ctx.close();
         }
     }

}

