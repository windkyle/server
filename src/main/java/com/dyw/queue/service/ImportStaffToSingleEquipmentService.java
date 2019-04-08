package com.dyw.queue.service;


import com.dyw.queue.controller.Egci;
import com.dyw.queue.entity.StaffEntity;

import java.util.ArrayList;
import java.util.List;

public class ImportStaffToSingleEquipmentService extends Thread {
    private String equipmentIp;//一体机ip

    public ImportStaffToSingleEquipmentService(String equipmentIp) {
        this.equipmentIp = equipmentIp;
    }

    @Override
    public void run() {
        int result = 0;
        //第一步：获取全部人员信息
        List<StaffEntity> staffEntityList;
        staffEntityList = Egci.session.selectList("mapping.staffMapper.getAllStaff");
        //登陆设备
        LoginService loginService = new LoginService();
        loginService.login(equipmentIp, (short) 8000, "admin", "hik12345");
        CardService cardService = new CardService();
        FaceService faceService = new FaceService();
        for (StaffEntity staffEntity : staffEntityList) {
            try {
                if (cardService.setCardInfo(loginService.getlUserID(), staffEntity.getCardNumber(), staffEntity.getName(), "666666", null)) {
                    faceService.setFaceInfo(staffEntity.getCardNumber(), staffEntity.getPhoto(), loginService.getlUserID());
                    result++;
                }
            } catch (Exception e) {

            }
        }
    }
}
