package com.dyw.queue.controller;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.entity.ConfigEntity;
import com.dyw.queue.service.*;
import com.dyw.queue.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Egci {
    //mybatis框架
//    private SqlSession session;
    //配置文件
    public static ConfigEntity configEntity;
    //一体机变量
    public static short devicePort;
    public static String deviceName;
    public static String devicePass;
    //全局变量
    private static Logger Elogger = LoggerFactory.getLogger(Egci.class);
    public static Statement stmt;
    public static List<String> deviceIps;//所有设备，不带“#”，给同步功能用的
    public static List<String> deviceIps0;//所有设备
    public static List<String> deviceIps1;//一核设备
    public static List<String> deviceIps2;//二核设备
    public static List<String> deviceIps3;//三核设备
    public static String queueIp;//队列的ip
    //初始化生产者数组
    public static List<ProducerService> producerServiceList;
    //初始化静态对象
    public static HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
    //监控推送服务的生产者合集
    public static List<ProducerService> producerMonitorServices;

    /*
     * 初始化函数
     * */
    private static void initServer() throws Exception {
        if (!HCNetSDK.INSTANCE.NET_DVR_Init()) {
            System.out.println("SDK初始化失败");
            return;
        }
        //读取配置文件
        configEntity = Tool.getConfig(System.getProperty("user.dir") + "\\config\\config.xml");
        //一体机参数配置
        devicePort = configEntity.getDevicePort();
        deviceName = configEntity.getDeviceName();
        devicePass = configEntity.getDevicePass();
        //初始化设备信息
        EquipmentService.initEquipmentInfo();
        //初始化下发队列
        producerServiceList = new ArrayList<ProducerService>();
        queueIp = configEntity.getQueueIp();//获取队列ip
        producerMonitorServices = new ArrayList<ProducerService>();
        for (int i = 0; i < deviceIps0.size(); i++) {
            ProducerService producerService = new ProducerService(i + "：" + deviceIps0.get(i), queueIp);
            producerServiceList.add(producerService);
            CustomerService customerService = new CustomerService(i + "：" + deviceIps0.get(i), queueIp);
            customerService.start();
        }
        //启动同步操作:0表示不启用；1表示单台；2表示全部
        if (!configEntity.getSynchronization().equals("0")) {
            TimerService.open();
            Elogger.info("开启自动同步功能");
        } else {
            Elogger.info("关闭自动同步功能");
        }
        //获取系统默认编码
        Elogger.info("系统默认编码：" + System.getProperty("file.encoding")); //查询结果GBK
        //系统默认字符编码
        Elogger.info("系统默认字符编码：" + Charset.defaultCharset()); //查询结果GBK
        //操作系统用户使用的语言
        Elogger.info("系统默认语言：" + System.getProperty("user.language")); //查询结果zh
        //启用onGuard数据接收服务
        OnguardService onguardService = new OnguardService();
        onguardService.start();
        //启用socket服务
        try {
            ServerSocket serverSocket = new ServerSocket(configEntity.getSocketPort());
            serverSocket.setSoTimeout(0);
            serverSocket.setReuseAddress(true);
            Elogger.info("等待客户端连接......");
            while (true) {
                Socket socket = serverSocket.accept();
                socket.setReuseAddress(true);
                SocketService socketService = new SocketService(socket);
                socketService.start();
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            Elogger.error("开启socket服务失败：", e);
        }
    }

    public static void main(String[] args) {
        try {
            Egci.initServer();
        } catch (Exception e) {
            Elogger.error("错误：", e);
        } finally {
            Elogger.error("人脸通行服务程序出现严重错误,需要被关闭");
        }
    }
}
