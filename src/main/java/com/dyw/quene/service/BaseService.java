package com.dyw.quene.service;

import com.dyw.quene.HCNetSDK;

public class BaseService {
    public BaseService() {
        if (!HCNetSDK.INSTANCE.NET_DVR_Init()) {
            return;
        }
    }
}
