package com.dyw.queue.entity;

public class ConfigEntity {
    //一体机通用配置
    private short devicePort;//设备端口
    private String deviceName;//设备账号
    private String devicePass;//设备密码
    //数据库配置
    private String dataBaseIp;//数据库ip
    private short dataBasePort;//数据库端口
    private String dataBaseName;//数据库账号
    private String dataBasePass;//数据库密码
    private String dataBaseLib;//数据库名称
    private long dataBaseTime;//数据库查询间隔，避免同时大量查询
    //队列配置
    private String queueIp;//队列ip
    //socket配置
    private short socketPort;//socket端口
    //同步配置
    private String synchronization;//0：不开启，1：开启单台，2：代表全部开启
    private int synchronizationHour;//几点同步
    private int synchronizationMinute;//几分同步
    private int synchronizationSecond;//几秒同步
    private long synchronizationTime;//查找全部卡号时，线程暂停时间
    //测试单台同步
    private String testIp;

    public short getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(short devicePort) {
        this.devicePort = devicePort;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDevicePass() {
        return devicePass;
    }

    public void setDevicePass(String devicePass) {
        this.devicePass = devicePass;
    }

    public String getDataBaseIp() {
        return dataBaseIp;
    }

    public void setDataBaseIp(String dataBaseIp) {
        this.dataBaseIp = dataBaseIp;
    }

    public short getDataBasePort() {
        return dataBasePort;
    }

    public void setDataBasePort(short dataBasePort) {
        this.dataBasePort = dataBasePort;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

    public String getDataBasePass() {
        return dataBasePass;
    }

    public void setDataBasePass(String dataBasePass) {
        this.dataBasePass = dataBasePass;
    }

    public short getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(short socketPort) {
        this.socketPort = socketPort;
    }

    public String getDataBaseLib() {
        return dataBaseLib;
    }

    public void setDataBaseLib(String dataBaseLib) {
        this.dataBaseLib = dataBaseLib;
    }

    public String getQueueIp() {
        return queueIp;
    }

    public void setQueueIp(String queueIp) {
        this.queueIp = queueIp;
    }

    public String getSynchronization() {
        return synchronization;
    }

    public void setSynchronization(String synchronization) {
        this.synchronization = synchronization;
    }

    public String getTestIp() {
        return testIp;
    }

    public long getSynchronizationTime() {
        return synchronizationTime;
    }

    public void setSynchronizationTime(long synchronizationTime) {
        this.synchronizationTime = synchronizationTime;
    }

    public void setTestIp(String testIp) {
        this.testIp = testIp;
    }

    public long getDataBaseTime() {
        return dataBaseTime;
    }

    public void setDataBaseTime(long dataBaseTime) {
        this.dataBaseTime = dataBaseTime;
    }

    public int getSynchronizationHour() {
        return synchronizationHour;
    }

    public void setSynchronizationHour(int synchronizationHour) {
        this.synchronizationHour = synchronizationHour;
    }

    public int getSynchronizationMinute() {
        return synchronizationMinute;
    }

    public void setSynchronizationMinute(int synchronizationMinute) {
        this.synchronizationMinute = synchronizationMinute;
    }

    public int getSynchronizationSecond() {
        return synchronizationSecond;
    }

    public void setSynchronizationSecond(int synchronizationSecond) {
        this.synchronizationSecond = synchronizationSecond;
    }
}
