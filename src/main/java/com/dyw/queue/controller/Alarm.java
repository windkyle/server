package com.dyw.queue.controller;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.service.LoginService;
import com.sun.jna.NativeLong;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Alarm {
    public static void main(String[] args) throws IOException {
        if (!HCNetSDK.INSTANCE.NET_DVR_Init()) {
            System.out.println("SDK初始化失败");
            return;
        }
        File file = new File("D:\\digicap.dav");
        FileInputStream fileInputStream = new FileInputStream("D:\\digicap.dav");
        System.out.println(file.getPath());
        System.out.println(fileInputStream.available());
        LoginService loginService = new LoginService();
        loginService.login("192.168.1.111", (short) 8000, "admin", "hik12345");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NativeLong result = HCNetSDK.INSTANCE.NET_DVR_Upgrade_V40(loginService.getlUserID(), HCNetSDK.ENUM_UPGRADE_TYPE.ENUM_UPGRADE_DVR, file.getPath(), null, fileInputStream.available());
        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!(result.longValue() < 0)) {
            System.out.println(HCNetSDK.INSTANCE.NET_DVR_GetLastError());
        }

    }
}
