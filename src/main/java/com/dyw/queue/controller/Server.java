package com.dyw.queue.controller;

import com.dyw.queue.tool.Tool;
import net.iharder.Base64;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws InterruptedException {
        Data data1 = new Data("192.168.40.1");
        data1.start();
        Data data2 = new Data("192.168.40.2");
        data2.start();
        Data data3 = new Data("192.168.40.3");
        data3.start();
        try {
            ServerSocket ss = new ServerSocket(12345);
            System.out.println("启动服务器....");
            Socket s = ss.accept();
            System.out.println("客户端:" + s.getInetAddress().getLocalHost() + "已连接到服务器");

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            while (true) {
                bw.write(data1.getI() + "\n");
                bw.flush();
                Thread.sleep(3000);
            }
        } catch (IOException e) {
            System.out.println("客户端关闭连接");
        }
    }
}