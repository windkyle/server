package com.dyw.quene.service;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.logging.Logger;

import net.iharder.Base64;

public class CustomerService extends BaseService {
    private Logger logger = Logger.getLogger(CustomerService.class.getName());
    private final static String QUEUE_NAME = "dyw";
    private CardService cardService = new CardService();
    private FaceService faceService = new FaceService();

    public CustomerService(String queneIp) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queneIp);
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
                            cardService.delCardInfo(cardNo, loginService.getlUserID());
                            faceService.delFace(cardNo, loginService.getlUserID());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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

                } else {
                    channel.basicReject(envelope.getDeliveryTag(), true);
                }
            }
        };
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }
}
