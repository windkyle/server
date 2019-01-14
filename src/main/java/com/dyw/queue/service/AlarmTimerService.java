package com.dyw.queue.service;

import com.dyw.queue.task.AlarmTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

public class AlarmTimerService {
    private static Logger logger = LoggerFactory.getLogger(AlarmTimerService.class);

    public static void open() {
        Timer timer = new Timer();
        AlarmTaskService alarmTimerService = new AlarmTaskService();
        timer.schedule(alarmTimerService, 60000, 10000);
        logger.info("启用布防自动重连功能");
    }
}
