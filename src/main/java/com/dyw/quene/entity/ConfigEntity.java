package com.dyw.quene.entity;

public class ConfigEntity {
    //一体机配置
    private String deviceIp;//设备ip
    private short devicePort;//设备端口
    private String deviceName;//设备账号
    private String devicePass;//设备密码
    //数据库配置
    private String dataBaseIp;//数据库ip
    private short dataBasePort;//数据库端口
    private String dataBaseName;//数据库账号
    private String dataBasePass;//数据库密码
    private String dataBaseLib;//数据库名称
    //队列配置
    private String queueIp;//队列ip
    //socket配置
    private short socketPort;//socket端口

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

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
}
