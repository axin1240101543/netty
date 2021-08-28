package com.darren.service.netty.split;

import lombok.Data;

/**
 * <h3>netty</h3>
 * <p></p>
 *
 * @author : Darren
 * @date : 2021年08月28日 16:29:11
 * 自定义协议包
 **/
@Data
public class MyMessageProtocol {

    /**
     * 消息的长度
     */
    private int len;

    /**
     * 消息的内容
     */
    private byte[] content;

}

