package com.darren.service.memory;

import java.nio.ByteBuffer;

/**
 * <h3>netty</h3>
 * <p>直接内存</p>
 *
 * @author : Darren
 * @date : 2021年05月27日 08:25:23
 **/
public class DirectMemoryTest {

    public static void main(String[] args) {
        heapAccess();
        directAccess();
    }

    public static void heapAccess() {
        long startTime = System.currentTimeMillis();
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        for (int i = 0; i < 100000; i++) {
            for (int j = 0; j < 200; j++) {
                buffer.putInt(j);
            }
            buffer.flip();

            for (int j = 0; j < 200; j++) {
                buffer.getInt();
            }
            buffer.clear();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("堆内存访问：" + (endTime - startTime) + "ms");
    }

    public static void directAccess(){
        long startTime = System.currentTimeMillis();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1000);
        for (int i = 0; i < 100000; i++) {
            for (int j = 0; j < 200; j++) {
                buffer.putInt(j);
            }
            buffer.flip();

            for (int j = 0; j < 200; j++) {
                buffer.getInt();
            }
            buffer.clear();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("直接内存访问：" + (endTime - startTime) + "ms");
    }

}

