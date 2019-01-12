package com.dyw.queue.service;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

public class CustomerMonitorService implements Runnable {
    private Logger logger = LoggerFactory.getLogger(CustomerMonitorService.class);
    private String queueName;
    private String queueIp;
    private Thread t;
    private Socket socket;
    private ProducerService producerService;

    public CustomerMonitorService(String queueName, String queueIp, Socket socket, ProducerService producerService) {
        this.queueName = queueName;
        this.queueIp = queueIp;
        this.socket = socket;
        this.producerService = producerService;
    }

    @Override
    public void run() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(queueIp);
            Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();
            try {
                channel.queueDeclare(queueName, false, false, true, null);//客户端断开后自动删除该队列
            } catch (IOException e) {
                logger.error("消费者创建队列错误：", e);
            }
            channel.basicQos(1);//每次从队列中获取指定的条数为：1
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write((new String(body) + "\n").getBytes());
                        os.flush();
                        channel.basicReject(envelope.getDeliveryTag(), false);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            logger.error("延迟出现错误", e);
                        }
                    } catch (SocketException e) {
                        //这里出现错误说明客户端已经断开
                        channel.basicReject(envelope.getDeliveryTag(), false);
//                        channel.queueDelete(queueName);
                        logger.error("客户端：" + socket.getInetAddress().getHostAddress() + " 已断开");
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
