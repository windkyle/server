package com.dyw.queue.service;

import com.dyw.queue.controller.Egci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class NetStateService {
    public Boolean ping(String ip) throws Exception {
        int timeOut = 3000;  //超时应该在3钞以上
        boolean status = InetAddress.getByName(ip).isReachable(timeOut);
        // 当返回值是true时，说明host是可用的，false则不可。
        return status;
    }
}
