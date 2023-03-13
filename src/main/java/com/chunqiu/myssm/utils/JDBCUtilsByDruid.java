package com.chunqiu.myssm.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCUtilsByDruid {
    private static ThreadLocal<Connection> threadLocal = new ThreadLocal<>();
    private static DataSource ds = null;

    static {
        Properties properties = new Properties();
        try {
            InputStream inputStream = JDBCUtilsByDruid.class.getClassLoader().getResourceAsStream("druid.properties");
            properties.load(inputStream);
            ds = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        Connection connection = threadLocal.get();
        try {
            if (connection == null) {
                connection = ds.getConnection();
                threadLocal.set(connection);
            }
        }catch (SQLException e) {
            throw new RuntimeException("connection get");
        }
        return  threadLocal.get();
    }

    //在数据库连接池技术中，close不是真的断掉连接
    public static  void close() throws SQLException {
        Connection connection = threadLocal.get();
        if (connection == null) return;
        if (!connection.isClosed()) {
            connection.close();
//            threadLocal.set(null);
            threadLocal.remove();
        }
    }
}
