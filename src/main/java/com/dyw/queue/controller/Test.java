package com.dyw.queue.controller;

import com.dyw.queue.entity.StaffEntity;
import com.dyw.queue.service.SessionService;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
//        SessionService sessionService = new SessionService();
//        SqlSession session = sessionService.createSession();
//        List<String> strings = session.selectList("mapping.staffMapper.getAllStaffCard");
//        System.out.println(strings.get(100));
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");
        list.add("three");
        try {
            System.out.println(list.get(0));
            return;
        } catch (Exception e) {
            System.out.println(list.get(1));
        } finally {
            System.out.println(list.get(2));
        }
    }
}