package com.darren.service.netty.codec;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>netty</h3>
 * <p></p>
 *
 * @author : Darren
 * @date : 2021年08月28日 15:33:49
 * Netty提供了很多编解码器，比如编解码字符串的StringEncoder和StringDecoder，编解码对象的ObjectEncoder和ObjectDecoder等。
 * 如果要实现高效的编解码可以用protobuf，但是protobuf需要维护大量的proto文件比较麻烦，现在一般可以使用protostuff。
 * protostuff是一个基于protobuf实现的序列化方法，它较于protobuf最明显的好处是，在几乎不损耗性能的情况下做到了不用我们写.proto文件来实现序列化。
 **/
public class ProtostuffUtil {

    private static Map<Class<?>, Schema<?>> cacheSchema = new ConcurrentHashMap<>();

    private static <T> Schema<T> getSchema(Class<T> clazz){
        Schema<T> schema = (Schema<T>) cacheSchema.get(clazz);
        if (schema == null){
            schema = RuntimeSchema.getSchema(clazz);
            if (schema != null){
                cacheSchema.put(clazz, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] serializer(T obj){
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try{
            Schema<T> schema = getSchema(clazz);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(), e);
        }finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserializer(byte[] data, Class<T> clazz){
        try {
            T obj = clazz.newInstance();
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(data, obj, schema);
            return obj;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }



}

