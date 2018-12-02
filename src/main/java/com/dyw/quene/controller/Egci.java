package com.dyw.quene.controller;

import com.alibaba.fastjson.JSON;
import com.dyw.quene.HCNetSDK;
import com.dyw.quene.service.*;
import com.dyw.quene.entity.StaffEntity;
import net.iharder.Base64;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Egci {
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(Egci.class.getName());
        //静态常量
        short port = 8000;//端口
        String name = "admin";//账户
        String pass = "hik12345";//密码
        //初始化登陆对象
        LoginService loginService = new LoginService();
        //初始化人员实体
        StaffEntity staff = new StaffEntity();
        //初始化设备状态
        StatusService statusService = new StatusService();
        //更改设备模式
        ModeService modeService = new ModeService();
        //连接数据库
        DatabaseService databaseService = new DatabaseService();
        Connection dbConn = databaseService.connection();
        Statement stmt = dbConn.createStatement();
        //获取设备ip列表
        ResultSet resultSet = stmt.executeQuery("select IP from Equipment");
        List<String> deviceIps = new ArrayList<String>();
        while (resultSet.next()) {//如果对象中有数据，就会循环打印出来
            deviceIps.add("#" + resultSet.getString("IP"));
        }
        System.out.println(deviceIps);
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
            String mess = br.readLine();
            String staffInfo = "";//结构体信息
            String operationCode = mess.substring(0, 1);
            //下发卡号人脸
            if (operationCode.equals("1")) {
                //读取数据库获取人员信息
                ResultSet rs = stmt.executeQuery("select CardNumber,Name,Photo from Staff WHERE CardNumber = " + mess.substring(2));
                while (rs.next()) {//如果对象中有数据，就会循环打印出来
                    staff.setName(rs.getString("name"));
                    staff.setCardNumber(rs.getString("cardNumber"));
                    staff.setPhoto(rs.getBytes("Photo"));
                }
                //重新组织人员信息:操作码+卡号+名称+图片
                staffInfo = "1#" + staff.getCardNumber() + "#" + staff.getName() + "#" + Base64.encodeBytes(staff.getPhoto());
                //发送消息到队列中
                for (String deviceIp : deviceIps) {
                    producerService.sendToQuene(staffInfo.concat(deviceIp));
                }
                //返回消息给客户端
                OutputStream os = socketInfo.getOutputStream();
                os.write("success\n".getBytes());
                os.flush();
                br.close();
                os.close();
                socketInfo.close();
            }
            //删除卡号和人脸
            if (operationCode.equals("2")) {
                staffInfo = "2#" + staff.getCardNumber() + "#test#none";
                //发送消息到队列中
                for (String deviceIp : deviceIps) {
                    producerService.sendToQuene(staffInfo.concat(deviceIp));
                }
                //返回消息给客户端
                OutputStream os = socketInfo.getOutputStream();
                os.write("success".getBytes());
                os.flush();
                br.close();
                os.close();
                socketInfo.close();
            }
            //获取设备状态
            if (operationCode.equals("3")) {
                List<String> deviceInfos = new ArrayList();
                for (String deviceIp : deviceIps) {
                    //判断是否在线
                    loginService.login(deviceIp.substring(1), port, name, pass);
                    if (LoginService.lUserID.longValue() > -1) {
                        deviceInfos.add("1");
                    } else {
                        deviceInfos.add("0");
                    }
                    statusService.getWorkStatus(LoginService.lUserID);
                    HCNetSDK.NET_DVR_ACS_WORK_STATUS_V50 statusV50 = statusService.getStatusV50();
                    System.out.println(Arrays.toString(statusV50.byCardReaderVerifyMode));
                }
                //返回消息给客户端
                OutputStream os = socketInfo.getOutputStream();
                os.write(JSON.toJSONString(deviceInfos).getBytes());
                os.flush();
                br.close();
                os.close();
                socketInfo.close();
            }
            //设置一体机的通行模式
            if (operationCode.equals("4")) {
                String[] info = mess.split("#");
                loginService.login(info[1], port, name, pass);
                //卡+人脸+密码
                if (info[2].equals("0")) {
                    modeService.changeMode(LoginService.lUserID, (byte) 13);
                }
                //卡+人脸
                if (info[2].equals("1")) {
                    modeService.changeMode(LoginService.lUserID, (byte) 14);
                }
            }
            //设置切换器模式:0是关闭人脸识别，1是开启人脸识别
            if (operationCode.equals("5")) {

            }
            //设置采集采集人脸方式：0是身份证+人脸，1是不刷身份证
            if (operationCode.equals("6")) {
                String[] info = mess.split("#");
                loginService.login(info[1], port, name, pass);
                //身份证+人脸
                if (info[2].equals("0")) {
                    modeService.changeMode(LoginService.lUserID, (byte) 13);
                }
                //人脸
                if (info[2].equals("1")) {
                    modeService.changeMode(LoginService.lUserID, (byte) 14);
                }
            }
        }
    }
}
