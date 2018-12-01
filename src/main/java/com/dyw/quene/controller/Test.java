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
        loginService.login("192.168.40.25", (short) 8000, "admin", "hik12345");
        //测试更改通行模式
        HCNetSDK.NET_DVR_WEEK_PLAN_CFG struWeekPlan = new HCNetSDK.NET_DVR_WEEK_PLAN_CFG();
        struWeekPlan.dwSize = struWeekPlan.size();
        struWeekPlan.byEnable = 1;
        HCNetSDK.NET_DVR_SINGLE_PLAN_SEGMENT struSinglePlanSegment = new HCNetSDK.NET_DVR_SINGLE_PLAN_SEGMENT();
        struWeekPlan.struPlanCfg = struSinglePlanSegment;

        struWeekPlan.struPlanCfg.byEnable = 1;
        struWeekPlan.struPlanCfg.byDoorStatus = 0;
        struWeekPlan.struPlanCfg.byVerifyMode = 8; //8-ָ��+ˢ��
        struWeekPlan.struPlanCfg.struTimeSegment.struBeginTime.byHour = 0x08; //8
        struWeekPlan.struPlanCfg.struTimeSegment.struEndTime.byHour = 0x0e;   //14
        struWeekPlan.write();

        if (!HCNetSDK.INSTANCE.NET_DVR_SetDVRConfig(LoginService.lUserID, 2101, new NativeLong(1), struWeekPlan.getPointer(), struWeekPlan.size())) {
            System.out.println("HOLIDAY_PLAN_CFG2 failed with:" + HCNetSDK.INSTANCE.NET_DVR_GetLastError());
        } else {
            System.out.println("HOLIDAY_PLAN_CFG2 succ");
        }
//            List<Map<String, String>> lists = new ArrayList<Map<String, String>>();
//            for (int i = 0; i < 3; i++) {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put("ip", "192.168.40." + i);
//                map.put("is_online", "0：表示离线/1：表示在线");
//                map.put("pass_mode", "0：表示卡+人脸/1：表示卡+人脸+密码");
//                lists.add(map);
//            }
//            System.out.println(JSON.toJSONString(lists));
//            ServerSocket ss = new ServerSocket(12345);
//            System.out.println("启动服务器....");
//            Socket s = ss.accept();
//            System.out.println("客户端:" + s.getInetAddress().getLocalHost() + "已连接到服务器");
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
//            //读取客户端发送来的消息
//            String mess = br.readLine();
//            System.out.println("客户端：" + mess);
//            OutputStream os = s.getOutputStream();
//            os.write((JSON.toJSONString(lists) + "\n").getBytes());
//            os.flush();
    }
}
