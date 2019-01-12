package com.dyw.queue.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProducerService {

    private String queueName;
    private Logger logger = LoggerFactory.getLogger(ProducerService.class);
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    public ProducerService(String queueName, String queueIp) throws Exception {
        this.queueName = queueName;
        factory = new ConnectionFactory();
        factory.setHost(queueIp);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public void sendToQueue(String body) throws Exception {
        channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, body.getBytes("GBK"));
    }

    public void deleteQueue() {
        try {
            channel.queueDelete(queueName);
            logger.info("删除队列成功");
        } catch (IOException e) {
            logger.error("删除队列失败", e);
        }
    }


}
