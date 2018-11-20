package com.dyw.quene.controller;

import com.dyw.quene.service.CardService;
import com.dyw.quene.service.FaceService;
import com.dyw.quene.service.LoginService;
import com.dyw.quene.service.StatusService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class Test implements Runnable {
    private int port = 8888;
    private ServerSocket socket;

    public Test(int port) {
        try {
            System.out.println("socket1处于监听状态");
            socket = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("socket2处于监听状态");
                Socket socketInfo = socket.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(socketInfo.getInputStream()));
                String meseng = br.readLine();
                System.out.println(meseng);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Test test1 = new Test(12345);
        Test test2 = new Test(34567);
        new Thread(test1).start();
        new Thread(test2).start();
    }
}
