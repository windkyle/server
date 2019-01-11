package com.dyw.queue.handler;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.service.CallBack4AlarmService;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmHandler implements HCNetSDK.FMSGCallBack_V31 {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private CallBack4AlarmService callBack4AlarmService = new CallBack4AlarmService();

    @Override
    public boolean invoke(NativeLong lCommand,
                          HCNetSDK.NET_DVR_ALARMER pAlarmer,
                          Pointer pAlarmInfo,
                          int dwBufLen,
                          Pointer pUser) {
        logger.info(String.format("lCommand : %d", lCommand.intValue()));
        return callBack4AlarmService.alarmNotice(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
    }
}
