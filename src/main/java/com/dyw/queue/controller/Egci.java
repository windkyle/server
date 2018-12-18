package com.dyw.queue.controller;

import com.alibaba.fastjson.JSON;
import com.dyw.queue.entity.ConfigEntity;
import com.dyw.queue.entity.StatusEntity;
import com.dyw.queue.service.*;
import com.dyw.queue.entity.StaffEntity;
import com.dyw.queue.tool.Tool;
import net.iharder.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Egci {
    //配置文件
    private static ConfigEntity configEntity;
    //一体机变量
    private short devicePort;
    private String deviceName;
    private String devicePass;
    //全局变量
    private static Logger Elogger = LoggerFactory.getLogger(Egci.class);
    private StaffEntity staff;
    private StatusService statusService;
    private ModeService modeService;
    private DatabaseService databaseService;
    private Statement stmt;
    private List<String> deviceIps;
    private List<StatusEntity> deviceStatus;
    private String queueIp;
    //初始化生产者数组
    private List<ProducerService> producerServiceList;

    /*
     * 构造函数
     * */
    public Egci() throws Exception {
        //读取配置文件
        configEntity = Tool.getConfig();
        System.out.println(configEntity.getDataBaseLib());

        //一体机参数配置
        devicePort = configEntity.getDevicePort();
        deviceName = configEntity.getDeviceName();
        devicePass = configEntity.getDevicePass();
        //初始化人员实体
        staff = new StaffEntity();
        //初始化设备状态
        statusService = new StatusService();
        //初始化设备状态信息
        deviceStatus = new ArrayList<StatusEntity>();
        //更改设备模式
        modeService = new ModeService();
        //连接数据库
        databaseService = new DatabaseService(configEntity.getDataBaseIp(), configEntity.getDataBasePort(), configEntity.getDataBaseName(), configEntity.getDataBasePass(), configEntity.getDataBaseLib());
        try {
            stmt = databaseService.connection().createStatement();
            //获取设备ip列表
            ResultSet resultSet = stmt.executeQuery("select IP from Equipment");
            deviceIps = new ArrayList<String>();
            while (resultSet.next()) {
                //如果对象中有数据，就会循环打印出来
                deviceIps.add("#" + resultSet.getString("IP"));
            }
            //deviceIps = Arrays.asList(new String[]{"#192.168.40.25"});
            Elogger.info("所有设备ip：" + String.valueOf(deviceIps));
        } catch (Exception e) {
            Elogger.error("连接数据库和获取全部设备IP失败：", e);
        }
        //初始化下发队列
        producerServiceList = new ArrayList<ProducerService>();
        queueIp = configEntity.getQueueIp();//获取队列ip
        for (int i = 0; i < deviceIps.size(); i++) {
            ProducerService producerService = new ProducerService(i + "：" + deviceIps.get(i), queueIp);
            producerServiceList.add(producerService);
            CustomerService customerService = new CustomerService(i + "：" + deviceIps.get(i), queueIp);
            customerService.start();
        }
    }

    /*
     * socket服务初始化
     * */
    public void initServer() throws Exception {
        try {
            ServerSocket serverSocket = new ServerSocket(configEntity.getSocketPort());
            serverSocket.setSoTimeout(0);
            serverSocket.setReuseAddress(true);
            Elogger.info("等待客户端连接......");
            while (true) {
                Socket socket = serverSocket.accept();
                socket.setReuseAddress(true);
                ClientServer clientServer = new ClientServer(socket);
                clientServer.operation();
            }
        } catch (IOException e) {
            Elogger.error("开启socket服务失败：", e);
        }
    }

    /*
     * 数据处理类
     * */
    class ClientServer {
        Socket socketInfo;

        public ClientServer(Socket socket) {
            this.socketInfo = socket;
        }

        /*
         * 数据处理
         * */
        public void operation() throws Exception {
            //查看客户端
            Elogger.info("客户端:" + socketInfo.getInetAddress().getHostAddress() + "已连接到服务器");
            //读取客户端发送来的信息
            BufferedReader br = new BufferedReader(new InputStreamReader(socketInfo.getInputStream()));
            String mess = br.readLine();
            Elogger.info("客户端发来的消息：" + mess);
            String staffInfo = "";//结构体信息
            String operationCode = mess.substring(0, 1);
            try {
                //下发卡号人脸
                if (operationCode.equals("1")) {
                    //读取数据库获取人员信息
                    String sql = "select CardNumber,Name,Photo from Staff WHERE CardNumber = '" + mess.substring(2) + "'";
                    ResultSet rs = stmt.executeQuery(sql);
                    //"delete from Users where UserID='"+UserID+"'"
                    while (rs.next()) {//如果对象中有数据，就会循环打印出来
                        staff.setName(rs.getString("Name"));
                        staff.setCardNumber(rs.getString("CardNumber"));
                        staff.setPhoto(rs.getBytes("Photo"));
                    }
                    //重新组织人员信息:操作码+卡号+名称+图片
                    staffInfo = "1#" + staff.getCardNumber() + "#" + staff.getName() + "#" + Base64.encodeBytes(staff.getPhoto());
                    //发送消息到队列中
                    for (int i = 0; i < deviceIps.size(); i++) {
                        producerServiceList.get(i).sendToQueue(staffInfo.concat(deviceIps.get(i)));
                    }
                    //返回正确消息给客户端
                    sendToClient(socketInfo, br, "success");
                }
                //删除卡号和人脸
                if (operationCode.equals("2")) {
                    staffInfo = "2#" + mess.substring(2) + "#test#none";
                    //发送消息到队列中
                    for (int i = 0; i < deviceIps.size(); i++) {
                        producerServiceList.get(i).sendToQueue(staffInfo.concat(deviceIps.get(i)));
                    }
                    //返回消息给客户端
                    sendToClient(socketInfo, br, "success");
                }
                //获取设备状态
                if (operationCode.equals("3")) {
                    LoginService loginService = new LoginService();
                    for (String deviceIp : deviceIps) {
                        StatusEntity statusEntity = new StatusEntity();
                        //判断是否在线
                        loginService.login(deviceIp.substring(1), devicePort, deviceName, devicePass);
                        if (loginService.getlUserID().longValue() > -1) {
                            statusEntity = statusService.getWorkStatus(loginService.getlUserID());
                            statusEntity.setIsLogin("1");
                            statusEntity.setDeviceIp(deviceIp.substring(1));
                        } else {
                            statusEntity.setIsLogin("0");
                            statusEntity.setDeviceIp(deviceIp.substring(1));
                            statusEntity.setCardNumber("-1");
                            statusEntity.setPassMode("-1");
                        }
                        deviceStatus.add(statusEntity);
                    }
                    //返回消息给客户端
                    sendToClient(socketInfo, br, JSON.toJSONString(deviceStatus));
                }
                //设置一体机的通行模式
                if (operationCode.equals("4")) {
                    LoginService loginService = new LoginService();
                    String[] info = mess.split("#");
                    loginService.login(info[1], devicePort, deviceName, devicePass);
                    //卡+人脸
                    if (info[2].equals("0")) {
                        modeService.changeMode(loginService.getlUserID(), (byte) 13);
                        //返回消息给客户端
                        sendToClient(socketInfo, br, "success");
                    }
                    //人脸
                    if (info[2].equals("1")) {
                        modeService.changeMode(loginService.getlUserID(), (byte) 14);
                        //返回消息给客户端
                        sendToClient(socketInfo, br, "success");
                    }
                }
                //设置切换器模式:0是关闭人脸识别，1是开启人脸识别
                if (operationCode.equals("5")) {

                }
                //设置采集采集人脸方式：0是身份证+人脸，1是不刷身份证
                if (operationCode.equals("6")) {
                    LoginService loginService = new LoginService();
                    String[] info = mess.split("#");
                    loginService.login(info[1], devicePort, deviceName, devicePass);
                    //身份证+人脸
                    if (info[2].equals("0")) {
                        modeService.changeMode(loginService.getlUserID(), (byte) 13);
                    }
                    //人脸
                    if (info[2].equals("1")) {
                        modeService.changeMode(loginService.getlUserID(), (byte) 14);
                    }
                    //返回消息给客户端
                    sendToClient(socketInfo, br, "success");
                }
            } catch (Exception e) {
                Elogger.error("socket数据处理出错：", e);
                sendToClient(socketInfo, br, "error");
            }
        }
    }

    /*
     *返回消息到客户端
     * */
    public void sendToClient(Socket socket, BufferedReader br, String message) {
        try {
            OutputStream os = socket.getOutputStream();
            os.write(message.getBytes());
            os.flush();
            br.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            Elogger.error("返回消息到客户端出错：" + e);
        } finally {

        }
    }

    public static void main(String[] args) {
        try {
            Egci egci = new Egci();
            egci.initServer();
        } catch (Exception e) {
            Elogger.error("错误：", e);
        } finally {
            Elogger.error("人脸通行服务程序出现严重错误,需要被关闭");
            System.out.print("人脸通行服务程序出现严重错误,需要被关闭,请按任意键关闭程序后重新打开...");
            char i = 0;
            try {
                i = (char) System.in.read();
            } catch (IOException e) {
                Elogger.error("程序异常退出：", e);
            }
        }
    }
}
