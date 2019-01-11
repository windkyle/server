package com.dyw.queue.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 12345);
            //接口服务端信息
            System.out.println("连接服务器成功，等待接收数据...");
            OutputStream os = socket.getOutputStream();
            os.write("8\n".getBytes());
            os.flush();
            while (true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String info = br.readLine();
                System.out.println("接收到的消息为" + info);
            }
        } catch (IOException e) {
            System.out.println("服务端关闭连接");
        }
    }
}