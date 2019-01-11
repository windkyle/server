package com.dyw.queue.handler;

import com.dyw.queue.HCNetSDK;
import com.dyw.queue.service.CallBack4AlarmService;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class AlarmHandler implements HCNetSDK.FMSGCallBack_V31 {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private CallBack4AlarmService callBack4AlarmService = new CallBack4AlarmService();
    private SqlSession session;

    public AlarmHandler() {
        //mybatis的配置文件
        String resource = "conf.xml";
        //使用类加载器加载mybatis的配置文件（它也加载关联的映射文件）
        InputStream is = AlarmHandler.class.getClassLoader().getResourceAsStream(resource);
        //构建sqlSession的工厂
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is);
        //使用MyBatis提供的Resources类加载mybatis的配置文件（它也加载关联的映射文件）
        //Reader reader = Resources.getResourceAsReader(resource);
        //构建sqlSession的工厂
        //SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader);
        //创建能执行映射文件中sql的sqlSession
        session = sessionFactory.openSession();
        /**
         * 映射sql的标识字符串，
         * me.gacl.mapping.userMapper是userMapper.xml文件中mapper标签的namespace属性的值，
         * getUser是select标签的id属性值，通过select标签的id属性值就可以找到要执行的SQL
         */
//        String statement = "mapping.alarmMapper";//映射sql的标识字符串
        //执行查询返回一个唯一user对象的sql
//            User user = session.selectOne(statement, 1);
    }

    @Override
    public boolean invoke(NativeLong lCommand,
                          HCNetSDK.NET_DVR_ALARMER pAlarmer,
                          Pointer pAlarmInfo,
                          int dwBufLen,
                          Pointer pUser) {
        logger.info(String.format("lCommand : %d", lCommand.intValue()));
        return callBack4AlarmService.alarmNotice(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser, session);
    }
}
