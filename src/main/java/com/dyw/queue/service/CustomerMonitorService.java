package com.dyw.queue.service;

import com.dyw.queue.controller.Egci;
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
    private Channel channel;
    private Thread t;
    private Socket socket;

    public CustomerMonitorService(String queueName, Channel channel, Socket socket) {
        this.queueName = queueName;
        this.channel = channel;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write((new String(body) + "\n").getBytes());
                        os.flush();
                        channel.basicReject(envelope.getDeliveryTag(), false);
                        try {
                            //服务端推送消息到客户端的延迟时间，防止客户端数据接收出错
                            Thread.sleep(Egci.configEntity.getPushTime());
                        } catch (InterruptedException e) {
                            logger.error("延迟出现错误", e);
                        }
                    } catch (SocketException e) {
                        //这里出现错误说明客户端已经断开
                        channel.basicReject(envelope.getDeliveryTag(), false);
                        logger.error("客户端：" + socket.getInetAddress().getHostAddress() + " 已断开");
                    }
                }
            };
            channel.basicConsume(queueName, false, consumer);
        } catch (IOException e) {
            logger.error("消费者错误位置1：", e);
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
