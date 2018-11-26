package com.dyw.quene.controller;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        try {
            List<Map<String, String>> lists = new ArrayList<Map<String, String>>();
            for (int i = 0; i < 3; i++) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("ip", "192.168.40." + i);
                map.put("is_online", "0：表示离线/1：表示在线");
                map.put("pass_mode", "0：表示卡+人脸/1：表示卡+人脸+密码");
                lists.add(map);
            }
            System.out.println(JSON.toJSONString(lists));
            ServerSocket ss = new ServerSocket(12345);
            System.out.println("启动服务器....");
            Socket s = ss.accept();
            System.out.println("客户端:" + s.getInetAddress().getLocalHost() + "已连接到服务器");

            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            //读取客户端发送来的消息
            String mess = br.readLine();
            System.out.println("客户端：" + mess);
            OutputStream os = s.getOutputStream();
            os.write((JSON.toJSONString(lists) + "\n").getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
