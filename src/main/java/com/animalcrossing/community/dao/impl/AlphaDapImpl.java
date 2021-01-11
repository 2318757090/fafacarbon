package com.animalcrossing.community.dao.impl;

import com.animalcrossing.community.dao.AlphaDao;
import org.springframework.stereotype.Repository;

@Repository("alphaH")
public class AlphaDapImpl implements AlphaDao {
    @Override
    public String select() {
         return "11";
    }
}
