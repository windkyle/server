import com.rabbitmq.client.MessageProperties;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Egci {
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(Egci.class.getName());
        String[] deviceIps = {"#192.168.40.25", "#192.168.40.27"};

        Producer producer = new Producer();
        Customer customer = new Customer();
        customer.customer();
        ServerSocket ss = new ServerSocket(12345);
        logger.info("启动服务器....");
        while (true) {
            Socket s = ss.accept();
            logger.info("客户端:" + s.getInetAddress().getLocalHost() + "已连接到服务器");
            //读取客户端发送来的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String mess = br.readLine();
            logger.info(mess);
            //发送消息到队列中
            for (int i = 0; i < deviceIps.length; i++) {
                producer.sendToQuene(mess.concat(deviceIps[i]));
            }
//            返回消息给客户端
            OutputStream ops = s.getOutputStream();
            OutputStreamWriter opsw = new OutputStreamWriter(ops);
            BufferedWriter bw = new BufferedWriter(opsw);

            bw.write("success\r\n\r\n");
            bw.flush();
            s.close();
        }
    }
}
