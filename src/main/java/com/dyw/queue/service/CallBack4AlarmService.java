package com.dyw.queue.service;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.controller.Egci;
import com.dyw.queue.entity.AlarmEntity;
import com.dyw.queue.entity.FaceCollectionEntity;
import com.dyw.queue.entity.StaffEntity;
import com.dyw.queue.tool.Tool;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;


public class CallBack4AlarmService {
    private Logger logger = LoggerFactory.getLogger(CallBack4AlarmService.class);

    public Boolean alarmNotice(NativeLong lCommand,
                               HCNetSDK.NET_DVR_ALARMER pAlarmer,
                               Pointer pAlarmInfo,
                               int dwBufLen,
                               Pointer pUser,
                               SqlSession session) {
        try {
            int alarmType = lCommand.intValue();
            switch (alarmType) {
//            case HCNetSDK.COMM_ALARM_V30:
//                logger.info("HCNetSDK.COMM_ALARM_V30");
//                entity = COMM_ALARM_V30_info(pAlarmInfo);
//                break;
                case HCNetSDK.COMM_ALARM_ACS: //门禁主机报警信息
                    logger.info("HCNetSDK.COMM_ALARM_ACS");
                    COMM_ALARM_ACS_info(pAlarmer, pAlarmInfo, session);
                    break;
                case HCNetSDK.COMM_ID_INFO_ALARM: //身份证信息
                    logger.info("HCNetSDK.COMM_ID_INFO_ALARM");
                    COMM_ID_INFO_ALARM_info(pAlarmer, pAlarmInfo, session);
                    break;
                default:
                    logger.info("go default");
                    break;
            }
        } catch (Exception e) {
            logger.error("error", e);
        }
        return true;
    }

    private void COMM_ID_INFO_ALARM_info(HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, SqlSession session) throws UnsupportedEncodingException {
        HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM strIDCardInfo = new HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM();
        strIDCardInfo.write();
        Pointer pIDCardInfo = strIDCardInfo.getPointer();
        pIDCardInfo.write(0, pAlarmInfo.getByteArray(0, strIDCardInfo.size()), 0, strIDCardInfo.size());
        strIDCardInfo.read();
        //人证比对失败，不保存和推送信息
        if (strIDCardInfo.dwMinor == 112) {
            return;
        }
        FaceCollectionEntity faceCollectionEntity = new FaceCollectionEntity();
        faceCollectionEntity.setName(new String(strIDCardInfo.struIDCardCfg.byName, "utf-8").trim());//姓名
        faceCollectionEntity.setCardId(new String(strIDCardInfo.struIDCardCfg.byIDNum).trim());//身份证号
        faceCollectionEntity.setNation(String.valueOf((strIDCardInfo.struIDCardCfg.byNation)));//民族
        faceCollectionEntity.setSex(String.valueOf((strIDCardInfo.struIDCardCfg.bySex)));//性别
        faceCollectionEntity.setBirthday(strIDCardInfo.struIDCardCfg.struBirth.wYear + "-" + strIDCardInfo.struIDCardCfg.struBirth.byMonth + "-" + strIDCardInfo.struIDCardCfg.struBirth.byDay);//出生日期
        faceCollectionEntity.setExpirationDate(strIDCardInfo.struIDCardCfg.struStartDate.wYear + "-" + strIDCardInfo.struIDCardCfg.struStartDate.byMonth + "-" + strIDCardInfo.struIDCardCfg.struStartDate.byDay + " 到 " + strIDCardInfo.struIDCardCfg.struEndDate.wYear + "-" + strIDCardInfo.struIDCardCfg.struEndDate.byMonth + "-" + strIDCardInfo.struIDCardCfg.struEndDate.byDay);//有效期
        faceCollectionEntity.setOrganization(new String(strIDCardInfo.struIDCardCfg.byIssuingAuthority, "utf-8").trim());//签发机关
        faceCollectionEntity.setSimilation(String.valueOf(Tool.getRandom(89, 76, 13)));
        ByteBuffer buffersId = strIDCardInfo.pPicData.getByteBuffer(0, strIDCardInfo.dwPicDataLen);
        byte[] bytesId = new byte[strIDCardInfo.dwPicDataLen];
        buffersId.get(bytesId);
        faceCollectionEntity.setIdentificationPhoto(bytesId);//身份证图片
        try {
            ByteBuffer buffersCp = strIDCardInfo.pCapturePicData.getByteBuffer(0, strIDCardInfo.dwCapturePicDataLen);
            byte[] bytesCp = new byte[strIDCardInfo.dwCapturePicDataLen];
            buffersCp.get(bytesCp);
            faceCollectionEntity.setStaffPhoto(bytesCp);
        } catch (Exception e) {
            faceCollectionEntity.setStaffPhoto(null);
            return;
        }
        session.insert("mapping.faceCollectionMapper.insertFaceCollection", faceCollectionEntity);
        session.commit();
        try {
            Egci.faceCollectionIpWithProducer.get(new String(pAlarmer.sDeviceIP).trim()).sendToQueue(faceCollectionEntity.getId() + "");
        } catch (Exception e) {
            logger.error("推送通信到消费者失败", e);
        }
        //判断布防是否是在线断开后自动重连了
        if (Egci.deviceIpsAlarmFail.contains(new String(pAlarmer.sDeviceIP).trim())) {
            Egci.deviceIpsAlarmFail.remove(new String(pAlarmer.sDeviceIP).trim());
        }
    }

