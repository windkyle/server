package com.dyw.queue.service;

import com.dyw.queue.task.PingTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

public class PingTimerService {
    private static Logger logger = LoggerFactory.getLogger(PingTimerService.class);

    public static void open() {
        Timer timer = new Timer();
        PingTaskService pingTaskService = new PingTaskService();
        timer.schedule(pingTaskService, 10000, 3000);
    }

}
