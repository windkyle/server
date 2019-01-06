package com.dyw.queue.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dyw.queue.entity.TemporaryStaffEntity;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
//        List<TemporaryStaffEntity> temporaryStaffEntityList = new ArrayList<TemporaryStaffEntity>();
//        for (int i = 0; i < 3; i++) {
        TemporaryStaffEntity temporaryStaffEntity = new TemporaryStaffEntity();
        temporaryStaffEntity.setCardNumber("123456");
        temporaryStaffEntity.setName("egci");
//            temporaryStaffEntityList.add(temporaryStaffEntity);
//        }
        String json = JSON.toJSONString(temporaryStaffEntity);
        System.out.println(JSON.toJSONString(temporaryStaffEntity));
        temporaryStaffEntity = JSON.parseObject(json, new TypeReference<TemporaryStaffEntity>() {
        });
        System.out.println(temporaryStaffEntity.getName());
    }
}