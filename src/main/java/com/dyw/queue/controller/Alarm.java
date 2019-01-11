package com.dyw.queue.controller;

import com.dyw.queue.service.AlarmService;
import com.dyw.queue.service.LoginService;

import java.sql.Timestamp;
import java.util.Random;

public class Alarm {
    public static void main(String[] args) {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            System.out.println(rand.nextInt(90) % (20) + 70);
        }
    }
}
