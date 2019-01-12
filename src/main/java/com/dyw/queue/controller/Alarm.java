package com.dyw.queue.controller;

import com.dyw.queue.entity.AlarmEntity;
import com.dyw.queue.service.AlarmService;
import com.dyw.queue.service.LoginService;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Alarm {
    public static void main(String[] args) {
        Set<AlarmEntity> alarmEntities = new HashSet<AlarmEntity>();
        for (int i = 0; i < 10; i++) {
            AlarmEntity alarmEntity = new AlarmEntity();
            alarmEntities.add(alarmEntity);
        }
        System.out.println(alarmEntities.size());
    }
}
