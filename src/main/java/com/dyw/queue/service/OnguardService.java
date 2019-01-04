package com.dyw.queue.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class OnguardService extends Thread {
    private static Logger logger = LoggerFactory.getLogger(OnguardService.class);

    @Override
    public void run() {
        try {
            Socket socket = new Socket("127.0.0.1", 9090);
            //接口服务端信息
            System.out.println("waiting...");
            while (true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String info = br.readLine();
            }
        } catch (Exception e) {
            logger.error("接收onGuard数据出错：", e);
        }
    }
}