    private void COMM_ALARM_ACS_info(HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, SqlSession session) {
        HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo = new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
        strACSInfo.write();
        Pointer pACSInfo = strACSInfo.getPointer();
        pACSInfo.write(0, pAlarmInfo.getByteArray(0, strACSInfo.size()), 0, strACSInfo.size());
        strACSInfo.read();
        if (Egci.deviceIpsFaceCollection.contains(new String(pAlarmer.sDeviceIP).trim())) {
            FaceCollectionEntity faceCollectionEntity = new FaceCollectionEntity();
            try {
                ByteBuffer buffers = strACSInfo.pPicData.getByteBuffer(0, strACSInfo.dwPicDataLen);
                byte[] bytes = new byte[strACSInfo.dwPicDataLen];
                buffers.get(bytes);
                faceCollectionEntity.setStaffPhoto(bytes);
            } catch (Exception e) {
                faceCollectionEntity.setStaffPhoto(null);
                return;
            }
            session.insert("mapping.faceCollectionMapper.insertFaceCollection", faceCollectionEntity);
            session.commit();
            try {
                Egci.faceCollectionIpWithProducer.get(new String(pAlarmer.sDeviceIP).trim()).sendToQueue(faceCollectionEntity.getId() + "");
            } catch (Exception e) {
                logger.error("推送通信到消费者失败", e);
            }
            return;
        }
        AlarmEntity alarmEntity = new AlarmEntity();
        alarmEntity.setCardNumber(new String(strACSInfo.struAcsEventInfo.byCardNo).trim());
        alarmEntity.setIP(new String(pAlarmer.sDeviceIP).trim());
        try {
            ByteBuffer buffers = strACSInfo.pPicData.getByteBuffer(0, strACSInfo.dwPicDataLen);
            byte[] bytes = new byte[strACSInfo.dwPicDataLen];
            buffers.get(bytes);
            alarmEntity.setCapturePhoto(bytes);
        } catch (Exception e) {
            alarmEntity.setCapturePhoto(null);
            return;
        }
        alarmEntity.setEventTypeId(strACSInfo.dwMinor);
        alarmEntity.setDate(Timestamp.valueOf(strACSInfo.struTime.dwYear + "-" + strACSInfo.struTime.dwMonth + "-" + strACSInfo.struTime.dwDay + " " + strACSInfo.struTime.dwHour + ":" + strACSInfo.struTime.dwMinute + ":" + strACSInfo.struTime.dwSecond));
        alarmEntity.setEquipmentName(Egci.deviceIps0Map.get(alarmEntity.getIP()));//设备名称
        //依据事件类型生成不同的事件对象
        switch (strACSInfo.dwMinor) {
            case 105:
                alarmEntity.setPass(true);
                alarmEntity.setSimilarity(Tool.getRandom(89, 76, 13));
                break;
            case 112:
                alarmEntity.setPass(false);
                alarmEntity.setSimilarity(Tool.getRandom(40, 15, 25));
                break;
            case 8:
                alarmEntity.setPass(false);
                alarmEntity.setSimilarity(0);
                break;
            default:
                alarmEntity.setPass(false);
                alarmEntity.setSimilarity(0);
                break;
        }
        //读取人员姓名
        try {
            StaffEntity staffEntity = session.selectOne("mapping.staffMapper.getStaff", alarmEntity);
            alarmEntity.setStaffName(staffEntity.getName());
        } catch (Exception e) {
            alarmEntity.setStaffName(null);
        }
        //提交数据
        session.insert("mapping.alarmMapper.insertAlarm", alarmEntity);
        //插入数据
        session.commit();
        //推送通信到消费者
        if (Egci.deviceIps1.contains(alarmEntity.getIP())) {
            for (ProducerService producerService : Egci.producerMonitorOneServices) {
                try {
                    producerService.sendToQueue(alarmEntity.getId() + "");
                } catch (Exception e) {
                    logger.error("推送通信到消费者失败", e);
                }
            }
        } else if (Egci.deviceIps2.contains(alarmEntity.getIP())) {
            for (ProducerService producerService : Egci.producerMonitorTwoServices) {
                try {
                    producerService.sendToQueue(alarmEntity.getId() + "");
                } catch (Exception e) {
                    logger.error("推送通信到消费者失败", e);
                }
            }
        } else if (Egci.deviceIps3.contains(alarmEntity.getIP())) {
            for (ProducerService producerService : Egci.producerMonitorThreeServices) {
                try {
                    producerService.sendToQueue(alarmEntity.getId() + "");
                } catch (Exception e) {
                    logger.error("推送通信到消费者失败", e);
                }
            }
        }
        //判断布防是否是在线断开后自动重连了
        if (Egci.deviceIpsAlarmFail.contains(alarmEntity.getIP())) {
            Egci.deviceIpsAlarmFail.remove(alarmEntity.getIP());
        }
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
