package com.dyw.queue.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dyw.queue.entity.TemporaryStaffEntity;
import com.dyw.queue.tool.Tool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws InterruptedException {
        try {
            ServerSocket ss = new ServerSocket(9090);
            TemporaryStaffEntity temporaryStaffEntity = new TemporaryStaffEntity();
            temporaryStaffEntity.setCardNumber("666666666");
            temporaryStaffEntity.setName("林志强");
            temporaryStaffEntity.setNameEn("egci");
            temporaryStaffEntity.setType(2);
            String json = JSON.toJSONString(temporaryStaffEntity) + "\n";
            System.out.println(json);
            System.out.println("启动服务器....");
            TemporaryStaffEntity temporaryStaffEntity1 = JSON.parseObject(json, new TypeReference<TemporaryStaffEntity>() {
            });
            System.out.println(Tool.addQuote(temporaryStaffEntity1.getName()));
            Socket s = ss.accept();
            System.out.println("客户端:" + s.getInetAddress().getLocalHost() + "已连接到服务器");
//            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
//            //读取客户端发送来的消息
//            String mess = br.readLine();
//            System.out.println("客户端：" + mess);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            while (true) {
                bw.write(json);
                bw.flush();
                Thread.sleep(10000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}