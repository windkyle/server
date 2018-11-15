import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.logging.Logger;

public class Producer {

    private final static String QUEUE_NAME = "hello";
    private Logger logger = Logger.getLogger(Producer.class.getName());
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    public Producer() throws Exception {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public void sendToQuene(String body) throws Exception {
        channel.basicPublish("", QUEUE_NAME, null, body.getBytes("UTF-8"));

    }
}
