package com.dyw.quene.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.logging.Logger;

public class ProducerService {

    private String queueName;
    private Logger logger = Logger.getLogger(ProducerService.class.getName());
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    public ProducerService(String queueName, String queneIp) throws Exception {
        this.queueName = queueName;
        factory = new ConnectionFactory();
        factory.setHost(queneIp);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public void sendToQuene(String body) throws Exception {
        channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, body.getBytes("UTF-8"));
    }
}
