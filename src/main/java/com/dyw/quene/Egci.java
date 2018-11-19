package com.dyw.quene;


import com.dyw.quene.entity.StaffEntity;
import net.iharder.Base64;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class Egci {
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(Egci.class.getName());
        //初始化人员实体
        StaffEntity staff = new StaffEntity();
        //ip列表
        String[] deviceIps = {"#192.168.40.25", "#192.168.40.26", "#192.168.40.27", "#192.168.40.40"};
        //连接数据库
        String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String dbURL = "jdbc:sqlserver://localhost:1433;DatabaseName=EntranceGuard";
        String userName = "dyw";
        String userPwd = "hik12345";
        Connection dbConn = null;
        try {
            Class.forName(driverName);
            dbConn = DriverManager.getConnection(dbURL, userName, userPwd);
            logger.info("连接数据库成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("连接失败");
        }
        //初始化队列
        Producer producer = new Producer();
        Customer customer = new Customer();
        customer.customer();
        //开启socket服务
        ServerSocket ss = new ServerSocket(12345);
        logger.info("启动服务器....");
        //保持监听
        while (true) {
            Socket s = ss.accept();
            logger.info("客户端:" + s.getInetAddress().getHostAddress() + "已连接到服务器");
            //读取客户端发送来的信息
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String mess = br.readLine();
            logger.info("客户端发来的信息:" + mess.substring(2));
            //读取数据库信息
            Statement stmt = dbConn.createStatement();
            ResultSet rs = stmt.executeQuery("select CardNumber,Name,Photo from Staff WHERE CardNumber = " + mess.substring(2));
            while (rs.next()) {//如果对象中有数据，就会循环打印出来
                staff.setName(rs.getString("name"));
                staff.setCardNumber(rs.getString("cardNumber"));
                staff.setPhoto(rs.getBytes("Photo"));
            }
            //重新组织人员信息:操作码+卡号+名称+图片
            String staffInfo = mess.substring(0, 1) + "#" + staff.getCardNumber() + "#" + staff.getName() + "#" + Base64.encodeBytes(staff.getPhoto());
            //发送消息到队列中
            for (int i = 0; i < deviceIps.length; i++) {
                producer.sendToQuene(staffInfo.concat(deviceIps[i]));
            }
            //返回消息给客户端
            OutputStream ops = s.getOutputStream();
            OutputStreamWriter opsw = new OutputStreamWriter(ops);
            BufferedWriter bw = new BufferedWriter(opsw);
            bw.write("success\r\n\r\n");
            bw.flush();
            br.close();
            bw.close();
            s.close();
        }
    }
}
