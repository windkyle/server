package com.dyw.queue.controller;

import com.dyw.queue.entity.StaffEntity;
import com.dyw.queue.service.SessionService;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

/**
 * 判断网络连接状况.
 */
public class Test {
    public static void main(String[] args) {
        try {
            List<String> list = new ArrayList<String>();
            list.add("one");
            list.add("two");

            try {
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("four");
            }

        } catch (Exception e) {
            System.out.println("one");
            e.printStackTrace();
        } finally {
            System.out.println("three");
        }
    }
}