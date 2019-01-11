package com.dyw.queue.service;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.handler.AlarmHandler;
import com.sun.jna.NativeLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AlarmService extends Thread {
    //布防标识符
    private NativeLong lAlarmHandleFlag = new NativeLong(-1);
    private Logger logger = LoggerFactory.getLogger(AlarmService.class);
    private HCNetSDK.FMSGCallBack_V31 alarmHandler = new AlarmHandler();

    private NativeLong lUserID;

    public AlarmService(NativeLong lUserID) {
        this.lUserID = lUserID;
    }

    @Override
    public void run() {
        setupAlarmChan(lUserID);
    }

    /**
     * 布防
     *
     * @param lUserID 海康注册成功后返回的userId
     * @return
     */
    public void setupAlarmChan(NativeLong lUserID) {
        try {
            if (lUserID.intValue() == -1) {
                logger.info("请先注册！");
            }
            if (lAlarmHandleFlag.intValue() >= 0) {
                logger.info("已经布防过了！");
            }

            if (!HCNetSDK.INSTANCE.NET_DVR_SetDVRMessageCallBack_V31(alarmHandler, null)) {
                logger.info("设置回调函数失败！");
            }
            HCNetSDK.NET_DVR_SETUPALARM_PARAM strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
            strAlarmInfo.dwSize = strAlarmInfo.size();
            strAlarmInfo.byLevel = 1;
            strAlarmInfo.byAlarmInfoType = 1;
            strAlarmInfo.byDeployType = 0;
            strAlarmInfo.write();
            lAlarmHandleFlag = HCNetSDK.INSTANCE.NET_DVR_SetupAlarmChan_V41(lUserID, strAlarmInfo);
            if (lAlarmHandleFlag.intValue() == -1) {
                logger.info("布防失败！");
            } else {
                logger.info("布防成功!");
            }
        } catch (Exception e) {
            logger.error("error", e);
        }
//        while (true) ;//保持监听状态
    }

    public void closeAlarmChan() {
        if (lAlarmHandleFlag.intValue() > -1) {
            if (!HCNetSDK.INSTANCE.NET_DVR_CloseAlarmChan_V30(lAlarmHandleFlag)) {
                logger.info("撤防失败!");
            } else {
                lAlarmHandleFlag = new NativeLong(-1);
            }
        }
        logger.info("撤防成功！");
    }
}
