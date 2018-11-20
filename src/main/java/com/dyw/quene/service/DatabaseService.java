package com.dyw.quene.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Logger;

public class DatabaseService {
    Logger logger = Logger.getLogger(DatabaseService.class.getName());

    //连接数据库
    String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    String dbURL = "jdbc:sqlserver://localhost:1433;DatabaseName=EntranceGuard";
    String userName = "dyw";
    String userPwd = "hik12345";
    Connection dbConn = null;

    public Connection connection() {
        try {
            Class.forName(driverName);
            dbConn = DriverManager.getConnection(dbURL, userName, userPwd);
            logger.info("连接数据库成功");
            return dbConn;
        } catch (Exception e) {
            logger.info("连接失败" + e.getMessage());
            return dbConn;
        }
    }
}
