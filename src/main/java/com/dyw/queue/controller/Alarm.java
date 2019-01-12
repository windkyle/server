package com.dyw.queue.controller;

import com.dyw.queue.service.ProducerService;

import java.util.*;

public class Alarm {
    public static void main(String[] args) throws Exception {
        Map<String, ProducerService> maps = new HashMap<String, ProducerService>();
        maps.put("one", new ProducerService("one", "localhost"));
        maps.put("two", new ProducerService("two", "localhost"));
        System.out.println(maps.size());
        if (maps.get("three") != null) {
            System.out.println("ok");
        }else {
            System.out.println("error");
        }
        System.out.println(maps.get("one"));
    }
}
