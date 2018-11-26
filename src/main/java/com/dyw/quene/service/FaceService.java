package com.dyw.quene.service;

import com.dyw.quene.HCNetSDK;
import com.dyw.quene.entity.FaceInfoEntity;
import com.dyw.quene.handler.FaceSendHandler;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import java.io.FileInputStream;
import java.util.logging.Logger;

public class FaceService extends BaseService {
    private Logger logger = Logger.getLogger(FaceService.class.getName());

    private HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
    private FaceSendHandler faceSendHandler = new FaceSendHandler();

    /*
     * 人脸下发
     * */
    public Boolean setFaceInfo(String cardNo, byte[] byteFace) {
        HCNetSDK.NET_DVR_FACE_PARAM_COND lpInBuffer = new HCNetSDK.NET_DVR_FACE_PARAM_COND();
        for (int i = 0; i < cardNo.length(); i++) {
            lpInBuffer.byCardNo[i] = (byte) cardNo.charAt(i);
        }
        lpInBuffer.dwFaceNum = 1;
        lpInBuffer.byFaceID = (byte) 1;
        lpInBuffer.byEnableCardReader = new byte[HCNetSDK.MAX_CARD_READER_NUM_512];
        lpInBuffer.byEnableCardReader[0] = 1;
        lpInBuffer.dwSize = lpInBuffer.size();
        lpInBuffer.write();
        // 启动远程配置。
        NativeLong lHandle = hcNetSDK.NET_DVR_StartRemoteConfig(LoginService.lUserID, HCNetSDK.NET_DVR_SET_FACE_PARAM_CFG,
                lpInBuffer.getPointer(), lpInBuffer.size(), faceSendHandler, null);
        logger.info("人脸下发错误码：" + hcNetSDK.NET_DVR_GetLastError());
        lpInBuffer.read();
        // 发送长连接数据
        HCNetSDK.NET_DVR_FACE_PARAM_CFG pSendBuf = new HCNetSDK.NET_DVR_FACE_PARAM_CFG();
        for (int i = 0; i < cardNo.length(); i++) {
            pSendBuf.byCardNo[i] = (byte) cardNo.charAt(i);
        }
        FaceInfoEntity faceInfo = new FaceInfoEntity();
//        byte[] byteFace1 = readPic7();
        faceInfo.byFaceInfo = byteFace;
        faceInfo.write();
        pSendBuf.pFaceBuffer = faceInfo.getPointer();
        pSendBuf.dwFaceLen = byteFace.length;
        pSendBuf.byEnableCardReader = new byte[HCNetSDK.MAX_CARD_READER_NUM_512];
        pSendBuf.byEnableCardReader[0] = 1;
        pSendBuf.byFaceID = (byte) 1;
        pSendBuf.byFaceDataType = (byte) 1;
        pSendBuf.dwSize = pSendBuf.size();
        pSendBuf.write();
        boolean result = hcNetSDK.NET_DVR_SendRemoteConfig(lHandle, HCNetSDK.ENUM_ACS_INTELLIGENT_IDENTITY_DATA,
                pSendBuf.getPointer(), pSendBuf.size());
        return true;
    }

    /*
     * 删除人脸
     * */
    public String delFace(String cardNo) {

        int iErr = 0;
        //删除人脸数据
        HCNetSDK.NET_DVR_FACE_PARAM_CTRL m_struFaceDel = new HCNetSDK.NET_DVR_FACE_PARAM_CTRL();
        m_struFaceDel.dwSize = m_struFaceDel.size();
        m_struFaceDel.byMode = 0; //删除方式：0- 按卡号方式删除，1- 按读卡器删除

        m_struFaceDel.struProcessMode.setType(HCNetSDK.NET_DVR_FACE_PARAM_BYCARD.class);
        m_struFaceDel.struProcessMode.struByCard.byCardNo = cardNo.getBytes();//需要删除人脸关联的卡号
        m_struFaceDel.struProcessMode.struByCard.byEnableCardReader[0] = 1; //读卡器
        m_struFaceDel.struProcessMode.struByCard.byFaceID[0] = 1; //人脸ID
        m_struFaceDel.write();
        Pointer lpInBuffer = m_struFaceDel.getPointer();
        boolean lRemoteCtrl = HCNetSDK.INSTANCE.NET_DVR_RemoteControl(LoginService.lUserID, HCNetSDK.NET_DVR_DEL_FACE_PARAM_CFG, lpInBuffer, m_struFaceDel.size());
        if (!lRemoteCtrl) {
            iErr = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
            logger.info("NET_DVR_DEL_FACE_PARAM_CFG删除人脸图片失败，错误号：" + iErr);
        } else {
            logger.info("NET_DVR_DEL_FACE_PARAM_CFG成功!");
        }
        return "adfadsf";
    }

    /*
     * 获取本地图片
     * */
    public static byte[] readPic7() {
        try {
            FileInputStream inputStream = new FileInputStream("C:/EntranceGuard/test.jpg");
            int i = inputStream.available();
            // byte数组用于存放图片字节数据
            byte[] buff = new byte[i];
            inputStream.read(buff);
            // 关闭输入流
            inputStream.close();
            return buff;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
