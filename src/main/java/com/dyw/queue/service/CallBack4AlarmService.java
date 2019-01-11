package com.dyw.queue.service;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.tool.Tool;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CallBack4AlarmService {
    private Logger logger = LoggerFactory.getLogger(CallBack4AlarmService.class);

    public Boolean alarmNotice(NativeLong lCommand,
                               HCNetSDK.NET_DVR_ALARMER pAlarmer,
                               Pointer pAlarmInfo,
                               int dwBufLen,
                               Pointer pUser) {
        try {
            int alarmType = lCommand.intValue();
            switch (alarmType) {
//            case HCNetSDK.COMM_ALARM_V30:
//                logger.info("HCNetSDK.COMM_ALARM_V30");
//                entity = COMM_ALARM_V30_info(pAlarmInfo);
//                break;
                case HCNetSDK.COMM_ALARM_ACS: //门禁主机报警信息
                    logger.info("HCNetSDK.COMM_ALARM_ACS");
                    System.out.println(COMM_ALARM_ACS_info(pAlarmInfo));
                    break;
                case HCNetSDK.COMM_ID_INFO_ALARM: //身份证信息
                    logger.info("HCNetSDK.COMM_ID_INFO_ALARM");
                    COMM_ID_INFO_ALARM_info(pAlarmInfo);
                    break;
                default:
                    logger.info("go default");
                    break;
            }
        } catch (Exception e) {
            logger.error("error", e);
        }

//        //TODO 发送给ai_yuyue
//        logger.info(JSON.toJSONString(pAlarmer));
//        HashMap<String, Object> dataMap = new HashMap<>();
//        dataMap.put("entity", entity);
//        dataMap.put("ip", sIP);
//        logger.info("dataMap : " + JSON.toJSONString(dataMap));
        return true;
    }

    private String COMM_ID_INFO_ALARM_info(Pointer pAlarmInfo) {
        HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM strIDCardInfo = new HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM();
        strIDCardInfo.write();
        Pointer pIDCardInfo = strIDCardInfo.getPointer();
        pIDCardInfo.write(0, pAlarmInfo.getByteArray(0, strIDCardInfo.size()), 0, strIDCardInfo.size());
        strIDCardInfo.read();
        return "：门禁身份证刷卡信息，身份证号码：" + new String(strIDCardInfo.struIDCardCfg.byIDNum).trim() + "，姓名：" +
                new String(strIDCardInfo.struIDCardCfg.byName).trim() + "，报警主类型：" + strIDCardInfo.dwMajor + "，报警次类型：" + strIDCardInfo.dwMinor;
    }

    private String COMM_ALARM_ACS_info(Pointer pAlarmInfo) throws UnsupportedEncodingException {
        HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo = new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
        strACSInfo.write();
        Pointer pACSInfo = strACSInfo.getPointer();
        pACSInfo.write(0, pAlarmInfo.getByteArray(0, strACSInfo.size()), 0, strACSInfo.size());
        strACSInfo.read();
        ByteBuffer buffers = strACSInfo.pPicData.getByteBuffer(0, strACSInfo.dwPicDataLen);
        byte[] bytes = new byte[strACSInfo.dwPicDataLen];
        buffers.get(bytes);
        String sAlarmTypeDesc = "：门禁主机报警信息，卡号：" + new String(strACSInfo.struAcsEventInfo.byCardNo).trim() + "，卡类型：" +
                strACSInfo.struAcsEventInfo.byCardType + "，报警主类型：" + strACSInfo.dwMajor + "，报警次类型：" + strACSInfo.dwMinor + "，设备IP地址：" + new String(strACSInfo.struRemoteHostAddr.sIpV4);
//        if (strACSInfo.dwPicDataLen > 0) {
//            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
//            String newName = sf.format(new Date());
//            FileOutputStream fos;
//            try {
//                String filename = newName + "_ACS_card_" + new String(strACSInfo.struAcsEventInfo.byCardNo).trim() + ".jpg";
//                fos = new FileOutputStream(filename);
//                //将字节写入文件
//                long offset = 0;
//                ByteBuffer buffers = strACSInfo.pPicData.getByteBuffer(offset, strACSInfo.dwPicDataLen);
//                byte[] bytes = new byte[strACSInfo.dwPicDataLen];
//                buffers.rewind();
//                buffers.get(bytes);
//                fos.write(bytes);
//                fos.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                logger.error("COMM_ALARM_ACS_info", e);
//            }
//        }
        return sAlarmTypeDesc;
    }


//    private AlarmDesc COMM_ALARM_V30_info(Pointer pAlarmInfo) {
//        HCNetSDK.NET_DVR_ALARMINFO_V30 strAlarmInfoV30 = new HCNetSDK.NET_DVR_ALARMINFO_V30();
//        strAlarmInfoV30.write();
//        Pointer pInfoV30 = strAlarmInfoV30.getPointer();
//        pInfoV30.write(0, pAlarmInfo.getByteArray(0, strAlarmInfoV30.size()), 0, strAlarmInfoV30.size());
//        strAlarmInfoV30.read();
//        int dwAlarmType = strAlarmInfoV30.dwAlarmType;
//        AlarmDesc alarmDesc = new AlarmDesc();
//        alarmDesc.setTypeCode(dwAlarmType);
//        switch (dwAlarmType) {
//            case 0:
//                alarmDesc.setCodeDesc("信号报警");
//                alarmDesc.setMessage(String.format("报警入口： %d", strAlarmInfoV30.dwAlarmInputNumber + 1));
//                break;
//            case 1:
//                alarmDesc.setCodeDesc("硬盘满");
//                break;
//            case 2:
//                alarmDesc.setCodeDesc("信号丢失");
//                break;
//            case 3:
//                alarmDesc.setCodeDesc("移动侦测");
//                StringBuilder chNo = new StringBuilder();
//                for (int i = 0; i < 64; i++) {
//                    if (strAlarmInfoV30.byChannel[i] == 1) {
//                        chNo.append("ch").append(i + 1).append(" ");
//                    }
//                }
//                alarmDesc.setMessage("报警通道：" + chNo.toString());
//                break;
//            case 4:
//                alarmDesc.setCodeDesc("硬盘未格式化");
//                break;
//            case 5:
//                alarmDesc.setCodeDesc("读写硬盘出错");
//                break;
//            case 6:
//                alarmDesc.setCodeDesc("遮挡报警");
//                break;
//            case 7:
//                alarmDesc.setCodeDesc("制式不匹配");
//                break;
//            case 8:
//                alarmDesc.setCodeDesc("非法访问");
//                break;
//        }
//        return alarmDesc;
//    }
}
