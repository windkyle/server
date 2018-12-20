package com.dyw.queue.controller;

import com.dyw.queue.entity.ConfigEntity;
import com.dyw.queue.service.DatabaseService;
import com.dyw.queue.service.LoginService;
import com.dyw.queue.service.SynchronizationService;
import com.dyw.queue.tool.Tool;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        ConfigEntity configEntity = Tool.getConfig("C:\\software\\server\\config\\test.xml");
        List<String> cards = new ArrayList<String>();
        //连接数据库
        DatabaseService databaseService = new DatabaseService(configEntity.getDataBaseIp(), configEntity.getDataBasePort(), configEntity.getDataBaseName(), configEntity.getDataBasePass(), configEntity.getDataBaseLib());
        try {
            Statement stmt = databaseService.connection().createStatement();
            //获取设备ip列表
            ResultSet resultSet = stmt.executeQuery("select CardNumber from Staff");
            while (resultSet.next()) {
                cards.add(resultSet.getString("CardNumber"));
            }
            System.out.println("数据库人员数量" + cards.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoginService loginService = new LoginService();
        loginService.login(configEntity.getTestIp(), (short) 8000, "admin", "hik12345");
        SynchronizationService synchronizationService = new SynchronizationService(configEntity.getTestIp(), loginService.getlUserID(), configEntity, cards);
        synchronizationService.start();
    }
}