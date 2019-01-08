package com.dyw.queue.service;

import com.dyw.queue.HCNetSDK;
import com.sun.jna.NativeLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginService extends BaseService {
    private Logger logger = LoggerFactory.getLogger(LoginService.class);
    private NativeLong lUserID = new NativeLong(-1);
    private static HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;

    public NativeLong login(String ip, short port, String name, String pass) {
        //注册之前先注销已注册的用户,预览情况下不可注销
        if (lUserID.longValue() > -1) {
            //先注销
            hcNetSDK.NET_DVR_Logout(lUserID);
            lUserID = new NativeLong(-1);
        }
        HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        lUserID = hcNetSDK.NET_DVR_Login_V30(ip, port, name, pass, m_strDeviceInfo);
        if (lUserID.longValue() < 0) {
            logger.info("设备登陆错误码：" + hcNetSDK.NET_DVR_GetLastError());
        } else {
            logger.info("设备登陆成功");

        }
        return lUserID;
    }

    public NativeLong getlUserID() {
        return lUserID;
    }

    public boolean logout() {
        // 注销和清空资源
        if (hcNetSDK.NET_DVR_Logout(lUserID) && hcNetSDK.NET_DVR_Cleanup()) {
            logger.info("设备资源释放成功");
            return true;
        } else {
            logger.info("设备资源释放失败：" + hcNetSDK.NET_DVR_GetLastError());
            return false;
        }
    }
}
