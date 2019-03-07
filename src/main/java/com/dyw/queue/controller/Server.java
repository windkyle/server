package com.dyw.queue.controller;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws InterruptedException {
        try {
            ServerSocket ss = new ServerSocket(12345);
            System.out.println("启动服务器....");
            Socket s = ss.accept();
            System.out.println("客户端:" + s.getInetAddress().getLocalHost() + "已连接到服务器");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            System.out.println("接收到的消息为:" + br.readLine());
            int i = 0;
            while (true) {
                i++;
                bw.write(i + "\n");
                bw.flush();
                Thread.sleep(5000);
            }
        } catch (IOException e) {
            System.out.println("客户端关闭连接");
        }
    }
}