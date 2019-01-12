package com.dyw.queue.service;

import com.alibaba.fastjson.JSON;
import com.dyw.queue.controller.Egci;
import com.dyw.queue.entity.StaffEntity;
import com.dyw.queue.entity.StatusEntity;
import com.dyw.queue.entity.TemporaryStaffEntity;
import net.iharder.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SocketService extends Thread {
    private Logger logger = LoggerFactory.getLogger(SocketService.class);
    private Socket socketInfo;
    private ModeService modeService;
    private StatusService statusService;

    public SocketService(Socket socket) {
        //初始化设备状态
        statusService = new StatusService();
        //更改设备模式
        modeService = new ModeService();
        this.socketInfo = socket;
    }

    /*
     * 数据处理
     * */
    @Override
    public void run() {
        //初始化人员信息
        StaffEntity staff = new StaffEntity();
        //查看客户端
        logger.info("客户端:" + socketInfo.getInetAddress().getHostAddress() + "已连接到服务器");
        //读取客户端发送来的信息
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(socketInfo.getInputStream()));
        } catch (IOException e) {
            logger.error("获取客户端消息失败：", e);
        }
        try {
            String mess = br.readLine();
            logger.info("客户端发来的消息：" + mess);
            String staffInfo = "";//结构体信息
            String operationCode = mess.substring(0, 1);
            //下发卡号人脸
            if (operationCode.equals("1")) {
                //读取数据库获取人员信息
                String sql = "select CardNumber,Name,Photo from Staff WHERE CardNumber = '" + mess.substring(2) + "'";
                ResultSet rs = Egci.stmt.executeQuery(sql);
                while (rs.next()) {//如果对象中有数据，就会循环打印出来
                    staff.setName(rs.getString("Name"));
                    staff.setCardNumber(rs.getString("CardNumber"));
                    staff.setPhoto(rs.getBytes("Photo"));
                }
                //重新组织人员信息:操作码+卡号+名称+图片
                staffInfo = "1#" + staff.getCardNumber() + "#" + staff.getName() + "#" + Base64.encodeBytes(staff.getPhoto());
                //发送消息到队列中
                for (int i = 0; i < Egci.deviceIps0WithOctothorpe.size(); i++) {
                    Egci.producerServiceList.get(i).sendToQueue(staffInfo.concat(Egci.deviceIps0WithOctothorpe.get(i)));
                }
                //删除临时表中的人员信息
                TemporaryStaffEntity temporaryStaffEntity = new TemporaryStaffEntity();
                temporaryStaffEntity.setCardNumber(staff.getCardNumber());
                OnguardService onguardService = new OnguardService();
                onguardService.deleteTemporary(temporaryStaffEntity);
                //返回正确消息给客户端
                sendToClient(socketInfo, br, "success");
            }
            //删除卡号和人脸
            if (operationCode.equals("2")) {
                staffInfo = "2#" + mess.substring(2) + "#test#none";
                //发送消息到队列中
                for (int i = 0; i < Egci.deviceIps0WithOctothorpe.size(); i++) {
                    Egci.producerServiceList.get(i).sendToQueue(staffInfo.concat(Egci.deviceIps0WithOctothorpe.get(i)));
                }
                //返回消息给客户端
                sendToClient(socketInfo, br, "success");
            }
            //获取设备状态
            if (operationCode.equals("3")) {
                List<StatusEntity> deviceStatus = new ArrayList<StatusEntity>();
                if (mess.substring(2).equals("0")) {
                    deviceStatus = getStatus(Egci.deviceIps0WithOctothorpe);
                } else if (mess.substring(2).equals("1")) {
                    deviceStatus = getStatus(Egci.deviceIps1WithOctothorpe);
                } else if (mess.substring(2).equals("2")) {
                    deviceStatus = getStatus(Egci.deviceIps2WithOctothorpe);
                } else if (mess.substring(2).equals("3")) {
                    deviceStatus = getStatus(Egci.deviceIps3WithOctothorpe);
                }
                //返回消息给客户端
                sendToClient(socketInfo, br, JSON.toJSONString(deviceStatus));
                System.out.println(JSON.toJSONString(deviceStatus));
            }
            //设置一体机的通行模式
            if (operationCode.equals("4")) {
                LoginService loginService = new LoginService();
                String[] info = mess.split("#");
                loginService.login(info[1], Egci.devicePort, Egci.deviceName, Egci.devicePass);
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
                sendToClient(socketInfo, br, "error");
            }
            //设置采集采集人脸方式：0是身份证+人脸，1是不刷身份证
            if (operationCode.equals("6")) {
                LoginService loginService = new LoginService();
                String[] info = mess.split("#");
                loginService.login(info[1], Egci.devicePort, Egci.deviceName, Egci.devicePass);
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
            //实时监控消息推送
            if (operationCode.equals("8")) {
                String[] info = mess.split("#");
                if (Egci.producerServiceMap.get(socketInfo.getInetAddress().getHostAddress()) != null) {
                    Egci.producerMonitorOneServices.remove(Egci.producerServiceMap.get(socketInfo.getInetAddress().getHostAddress()));
                    Egci.producerMonitorTwoServices.remove(Egci.producerServiceMap.get(socketInfo.getInetAddress().getHostAddress()));
                    Egci.producerMonitorThreeServices.remove(Egci.producerServiceMap.get(socketInfo.getInetAddress().getHostAddress()));
                    Egci.producerServiceMap.get(socketInfo.getInetAddress().getHostAddress()).deleteQueue();
                    Thread.sleep(3000);
                }
                ProducerService producerService = new ProducerService("push:" + socketInfo.getInetAddress().getHostAddress(), Egci.queueIp);
                CustomerMonitorService customerMonitorService = new CustomerMonitorService("push:" + socketInfo.getInetAddress().getHostAddress(), Egci.queueIp, socketInfo, producerService);
                customerMonitorService.start();
                if (info[1].equals("1")) {
                    Egci.producerMonitorOneServices.add(producerService);
                }
                if (info[2].equals("1")) {
                    Egci.producerMonitorTwoServices.add(producerService);
                }
                if (info[3].equals("1")) {
                    Egci.producerMonitorThreeServices.add(producerService);
                }
                Egci.producerServiceMap.put(socketInfo.getInetAddress().getHostAddress(), producerService);
                Thread.sleep(2000);
            }
        } catch (IOException e) {
            logger.error("socket断开");
        } catch (Exception e) {
            logger.error("socket数据处理出错：", e);
            sendToClient(socketInfo, br, "error");
        }
    }

    /*
     *返回消息到客户端
     * */
    private void sendToClient(Socket socket, BufferedReader br, String message) {
        try {
            OutputStream os = socket.getOutputStream();
            os.write(message.getBytes());
            os.flush();
            br.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            logger.error("返回消息到客户端出错：" + e);
        }
    }

    /*
     * 获取设备状态
     * */
    public List<StatusEntity> getStatus(List<String> deviceIps) {
        List<StatusEntity> deviceStatus = new ArrayList<StatusEntity>();
        LoginService loginService = new LoginService();
        for (String deviceIp : deviceIps) {
            StatusEntity statusEntity = new StatusEntity();
            //判断是否在线
            loginService.login(deviceIp.substring(1), Egci.devicePort, Egci.deviceName, Egci.devicePass);
            if (loginService.getlUserID().longValue() > -1) {
                statusEntity = statusService.getWorkStatus(loginService.getlUserID());
                statusEntity.setIsLogin("1");
                statusEntity.setDeviceIp(deviceIp.substring(1));
            } else {
                statusEntity.setIsLogin("0");
                statusEntity.setDeviceIp(deviceIp.substring(1));
                statusEntity.setCardNumber("0");
                statusEntity.setPassMode("0");
            }
            deviceStatus.add(statusEntity);
        }
        return deviceStatus;
    }
}
