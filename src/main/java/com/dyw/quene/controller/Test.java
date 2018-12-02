package com.dyw.quene.controller;

import com.dyw.quene.service.ModeService;
import com.dyw.quene.service.LoginService;
import com.dyw.quene.service.StatusService;

public class Test {
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        LoginService loginService = new LoginService();
        loginService.login("192.168.1.149", (short) 8000, "admin", "hik12345");
        System.out.println(LoginService.lUserID);
        StatusService statusService = new StatusService();
        statusService.getWorkStatus(LoginService.lUserID);

        ModeService modeService = new ModeService();
        modeService.changeMode(LoginService.lUserID, (byte) 14);
    }
}
