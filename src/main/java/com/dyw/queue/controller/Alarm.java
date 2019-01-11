package com.dyw.queue.controller;

import com.dyw.queue.service.AlarmService;
import com.dyw.queue.service.LoginService;

public class Alarm {
    public static void main(String[] args) {
        LoginService loginService = new LoginService();
        loginService.login("192.168.40.25", (short) 8000, "admin", "hik12345");
        AlarmService alarmService = new AlarmService();
        alarmService.setupAlarmChan(loginService.getlUserID());
    }
}
