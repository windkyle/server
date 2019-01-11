package com.dyw.queue.service;

import com.dyw.queue.controller.Egci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

public class TaskService extends TimerTask {
    private Logger logger = LoggerFactory.getLogger(TaskService.class);

    public void run() {
        List<String> cards = new ArrayList<String>();//数据库人员信息
        //连接数据库
        DatabaseService databaseService = new DatabaseService(Egci.configEntity.getDataBaseIp(), Egci.configEntity.getDataBasePort(), Egci.configEntity.getDataBaseName(), Egci.configEntity.getDataBasePass(), Egci.configEntity.getDataBaseLib());
        try {
            Statement stmt = databaseService.connection().createStatement();
            //获取设备ip列表
            String sql = "select CardNumber from Staff WHERE CardNumber != '0'";
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                cards.add(resultSet.getString("CardNumber"));
            }
        } catch (Exception e) {
            logger.error("查询数据库人员信息出错：", e);
        }
        //1：单台启用同步；2：全部启用同步
        if (Egci.configEntity.getSynchronization().equals("1")) {
            Egci.deviceIps = Collections.singletonList(Egci.configEntity.getTestIp());
        }
        for (String deviceIp : Egci.deviceIps) {
            LoginService loginService = new LoginService();
            loginService.login(deviceIp, Egci.configEntity.getDevicePort(), Egci.configEntity.getDeviceName(), Egci.configEntity.getDevicePass());
            if (loginService.getlUserID().longValue() > -1) {
                SynchronizationService synchronizationService = new SynchronizationService(deviceIp, loginService.getlUserID(), Egci.configEntity, cards);
                synchronizationService.start();
                try {
                    Thread.sleep(Egci.configEntity.getDataBaseTime());//避免同时查询数据库
                } catch (InterruptedException e) {
                    logger.error("开启同步失败：", e);
                }
            } else {
                logger.error(deviceIp + "：同步失败：设备不在线或网络异常");
            }
        }
    }
}