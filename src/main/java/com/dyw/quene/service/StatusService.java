package com.dyw.quene.service;

import com.dyw.quene.HCNetSDK;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.logging.Logger;

public class StatusService extends BaseService {
    private Logger logger = Logger.getLogger(StatusService.class.getName());
    private HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
    private HCNetSDK.NET_DVR_ACS_WORK_STATUS_V50 statusV50;


    /*
     * 获取设备状态
     * */
    public void getWorkStatus(NativeLong iUserID) {
        HCNetSDK.NET_DVR_ACS_WORK_STATUS_V50 struAcsWorkStatusCfg = new HCNetSDK.NET_DVR_ACS_WORK_STATUS_V50();
        struAcsWorkStatusCfg.dwSize = struAcsWorkStatusCfg.size();
        IntByReference pInt = new IntByReference(struAcsWorkStatusCfg.size());
        NativeLong iChannel = new NativeLong(0xFFFFFFFF);
        struAcsWorkStatusCfg.write();
        if (!HCNetSDK.INSTANCE.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_ACS_WORK_STATUS_V50, iChannel, struAcsWorkStatusCfg.getPointer(), struAcsWorkStatusCfg.size(), pInt)) {
            logger.info("NET_DVR_GET_ACS_WORK_STATUS_V50 failed with:" + HCNetSDK.INSTANCE.NET_DVR_GetLastError() + HCNetSDK.INSTANCE.NET_DVR_GetErrorMsg(struAcsWorkStatusCfg.getPointer()));
        } else {
            struAcsWorkStatusCfg.read();
            statusV50 = struAcsWorkStatusCfg;
            logger.info("卡数量：" + struAcsWorkStatusCfg.dwCardNum + "通行模式：" + struAcsWorkStatusCfg.byCardReaderVerifyMode[0]);
        }
    }

    /*
     * 设置设备状态
     * */
    public void setWorkStatus(NativeLong iUserID) {

    }

    public HCNetSDK.NET_DVR_ACS_WORK_STATUS_V50 getStatusV50() {
        return statusV50;
    }
}
