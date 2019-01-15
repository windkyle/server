package com.dyw.queue.service;

import com.dyw.queue.controller.Egci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class EquipmentService {
    private static Logger logger = LoggerFactory.getLogger(EquipmentService.class);

    public static void initEquipmentInfo() {
        try {
            //获取设备ip列表
            ResultSet resultSet = Egci.stmt.executeQuery("select Name,GroupId,IP from Equipment");
            Egci.deviceIps0 = new ArrayList<String>();
            Egci.deviceIps1 = new ArrayList<String>();
            Egci.deviceIps2 = new ArrayList<String>();
            Egci.deviceIps3 = new ArrayList<String>();
            Egci.deviceIps0WithOctothorpe = new ArrayList<String>();
            Egci.deviceIps1WithOctothorpe = new ArrayList<String>();
            Egci.deviceIps2WithOctothorpe = new ArrayList<String>();
            Egci.deviceIps3WithOctothorpe = new ArrayList<String>();
            Egci.deviceIps0Map = new HashMap<String, String>();
            while (resultSet.next()) {
                //如果对象中有数据，就会循环打印出来
                Egci.deviceIps0Map.put(resultSet.getString("IP"), resultSet.getString("Name"));
                Egci.deviceIps0.add(resultSet.getString("IP"));
                Egci.deviceIps0WithOctothorpe.add("#" + resultSet.getString("IP"));
                if (resultSet.getInt("GroupId") == 2) {
                    Egci.deviceIps1.add(resultSet.getString("IP"));
                    Egci.deviceIps1WithOctothorpe.add("#" + resultSet.getString("IP"));
                } else if (resultSet.getInt("GroupId") == 3) {
                    Egci.deviceIps2.add(resultSet.getString("IP"));
                    Egci.deviceIps2WithOctothorpe.add("#" + resultSet.getString("IP"));
                } else if (resultSet.getInt("GroupId") == 4) {
                    Egci.deviceIps3.add(resultSet.getString("IP"));
                    Egci.deviceIps3WithOctothorpe.add("#" + resultSet.getString("IP"));
                }
            }
            logger.info("所有设备ip：" + String.valueOf(Egci.deviceIps0WithOctothorpe));
            logger.info("一核设备ip：" + String.valueOf(Egci.deviceIps1WithOctothorpe));
            logger.info("二核设备ip：" + String.valueOf(Egci.deviceIps2WithOctothorpe));
            logger.info("三核设备ip：" + String.valueOf(Egci.deviceIps3WithOctothorpe));
        } catch (Exception e) {
            logger.error("连接数据库和获取全部设备IP失败：", e);
        }
    }

    public static void initEquipmentAlarm() {
        //对所有一体机设备进行布防
        for (String deviceIp : Egci.deviceIpsOn) {
            LoginService loginService = new LoginService();
            loginService.login(deviceIp, Egci.configEntity.getDevicePort(), Egci.configEntity.getDeviceName(), Egci.configEntity.getDevicePass());
            AlarmService alarmService = new AlarmService();
            if (!alarmService.setupAlarmChan(loginService.getlUserID())) {
                Egci.deviceIpsAlarmFail.add(deviceIp);
            }
        }
    }
}
