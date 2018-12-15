package com.dyw.quene.controller;

import com.alibaba.fastjson.JSON;
import com.dyw.quene.entity.ConfigEntity;
import com.dyw.quene.entity.StatusEntity;
import com.dyw.quene.service.*;
import com.dyw.quene.entity.StaffEntity;
import com.dyw.quene.tool.Tool;
import net.iharder.Base64;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Egci {
    //读取配置文件
    private static ConfigEntity configEntity;
    //静态常量
    private static final short PORT = 8000;
    private static final String NAME = "admin";
    private static final String PASS = "hik12345";
    //全局变量
    private Logger logger;
    private StaffEntity staff;
    private StatusService statusService;
    private ModeService modeService;
    private DatabaseService databaseService;
    private Statement stmt;
    private List<String> deviceIps;
    private List<StatusEntity> deviceStatus;
    private String queneIp;
    //初始化生产者数组
    private List<ProducerService> producerServiceList;

    /*
     * 构造函数
     * */
    public Egci() throws Exception {
        //获取配置文件
        configEntity = Tool.getConfig();
        System.out.println(configEntity.getDataBaseLib());

        logger = Logger.getLogger(Egci.class.getName());
        //初始化人员实体
        staff = new StaffEntity();
        //初始化设备状态
        statusService = new StatusService();
        //初始化设备状态信息
        deviceStatus = new ArrayList<StatusEntity>();
        //更改设备模式
        modeService = new ModeService();
        //连接数据库
        databaseService = new DatabaseService();
        stmt = databaseService.connection().createStatement();
        //获取设备ip列表
        ResultSet resultSet = stmt.executeQuery("select IP from Equipment");
        deviceIps = new ArrayList<String>();
        while (resultSet.next()) {//如果对象中有数据，就会循环打印出来
            deviceIps.add("#" + resultSet.getString("IP"));
        }
//        deviceIps = Arrays.asList(new String[]{"#192.168.40.25"});
        logger.info(String.valueOf(deviceIps));
        //初始化下发队列
        producerServiceList = new ArrayList<ProducerService>();
        queneIp = "127.0.0.1";
        for (int i = 0; i < 52; i++) {
            ProducerService producerService = new ProducerService(i + "", queneIp);
            producerServiceList.add(producerService);
            CustomerService customerService = new CustomerService(i + "", queneIp);
            customerService.start();
        }
    }

    /*
     * socket服务初始化
     * */
    public void initServer() throws Exception {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            serverSocket.setSoTimeout(0);
            serverSocket.setReuseAddress(true);
            logger.info("等待客户端连接......");
            while (true) {
                Socket socket = serverSocket.accept();
                socket.setReuseAddress(true);
                ClientServer clientServer = new ClientServer(socket);
                clientServer.operation();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 数据处理类
     * */
    class ClientServer {
        Socket socketInfo;

        public ClientServer(Socket socket) throws IOException, Exception {
            this.socketInfo = socket;
        }

        /*
         * 数据处理
         * */
        public void operation() throws Exception {
            //查看客户端
            logger.info("客户端:" + socketInfo.getInetAddress().getHostAddress() + "已连接到服务器");
            //读取客户端发送来的信息
            BufferedReader br = new BufferedReader(new InputStreamReader(socketInfo.getInputStream()));
            String mess = br.readLine();
            logger.info("客户端发来的消息：" + mess);
            String staffInfo = "";//结构体信息
            String operationCode = mess.substring(0, 1);
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
                    producerServiceList.get(i).sendToQuene(staffInfo.concat(deviceIps.get(i)));
                }
                //返回消息给客户端
                OutputStream os = socketInfo.getOutputStream();
                os.write("success".getBytes());
                os.flush();
                br.close();
                os.close();
                socketInfo.close();
            }
            //删除卡号和人脸
            if (operationCode.equals("2")) {
                staffInfo = "2#" + mess.substring(2) + "#test#none";
                //发送消息到队列中
                for (int i = 0; i < deviceIps.size(); i++) {
                    producerServiceList.get(i).sendToQuene(staffInfo.concat(deviceIps.get(i)));
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
                LoginService loginService = new LoginService();
                for (String deviceIp : deviceIps) {
                    StatusEntity statusEntity = new StatusEntity();
                    //判断是否在线
                    loginService.login(deviceIp.substring(1), PORT, NAME, PASS);
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
                OutputStream os = socketInfo.getOutputStream();
                os.write(JSON.toJSONString(deviceStatus).getBytes());
                os.flush();
                br.close();
                os.close();
                socketInfo.close();
            }
            //设置一体机的通行模式
            if (operationCode.equals("4")) {
                LoginService loginService = new LoginService();
                String[] info = mess.split("#");
                loginService.login(info[1], PORT, NAME, PASS);
                //卡+人脸
                if (info[2].equals("0")) {
                    modeService.changeMode(loginService.getlUserID(), (byte) 13);
                    //返回消息给客户端
                    OutputStream os = socketInfo.getOutputStream();
                    os.write("success".getBytes());
                    os.flush();
                    br.close();
                    os.close();
                    socketInfo.close();
                }
                //人脸
                if (info[2].equals("1")) {
                    modeService.changeMode(loginService.getlUserID(), (byte) 14);
                    //返回消息给客户端
                    OutputStream os = socketInfo.getOutputStream();
                    os.write("success".getBytes());
                    os.flush();
                    br.close();
                    os.close();
                    socketInfo.close();
                }
            }
            //设置切换器模式:0是关闭人脸识别，1是开启人脸识别
            if (operationCode.equals("5")) {

            }
            //设置采集采集人脸方式：0是身份证+人脸，1是不刷身份证
            if (operationCode.equals("6")) {
                LoginService loginService = new LoginService();

                String[] info = mess.split("#");
                loginService.login(info[1], PORT, NAME, PASS);
                //身份证+人脸
                if (info[2].equals("0")) {
                    modeService.changeMode(loginService.getlUserID(), (byte) 13);
                }
                //人脸
                if (info[2].equals("1")) {
                    modeService.changeMode(loginService.getlUserID(), (byte) 14);
                }
                //返回消息给客户端
                OutputStream os = socketInfo.getOutputStream();
                os.write("success".getBytes());
                os.flush();
                br.close();
                os.close();
                socketInfo.close();
            }
        }
    }

    public static void main(String[] args) {
        try {
            Egci egci = new Egci();
            egci.initServer();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("出现异常");
        }
    }
}
