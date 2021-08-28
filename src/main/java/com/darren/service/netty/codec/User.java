package com.darren.service.netty.codec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h3>netty</h3>
 * <p></p>
 *
 * @author : Darren
 * @date : 2021年08月28日 15:54:01
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private long id;

    private String name;

    private Integer age;

}

