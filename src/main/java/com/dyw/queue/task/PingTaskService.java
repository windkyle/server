package com.dyw.queue.task;

import com.dyw.queue.controller.Egci;
import com.dyw.queue.service.NetStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.TimerTask;

public class PingTaskService extends TimerTask {
    private Logger logger = LoggerFactory.getLogger(PingTaskService.class);

    @Override
    public void run() {
        try {
            for (String ip : Egci.deviceIps0) {
                NetStateService netStateService = new NetStateService();
                if (netStateService.ping(ip)) {
                    System.out.println("网络正常");
                } else {
                    System.out.println("网络异常");
                }
            }
        } catch (Exception e) {
            logger.error("获取设备网络状态失败", e);
        }
    }
}
