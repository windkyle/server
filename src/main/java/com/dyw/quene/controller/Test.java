package com.dyw.quene.controller;

import com.alibaba.fastjson.JSON;
import com.dyw.quene.HCNetSDK;
import com.dyw.quene.entity.StaffEntity;
import com.dyw.quene.entity.StatusEntity;
import com.dyw.quene.service.ModeService;
import com.dyw.quene.service.LoginService;
import com.dyw.quene.service.StatusService;
import com.sun.jna.NativeLong;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        LoginService loginService = new LoginService();
        loginService.login("192.168.1.148", (short) 8000, "admin", "hik12345");
        NativeLong jindu = HCNetSDK.INSTANCE.NET_DVR_Upgrade(LoginService.lUserID, "D:\\digicap.dav");
        System.out.println(HCNetSDK.INSTANCE.NET_DVR_GetLastError());
        while (true) {
            System.out.println(HCNetSDK.INSTANCE.NET_DVR_GetUpgradeProgress(jindu));
            Thread.sleep(3000);
        }

    }
}
