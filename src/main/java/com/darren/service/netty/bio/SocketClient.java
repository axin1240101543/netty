package com.darren.service.netty.bio;

import java.io.IOException;
import java.net.Socket;

/**
 * <h3>netty</h3>
 * <p>BIO客户端</p>
 *
 * @author : Darren
 * @date : 2021年05月21日 21:45:57
 **/
public class SocketClient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8080);
        //向服务端发送数据
        socket.getOutputStream().write("HelloServer".getBytes());
        socket.getOutputStream().flush();
        System.out.println("向服务端发送数据结束");
        byte[] bytes = new byte[1024];
        //接收服务端回传的数据
        socket.getInputStream().read(bytes);
        System.out.println("接收到服务端的数据：" + new String(bytes));
        socket.close();
    }

}

