package com.dyw.quene.service;

import com.dyw.quene.HCNetSDK;
import com.sun.jna.NativeLong;

import java.util.logging.Logger;

public class LoginService extends BaseService {
    private Logger logger = Logger.getLogger(LoginService.class.getName());
    public static NativeLong lUserID = new NativeLong(-1);
    private static HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;

    public boolean login(String ip, short port, String name, String pass) {
        //注册之前先注销已注册的用户,预览情况下不可注销
        if (lUserID.longValue() > -1) {
            //先注销
            hcNetSDK.NET_DVR_Logout(lUserID);
            lUserID = new NativeLong(-1);
        }
        HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        lUserID = hcNetSDK.NET_DVR_Login_V30(ip, port, name, pass, m_strDeviceInfo);
        if (lUserID.longValue() < 0) {
            logger.info("登陆错误码：" + hcNetSDK.NET_DVR_GetLastError());
        }
        long userID = lUserID.longValue();
        return userID != -1;
    }

    public boolean logout() {
        // 注销和清空资源
        return hcNetSDK.NET_DVR_Logout(lUserID) && hcNetSDK.NET_DVR_Cleanup();
    }
}
