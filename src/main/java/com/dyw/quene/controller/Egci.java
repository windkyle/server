package com.dyw.quene.controller;

import com.dyw.quene.service.CustomerService;
import com.dyw.quene.service.DatabaseService;
import com.dyw.quene.service.LoginService;
import com.dyw.quene.service.ProducerService;
import com.dyw.quene.entity.StaffEntity;
import net.iharder.Base64;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class Egci {
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(Egci.class.getName());
        //初始化登陆对象
        LoginService loginService = new LoginService();
        //初始化人员实体
        StaffEntity staff = new StaffEntity();
        //ip列表
        String[] deviceIps = {"#192.168.40.25"};
        //连接数据库
        DatabaseService databaseService = new DatabaseService();
        Connection dbConn = databaseService.connection();
        //初始化下发队列
        ProducerService producerService = new ProducerService();
        CustomerService customerService = new CustomerService();
        customerService.customer();
        //开启socket服务
        ServerSocket socket = new ServerSocket(12345);
        logger.info("启动服务器....");
        //保持监听
        while (true) {
            Socket socketInfo = socket.accept();
            logger.info("客户端:" + socketInfo.getInetAddress().getHostAddress() + "已连接到服务器");
            //读取客户端发送来的信息
            BufferedReader br = new BufferedReader(new InputStreamReader(socketInfo.getInputStream()));
            String meseng = br.readLine();
            String staffInfo = "";//结构体信息
            String operationCode = meseng.substring(0, 1);
            //读取数据库信息
            if (operationCode.equals("1")) {
                Statement stmt = dbConn.createStatement();
                ResultSet rs = stmt.executeQuery("select CardNumber,Name,Photo from Staff WHERE CardNumber = " + meseng.substring(2));
                while (rs.next()) {//如果对象中有数据，就会循环打印出来
                    staff.setName(rs.getString("name"));
                    staff.setCardNumber(rs.getString("cardNumber"));
                    staff.setPhoto(rs.getBytes("Photo"));
                }
                //重新组织人员信息:操作码+卡号+名称+图片
                staffInfo = "1#" + staff.getCardNumber() + "#" + staff.getName() + "#" + Base64.encodeBytes(staff.getPhoto());
            } else {
                staffInfo = "2#" + staff.getCardNumber() + "#test#none";
            }
            //发送消息到队列中
            for (String deviceIp : deviceIps) {
                producerService.sendToQuene(staffInfo.concat(deviceIp));
            }
            //返回消息给客户端
            OutputStream ops = socketInfo.getOutputStream();
            OutputStreamWriter opsw = new OutputStreamWriter(ops);
            BufferedWriter bw = new BufferedWriter(opsw);
            bw.write("succeseng\r\n\r\n");
            bw.flush();
            br.close();
            bw.close();
            socketInfo.close();
        }
    }
}
