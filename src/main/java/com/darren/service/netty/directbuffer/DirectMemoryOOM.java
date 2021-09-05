package com.darren.service.netty.directbuffer;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * <h3>netty</h3>
 * <p></p>
 *
 * @author : Darren
 * @date : 2021年09月01日 22:29:43
 * jvm直接内存溢出
 * JVM参数：-Xmx20M -XX:MaxDirectMemorySize=10M
 **/
public class DirectMemoryOOM {

    private static final int _1MB = 1024 * 1024;

    public static void main(String[] args) throws IllegalAccessException {
        Field unsafeFiled = Unsafe.class.getDeclaredFields()[0];
        unsafeFiled.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeFiled.get(null);
        while (true){
            unsafe.allocateMemory(_1MB);
        }
    }

}

