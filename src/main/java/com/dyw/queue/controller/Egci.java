package com.dyw.queue.controller;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.entity.ConfigEntity;
import com.dyw.queue.handler.AlarmHandler;
import com.dyw.queue.service.*;
import com.dyw.queue.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
    private static Logger Elogger;
    public static Statement stmt;
    public static List<String> deviceIps0;//所有设备，不带“#”
    public static List<String> deviceIps1;//一核设备，不带“#”
    public static List<String> deviceIps2;//二核设备，不带“#”
    public static List<String> deviceIps3;//三核设备，不带“#”
    public static List<String> deviceIps0WithOctothorpe;//所有设备，带“#”
    public static List<String> deviceIps1WithOctothorpe;//一核设备，带“#”
    public static List<String> deviceIps2WithOctothorpe;//二核设备，带“#”
    public static List<String> deviceIps3WithOctothorpe;//三核设备，带“#”
    public static List<String> deviceIpsOn;//在线设备
    public static List<String> deviceIpsOff;//离线设备
    public static Set<String> deviceIpsAlarmFail;//布防失败的设备
    public static Map<String, String> deviceIps0Map;//所有设备的信息，包含设备名称
    public static String queueIp;//队列的ip
    //初始化生产者数组
    public static List<ProducerService> producerServiceList;
    //初始化静态对象
    public static HCNetSDK hcNetSDK;
    //报警回调函数
    private static HCNetSDK.FMSGCallBack_V31 alarmHandler;
    //监控推送服务的生产者合集
    public static List<ProducerService> producerMonitorOneServices;//监听一核设备
    public static List<ProducerService> producerMonitorTwoServices;//监听二核设备
    public static List<ProducerService> producerMonitorThreeServices;//监听三核设备
    //推送服务的生产者对象数组，用来解决异常推送问题
    public static Map<String, ProducerService> producerServiceMap;

    /*
     * 初始化函数
     * */
    private static void initServer() {
        //初始化日志对象
        Elogger = LoggerFactory.getLogger(Egci.class);
        //初始化SDK静态对象
        hcNetSDK = HCNetSDK.INSTANCE;
        //初始化SDK
        if (!hcNetSDK.NET_DVR_Init()) {
            Elogger.error("SDK初始化失败");
            return;
        }
        //读取配置文件
        configEntity = Tool.getConfig(System.getProperty("user.dir") + "\\config\\config.xml");
        //一体机参数配置
        devicePort = configEntity.getDevicePort();
        deviceName = configEntity.getDeviceName();
        devicePass = configEntity.getDevicePass();
        //连接数据库
        DatabaseService databaseService = new DatabaseService(Egci.configEntity.getDataBaseIp(), Egci.configEntity.getDataBasePort(), Egci.configEntity.getDataBaseName(), Egci.configEntity.getDataBasePass(), Egci.configEntity.getDataBaseLib());
        try {
            Egci.stmt = databaseService.connection().createStatement();
        } catch (SQLException e) {
            Elogger.error("连接数据库失败", e);
        }
        //初始化设备信息
        EquipmentService.initEquipmentInfo();
        //获取一体机设备网络状态,并设置定时状态更新
        deviceIpsOn = new ArrayList<String>();
        deviceIpsOff = new ArrayList<String>();
        for (String ip : deviceIps0) {
            try {
                NetStateService netStateService = new NetStateService();
                if (netStateService.ping(ip)) {
                    deviceIpsOn.add(ip);
                } else {
                    deviceIpsOff.add(ip);
                }
                PingTimerService pingTimerService = new PingTimerService(ip);
                pingTimerService.start();
            } catch (Exception e) {
                Elogger.error("获取在线/离线设备出错", e);
            }
        }
        //初始化监听生产者
        producerMonitorOneServices = new ArrayList<ProducerService>();
        producerMonitorTwoServices = new ArrayList<ProducerService>();
        producerMonitorThreeServices = new ArrayList<ProducerService>();
        //设置报警回调函数
        alarmHandler = new AlarmHandler();
        if (!HCNetSDK.INSTANCE.NET_DVR_SetDVRMessageCallBack_V31(alarmHandler, null)) {
            Elogger.info("设置回调函数失败，错误码：" + hcNetSDK.NET_DVR_GetLastError());
        }
        //对所有一体机设备进行布防
        deviceIpsAlarmFail = new HashSet<String>();
        EquipmentService.initEquipmentAlarm();
        try {
            Thread.sleep(configEntity.getAlarmTime());
        } catch (InterruptedException e) {
            Elogger.error("一体机设备布防延迟失败", e);
        }
        //开启自动布防重连定时任务
        AlarmTimerService.open();
        //用来处理通行信息推送的问题
        producerServiceMap = new HashMap<String, ProducerService>();
        //初始化下发队列
        producerServiceList = new ArrayList<ProducerService>();
        queueIp = configEntity.getQueueIp();//获取队列ip
        for (int i = 0; i < deviceIps0WithOctothorpe.size(); i++) {
            ProducerService producerService = new ProducerService(i + "：" + deviceIps0WithOctothorpe.get(i), queueIp);
            producerServiceList.add(producerService);
            CustomerService customerService = new CustomerService(i + "：" + deviceIps0WithOctothorpe.get(i), queueIp);
            customerService.start();
        }
        //启动同步操作:0表示不启用；1表示单台；2表示全部
        if (!configEntity.getSynchronization().equals("0")) {
            SynchronizationTimerService.open();
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
            System.out.println("本机IP地址" + InetAddress.getLocalHost());
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
        } catch (InterruptedException e) {
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
            while (true) ;
        }
    }
}
