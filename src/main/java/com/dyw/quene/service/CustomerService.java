package com.dyw.quene.service;

import com.dyw.quene.HCNetSDK;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.logging.Logger;

import net.iharder.Base64;

public class CustomerService extends BaseService {
    private Logger logger = Logger.getLogger(CustomerService.class.getName());
    private final static String QUEUE_NAME = "dyw";
    private LoginService loginService = new LoginService();
    private CardService cardService = new CardService();
    private FaceService faceService = new FaceService();

    public void customer() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String[] personInfo = new String(body).split("#");//人员信息：卡号、名称、人脸
                String operationCode = personInfo[0];
                String cardNo = personInfo[1];//卡号
                String cardName = personInfo[2];//姓名
                String picInfo = personInfo[3];//人脸信息
                String ip = personInfo[4];//ip
                logger.info("正在执行的IP:" + ip);
                //登陆
                Boolean loginStatus = loginService.login(ip, (short) 8000, "admin", "hik12345");
                if (loginStatus) {
                    //判断卡号是否存在，存在卡号则先删除
                    try {
                        if (cardService.getCardInfo(cardNo)) {
                            logger.info("卡号已存在，先删除卡号和人脸");
                            cardService.delCardInfo(cardNo);
                            faceService.delFace(cardNo);
                        }
                    } catch (Exception e) {
                        logger.info("删除卡号失败:");
                        e.printStackTrace();
                    }
                    //判断操作码
                    if (operationCode.equals("1")) {
                        //卡号下发
                        Boolean cardStatus = cardService.setCardInfo(cardNo, cardName, "666666");
                        if (cardStatus) {
                            //图片下发
                            Boolean faceStatus = faceService.setFaceInfo(cardNo, Base64.decode(picInfo));
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

                } else {
                    channel.basicReject(envelope.getDeliveryTag(), true);
                }
            }
        };
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }
}
