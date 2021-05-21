package com.darren.service.netty.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <h3>netty</h3>
 * <p>BIO服务端</p>
 * 同步阻塞模型，一个客户端连接对应一个处理线程
 *
 * 缺点：
 * 1、IO代码里的accept、read操作是阻塞操作，如果连接不做数据读写操作会导致线程阻塞，浪费资源
 * 2、如果线程很多，会导致服务器线程太多，压力太大，比如C10k问题
 *
 * 应用场景：
 * BIO方式适用于连接数目比较小且固定的架构，这种方式对服务器资源要求比较高，但程序简单易理解
 *
 * 优化：可使用线程池
 *
 * @author : Darren
 * @date : 2021年05月21日 21:26:27
 **/
public class SocketServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true){
            System.out.println("等待连接...");
            //阻塞方法
            //接收客户端的连接，没有客户端连接就阻塞
            Socket clientSocket = serverSocket.accept();
            System.out.println("有客户端连接了...");
            //handler(clientSocket);
            //优化
            ExecutorService executorService = Executors.newFixedThreadPool(30);
            executorService.submit(() -> handler(clientSocket));
        }
    }

    private static void handler(Socket clientSocket){
        byte[] bytes = new byte[1024];
        System.out.println("准备read...");
        //阻塞方法
        //接收客户端的数据，没有数据可读时就阻塞
        int read = 0;
        try {
            read = clientSocket.getInputStream().read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("读取完毕...");
        if (read != -1){
            System.out.println("接收到客户端的数据：" + new String(bytes));
        }
        //给服务端输出HelloClient
        try {
            clientSocket.getOutputStream().write("HelloClient".getBytes());
            clientSocket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

