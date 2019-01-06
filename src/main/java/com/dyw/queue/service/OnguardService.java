package com.dyw.queue.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dyw.queue.controller.Egci;
import com.dyw.queue.entity.TemporaryStaffEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.ResultSet;

public class OnguardService extends Thread {
    private static Logger logger = LoggerFactory.getLogger(OnguardService.class);

    @Override
    public void run() {
        try {
            Socket socket = new Socket("127.0.0.1", 9090);
            //接口服务端信息
            System.out.println("waiting...");
            while (true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String info = br.readLine();
                TemporaryStaffEntity temporaryStaffEntity = JSON.parseObject(info, new TypeReference<TemporaryStaffEntity>() {
                });
                assert temporaryStaffEntity != null;
                System.out.println(temporaryStaffEntity.getName());
                String sql = "INSERT INTO TemporaryStaff (CardId,CardNumber,UserId,Name,NameEn,Company,Sex,Birthday) VALUES (" + temporaryStaffEntity.getCardId() + "," + temporaryStaffEntity.getCardNumber() + "," + temporaryStaffEntity.getUserId() + "," + temporaryStaffEntity.getName() + "," + temporaryStaffEntity.getNameEn() + "," + temporaryStaffEntity.getCompany() + "," + temporaryStaffEntity.getSex() + "," + temporaryStaffEntity.getBirthday() + ")";
                System.out.println(sql);
                if (Egci.stmt.execute(sql)) {
                    System.out.println("插入成功");
                }
            }
        } catch (Exception e) {
            logger.error("接收onGuard数据出错：", e);
        }
    }
}