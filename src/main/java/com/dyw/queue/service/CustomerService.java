package com.dyw.queue.service;

import com.dyw.queue.HCNetSDK;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import net.iharder.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerService extends BaseService implements Runnable {
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
            Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();
            try {
                channel.queueDeclare(queueName, true, false, false, null);
            } catch (IOException e) {
                logger.error("消费者创建队列错误：", e);
            }
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String[] personInfo = new String(body).split("#");//人员信息：卡号、名称、人脸
                    String operationCode = personInfo[0];
                    String cardNo = personInfo[1];//卡号
                    String cardName = personInfo[2];//姓名
                    String picInfo = personInfo[3];//人脸信息
                    String ip = personInfo[4];//ip地址
                    logger.info("正在执行操作的IP:" + ip + ",卡号：" + cardNo);

                    //登陆
                    LoginService loginService = new LoginService();
                    loginService.login(ip, (short) 8000, "admin", "hik12345");
                    if (loginService.getlUserID().longValue() > -1) {
                        //判断卡号是否存在，存在卡号则先删除和人脸/如果命令是2，则正好只执行删除操作
                        try {
                            if (cardService.getCardInfo(cardNo, loginService.getlUserID())) {
                                logger.info("卡号已存在，先删除卡号和人脸");
                                //删除卡号
                                if (cardService.delCardInfo(cardNo, loginService.getlUserID())) {
                                    logger.info("卡号删除成功");
                                    if (faceService.delFace(cardNo, loginService.getlUserID())) {
                                        logger.info("人脸删除成功");
                                    } else {
                                        channel.basicAck(envelope.getDeliveryTag(), false);
                                    }
                                } else {
                                    channel.basicAck(envelope.getDeliveryTag(), false);
                                }
                                if (operationCode.equals("2")) {
                                    loginService.logout();
                                }
                            }
                        } catch (InterruptedException e) {
                            logger.error("删除卡号和人脸出错：" + e);
                        }
                        //判断操作码
                        if (operationCode.equals("1")) {
                            //卡号姓名下发
                            Boolean cardStatus = cardService.setCardInfo(loginService.getlUserID(), cardNo, cardName, "666666");
                            if (cardStatus) {
                                //人脸图片下发
                                Boolean faceStatus = faceService.setFaceInfo(cardNo, Base64.decode(picInfo), loginService.getlUserID());
                                if (faceStatus) {
                                    channel.basicAck(envelope.getDeliveryTag(), false);
                                } else {
                                    channel.basicReject(envelope.getDeliveryTag(), true);
                                }
                            } else {
                                channel.basicReject(envelope.getDeliveryTag(), true);
                            }
                        } else {
                            channel.basicReject(envelope.getDeliveryTag(), false);
                        }
                        loginService.logout();//释放设备资源
                    } else {
                        channel.basicReject(envelope.getDeliveryTag(), true);
                    }
                }
            };
            channel.basicConsume(queueName, false, consumer);
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
}
