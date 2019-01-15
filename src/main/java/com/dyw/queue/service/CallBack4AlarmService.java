package com.dyw.queue.service;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.controller.Egci;
import com.dyw.queue.entity.AlarmEntity;
import com.dyw.queue.entity.StaffEntity;
import com.dyw.queue.tool.Tool;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.List;


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
                    System.out.println(COMM_ALARM_ACS_info(pAlarmer, pAlarmInfo, session));
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
        return true;
    }

    private String COMM_ID_INFO_ALARM_info(Pointer pAlarmInfo) {
        HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM strIDCardInfo = new HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM();
        strIDCardInfo.write();
        Pointer pIDCardInfo = strIDCardInfo.getPointer();
        pIDCardInfo.write(0, pAlarmInfo.getByteArray(0, strIDCardInfo.size()), 0, strIDCardInfo.size());
        strIDCardInfo.read();
        return "：门禁身份证刷卡信息，身份证号码：" + new String(strIDCardInfo.struIDCardCfg.byIDNum).trim() + "，姓名：" +
                new String(strIDCardInfo.struIDCardCfg.byName).trim() + "，报警主类型：" + strIDCardInfo.dwMajor +
                "，报警次类型：" + strIDCardInfo.dwMinor;
    }

    private String COMM_ALARM_ACS_info(HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, SqlSession session) throws UnsupportedEncodingException {
        HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo = new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
        strACSInfo.write();
        Pointer pACSInfo = strACSInfo.getPointer();
        pACSInfo.write(0, pAlarmInfo.getByteArray(0, strACSInfo.size()), 0, strACSInfo.size());
        strACSInfo.read();
        AlarmEntity alarmEntity = new AlarmEntity();
        alarmEntity.setCardNumber(new String(strACSInfo.struAcsEventInfo.byCardNo).trim());
        alarmEntity.setIP(new String(pAlarmer.sDeviceIP).trim());
        ByteBuffer buffers = strACSInfo.pPicData.getByteBuffer(0, strACSInfo.dwPicDataLen);
        byte[] bytes = new byte[strACSInfo.dwPicDataLen];
        buffers.get(bytes);
        alarmEntity.setCapturePhoto(bytes);
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
        }
        //读取人员姓名
        StaffEntity staffEntity = session.selectOne("mapping.staffMapper.getStaff", alarmEntity);
        alarmEntity.setStaffName(staffEntity.getName());
        //提交数据
        session.insert("mapping.alarmMapper.insertAlarm", alarmEntity);
        //插入数据
        session.commit();
        //推送通信到消费者
        if (Egci.deviceIps1.contains(alarmEntity.getIP())) {
            logger.info("设备属于一核");
            for (ProducerService producerService : Egci.producerMonitorOneServices) {
                try {
                    producerService.sendToQueue(alarmEntity.getId() + "");
                } catch (Exception e) {
                    logger.error("推送通信到消费者失败", e);
                }
            }
        } else if (Egci.deviceIps2.contains(alarmEntity.getIP())) {
            logger.info("设备属于二核");
            for (ProducerService producerService : Egci.producerMonitorTwoServices) {
                try {
                    producerService.sendToQueue(alarmEntity.getId() + "");
                } catch (Exception e) {
                    logger.error("推送通信到消费者失败", e);
                }
            }
        } else if (Egci.deviceIps3.contains(alarmEntity.getIP())) {
            logger.info("设备属于三核");
            for (ProducerService producerService : Egci.producerMonitorThreeServices) {
                try {
                    producerService.sendToQueue(alarmEntity.getId() + "");
                } catch (Exception e) {
                    logger.error("推送通信到消费者失败", e);
                }
            }
        }
        return "相似度值：" + alarmEntity.getSimilarity() + ";事件类型：" + alarmEntity.getEventTypeId();
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
