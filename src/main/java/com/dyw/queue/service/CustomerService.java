package com.dyw.queue.service;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.controller.Egci;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import net.iharder.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerService implements Runnable {
    private Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private String queueName;
    private String queueIp;
    private CardService cardService = new CardService();
    private FaceService faceService = new FaceService();
    private Thread t;

    public CustomerService(String queueName, String queueIp) {
        this.queueName = queueName;
        this.queueIp = queueIp;
    }

    @Override
    public void run() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(queueIp);
            factory.setAutomaticRecoveryEnabled(true);//断线重连
            Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();
            try {
                channel.queueDeclare(queueName, true, false, false, null);
            } catch (IOException e) {
                logger.error("消费者创建队列错误：", e);
            }
            channel.basicQos(0, 1, true);//每次从队列中获取指定的条数为：1

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String[] personInfo = new String(body).split("#");//人员信息：卡号、名称、人脸
                    String operationCode = personInfo[0];
                    String cardNo = personInfo[1];//卡号
                    String cardName = personInfo[2];//姓名
                    String picInfo = personInfo[3];//人脸信息
                    String ip = personInfo[4];//ip地址
                    logger.info("正在执行操作的IP:" + ip + ",卡号：" + cardNo);
                    if (!Egci.deviceIpsOn.contains(ip)) {
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException e) {
                            logger.error("下发延迟失败", e);
                        }
                        logger.info("设备:" + ip + "不在线");
                        channel.basicReject(envelope.getDeliveryTag(), true);
                        return;
                    }
                    //登陆
                    LoginService loginService = new LoginService();
                    loginService.login(ip, Egci.configEntity.getDevicePort(), Egci.configEntity.getDeviceName(), Egci.configEntity.getDevicePass());
                    try {
                        if (loginService.getlUserID().longValue() > -1) {
                            //判断卡号是否存在，存在卡号则先删除和人脸;如果命令是2，则正好只执行删除操作
                            if (cardService.getCardInfo(cardNo, loginService.getlUserID())) {
                                logger.info(ip + ":卡号已存在，先删除卡号和人脸");
                                //删除卡号
                                if (cardService.delCardInfo(cardNo, loginService.getlUserID())) {
                                    logger.info(ip + ":卡号删除成功");
                                    //删除人脸，删除失败不需要操作
                                    if (faceService.delFace(cardNo, loginService.getlUserID())) {
                                        logger.info(ip + ":人脸删除成功");
                                    }
                                } else {
                                    channel.basicReject(envelope.getDeliveryTag(), true);
                                }
                            }
                            if (operationCode.equals("2")) {
                                channel.basicReject(envelope.getDeliveryTag(), false);
                            }
                            //判断操作码
                            if (operationCode.equals("1")) {
                                //卡号姓名下发
                                if (cardService.setCardInfo(loginService.getlUserID(), cardNo, cardName, "666666")) {
                                    //人脸图片下发
                                    if (faceService.setFaceInfo(cardNo, Base64.decode(picInfo), loginService.getlUserID())) {
                                        channel.basicReject(envelope.getDeliveryTag(), false);
                                    } else {
                                        channel.basicReject(envelope.getDeliveryTag(), true);
                                    }
                                } else {
                                    channel.basicReject(envelope.getDeliveryTag(), true);
                                }
                            }
                        } else {
                            channel.basicReject(envelope.getDeliveryTag(), true);
                        }
                    } catch (InterruptedException e) {
                        logger.error(ip + ":删除卡号和人脸出错：" + e);
                    } catch (IOException e) {
                        logger.error(ip + ":删除卡号和人脸出错：" + e);
                    } finally {
                        //不管有没有执行成功都执行资源释放操作
                        logger.info("执行资源释放操作");
                        loginService.logout();
                    }
                }
            };
            channel.basicConsume(queueName, false, consumer);
            try {
                logger.info("消费者数量" + queueName + ":" + channel.consumerCount(queueName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            logger.error("消费者错误位置1：", e);
        } catch (TimeoutException e) {
            logger.error("消费者错误位置2；", e);
        }
    }

    public void start() {
        logger.info("Starting: " + queueName);
        if (t == null) {
            t = new Thread(this, queueName);
            t.start();
        }
    }
    //获取消费者的数量

}
