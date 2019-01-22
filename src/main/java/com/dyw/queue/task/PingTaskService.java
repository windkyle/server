package com.dyw.queue.task;

import com.dyw.queue.controller.Egci;
import com.dyw.queue.service.NetStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public class PingTaskService extends TimerTask {
    private Logger logger = LoggerFactory.getLogger(PingTaskService.class);
    private String deviceIp;
    private Boolean previousStatus = true;

    public PingTaskService(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    @Override
    public void run() {
        try {
            NetStateService netStateService = new NetStateService();
            if (netStateService.ping(deviceIp)) {
                if (!previousStatus) {
                    logger.info(deviceIp + ":加入布防重连任务");
                    Egci.deviceIpsAlarmFail.add(deviceIp);
                }
                previousStatus = true;
            } else {
                Egci.deviceIpsAlarmFail.remove(deviceIp);
                previousStatus = false;
            }
        } catch (Exception e) {
            logger.error("获取设备网络状态失败", e);
        }
    }
}
