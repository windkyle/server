import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.logging.Logger;
import net.iharder.Base64;

public class Customer {

    private Logger logger = Logger.getLogger(Customer.class.getName());
    private final static String QUEUE_NAME = "hello";


    public void customer() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        logger.info("这个");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String[] personInfo = new String(body).split("#");//人员信息：卡号、名称、人脸
                String cardNo = personInfo[0];//卡号
                String cardName = personInfo[1];//姓名
                byte[] picInfo = Base64.decode(personInfo[2]);//人脸信息
                String ip = personInfo[3];//ip
                logger.info("IP是：" + ip);

                channel.basicReject(envelope.getDeliveryTag(), true);
            }
        };
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }
}
