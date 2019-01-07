package com.dyw.queue.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dyw.queue.controller.Egci;
import com.dyw.queue.entity.TemporaryStaffEntity;
import com.dyw.queue.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Statement;

public class OnguardService extends Thread {
    private static Logger logger = LoggerFactory.getLogger(OnguardService.class);
    private Statement stmt;

    public OnguardService() {
        try {
            DatabaseService databaseService = new DatabaseService(Egci.configEntity.getDataBaseIp(), Egci.configEntity.getDataBasePort(), Egci.configEntity.getDataBaseName(), Egci.configEntity.getDataBasePass(), Egci.configEntity.getDataBaseLib());
            stmt = databaseService.connection().createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(Egci.configEntity.getOnGuardIp(), Egci.configEntity.getOnGuardPort());
            //接口服务端信息
            System.out.println("waiting...");
            while (true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String info = br.readLine();
                TemporaryStaffEntity temporaryStaffEntity = JSON.parseObject(info, new TypeReference<TemporaryStaffEntity>() {
                });
                assert temporaryStaffEntity != null;
                System.out.println(temporaryStaffEntity.getType());
                if (temporaryStaffEntity.getType() == 1) {
                    insert(temporaryStaffEntity);
                }
                if (temporaryStaffEntity.getType() == 2) {
                    update(temporaryStaffEntity);
                }
                if (temporaryStaffEntity.getType() == 3) {
                    delete(temporaryStaffEntity);
                }
            }
        } catch (Exception e) {
            logger.error("接收onGuard数据出错：", e);
        }
    }

    /*
     * 新增数据
     * */
    public void insert(TemporaryStaffEntity temporaryStaffEntity) {
        try {
            String sql_staff = "INSERT INTO TemporaryStaff (CardId,CardNumber,UserId,Name,NameEn,Company,Sex,Birthday) VALUES (" + Tool.addQuote(temporaryStaffEntity.getCardId()) + "," + Tool.addQuote(temporaryStaffEntity.getCardNumber()) + "," + Tool.addQuote(temporaryStaffEntity.getUserId()) + "," + Tool.addQuote(temporaryStaffEntity.getName()) + "," + Tool.addQuote(temporaryStaffEntity.getNameEn()) + "," + Tool.addQuote(temporaryStaffEntity.getCompany()) + "," + Tool.addQuote(temporaryStaffEntity.getSex()) + "," + Tool.addQuote(temporaryStaffEntity.getBirthday()) + ")";
            stmt.execute(sql_staff);
            logger.error("onGuard数据新增成功");
        } catch (SQLException e) {
            logger.error("onGuard数据新增失败:", e);
        }
    }

    /*
     * 更新数据
     * */
    public void update(TemporaryStaffEntity temporaryStaffEntity) {
        try {
            //更新人员表信息
            String sql_staff = "UPDATE Staff SET Name=" + Tool.addQuote(temporaryStaffEntity.getName()) + "," + "NameEn=" + Tool.addQuote(temporaryStaffEntity.getNameEn()) + "," + "CardId=" + Tool.addQuote(temporaryStaffEntity.getCardId()) + "," + "CardNumber=" + Tool.addQuote(temporaryStaffEntity.getCardNumber()) + "," + "Birthday=" + Tool.addQuote(temporaryStaffEntity.getBirthday()) + "," + "Sex=" + Tool.addQuote(temporaryStaffEntity.getSex()) + "," + "Company=" + Tool.addQuote(temporaryStaffEntity.getCompany()) + " WHERE CardNumber=" + Tool.addQuote(temporaryStaffEntity.getCardNumber());
            stmt.execute(sql_staff);
            //更新临时表信息
            String sql_temporary = "UPDATE TemporaryStaff SET Name=" + Tool.addQuote(temporaryStaffEntity.getName()) + "," + "NameEn=" + Tool.addQuote(temporaryStaffEntity.getNameEn()) + "," + "CardId=" + Tool.addQuote(temporaryStaffEntity.getCardId()) + "," + "CardNumber=" + Tool.addQuote(temporaryStaffEntity.getCardNumber()) + "," + "Birthday=" + Tool.addQuote(temporaryStaffEntity.getBirthday()) + "," + "Sex=" + Tool.addQuote(temporaryStaffEntity.getSex()) + "," + "Company=" + Tool.addQuote(temporaryStaffEntity.getCompany()) + " WHERE CardNumber=" + Tool.addQuote(temporaryStaffEntity.getCardNumber());
            stmt.execute(sql_temporary);
            logger.error("onGuard数据更新成功");
        } catch (SQLException e) {
            logger.error("onGuard数据更新失败:", e);
        }
    }

    /*
     * 删除数据
     * */
    public void delete(TemporaryStaffEntity temporaryStaffEntity) {
        try {
            //删除人员表数据
            String sql_staff = "DELETE FROM Staff WHERE CardNumber= " + Tool.addQuote(temporaryStaffEntity.getCardNumber());
            stmt.execute(sql_staff);
            String staffInfo = "2#" + temporaryStaffEntity.getCardNumber() + "#test#none";
            //发送删除卡号人脸消息到队列中
            for (int i = 0; i < Egci.deviceIps0.size(); i++) {
                Egci.producerServiceList.get(i).sendToQueue(staffInfo.concat(Egci.deviceIps0.get(i)));
            }
            //删除临时表数据
            String sql_temporary = "DELETE FROM TemporaryStaff WHERE CardNumber= " + Tool.addQuote(temporaryStaffEntity.getCardNumber());
            stmt.execute(sql_temporary);
        } catch (SQLException e) {
            logger.error("onGuard数据删除失败");
        } catch (Exception e) {
            logger.error("onGuard数据删除失败");
        }
    }
}