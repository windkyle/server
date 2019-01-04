package com.dyw.queue.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class Test {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 9090);
            //接口服务端信息
            System.out.println("waiting...");
            while (true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String info = br.readLine();
                System.out.println("收到的消息" + info);
            }
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException el) {
                el.printStackTrace();
            }
        }
    }
}