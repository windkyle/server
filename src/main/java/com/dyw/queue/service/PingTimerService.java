package com.dyw.queue.service;

import com.dyw.queue.task.PingTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

public class PingTimerService extends Thread {
    private Logger logger = LoggerFactory.getLogger(PingTimerService.class);
    private String deviceIp;

    public PingTimerService(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        PingTaskService pingTaskService = new PingTaskService(deviceIp);
        timer.schedule(pingTaskService, 6000, 10000);
        logger.info(deviceIp + ":启用自动更新网络状态");
    }
}
