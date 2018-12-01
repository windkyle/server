package com.dyw.quene.controller;

import com.alibaba.fastjson.JSON;
import com.dyw.quene.HCNetSDK;
import com.dyw.quene.service.LoginService;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        LoginService loginService = new LoginService();
        loginService.login("192.168.1.148", (short) 8000, "admin", "hik12345");
        System.out.println(LoginService.lUserID);
        //更改认证方式
        Test_EzvizWeekPlanCfg(LoginService.lUserID);
    }

    public static void Test_EzvizWeekPlanCfg(NativeLong iUserID) {
        HCNetSDK.NET_DVR_WEEK_PLAN_CFG struWeekPlan = new HCNetSDK.NET_DVR_WEEK_PLAN_CFG();
        struWeekPlan.dwSize = struWeekPlan.size();
        IntByReference pInt = new IntByReference(0);
        struWeekPlan.write();

        if (!HCNetSDK.INSTANCE.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_VERIFY_WEEK_PLAN, new NativeLong(1), struWeekPlan.getPointer(), struWeekPlan.size(), pInt)) {
            System.out.println("获取周计划信息失败:" + HCNetSDK.INSTANCE.NET_DVR_GetLastError());
        } else {
            System.out.println("获取周计划信息成功");
        }
        struWeekPlan.byEnable = 1;
        for (int i = 0; i < 7; i++) {
            struWeekPlan.struPlanCfg[i].struDaysPlanCfg[0].byEnable = 1;
            struWeekPlan.struPlanCfg[i].struDaysPlanCfg[0].byVerifyMode = 13;//14是人脸；13是卡加人脸
            struWeekPlan.struPlanCfg[i].struDaysPlanCfg[0].struTimeSegment.struBeginTime.byHour = 0; //时
            struWeekPlan.struPlanCfg[i].struDaysPlanCfg[0].struTimeSegment.struBeginTime.byMinute = 0; //分
            struWeekPlan.struPlanCfg[i].struDaysPlanCfg[0].struTimeSegment.struBeginTime.bySecond = 0; //秒
            struWeekPlan.struPlanCfg[i].struDaysPlanCfg[0].struTimeSegment.struEndTime.byHour = 23;   //时
            struWeekPlan.struPlanCfg[i].struDaysPlanCfg[0].struTimeSegment.struEndTime.byMinute = 59;   //分
            struWeekPlan.struPlanCfg[i].struDaysPlanCfg[0].struTimeSegment.struEndTime.bySecond = 59;   //秒
        }
        struWeekPlan.write();
        if (!HCNetSDK.INSTANCE.NET_DVR_SetDVRConfig(iUserID, HCNetSDK.NET_DVR_SET_VERIFY_WEEK_PLAN, new NativeLong(1), struWeekPlan.getPointer(), struWeekPlan.size())) {
            System.out.println("WEEK_PLAN_CFG2 failed with:" + HCNetSDK.INSTANCE.NET_DVR_GetLastError());
        } else {
            System.out.println("读卡器通行方式更新成功");
        }
    }
}
