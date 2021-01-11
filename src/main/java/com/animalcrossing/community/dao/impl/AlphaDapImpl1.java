package com.animalcrossing.community.dao.impl;

import com.animalcrossing.community.dao.AlphaDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.net.Socket;
import java.nio.channels.ServerSocketChannel;

@Repository
@Primary
public class AlphaDapImpl1 implements AlphaDao {

    @Override
    public String select() {
        
        return "mybatis";
    }
}
