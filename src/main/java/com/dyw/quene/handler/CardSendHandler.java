package com.dyw.quene.handler;

import com.dyw.quene.HCNetSDK;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class CardSendHandler implements HCNetSDK.FRemoteConfigCallback {
    private NativeLong connFlag = new NativeLong(-1);

    public NativeLong getConnFlag() {
        return connFlag;
    }

    public void setConnFlag(NativeLong connFlag) {
        this.connFlag = connFlag;
    }

    @Override
    public void invoke(int dwType, Pointer lpBuffer, int dwBufLen, Pointer pUserData) {
//        AlarmJavaDemoApp.noticeCardSet(dwType, lpBuffer, dwBufLen, pUserData, this.connFlag);
    }
}