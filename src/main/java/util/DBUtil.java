package util;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBUtil {
    public static final String HOST_NAME = "localhost";
    public static final String DB_NAME = "fashionecom";
    public static final String DB_PORT = "3306";
    public static final String USER_NAME = "root";
    public static final String PASSWORD = "123456";
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final int DB_MIN_CONNECTIONS = 2;
    public static final int DB_MAX_CONNECTIONS = 4;
    public static final String CONNECTION_URL = "jdbc:mysql://" + HOST_NAME + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";

    private static ComboPooledDataSource cpds = new ComboPooledDataSource();

    static {
        try {
            cpds.setDriverClass(DB_DRIVER);
            cpds.setJdbcUrl(CONNECTION_URL);
            cpds.setUser(USER_NAME);
            cpds.setPassword(PASSWORD);
            cpds.setMinPoolSize(DB_MIN_CONNECTIONS);
            cpds.setInitialPoolSize(DB_MIN_CONNECTIONS);
            cpds.setMaxPoolSize(DB_MAX_CONNECTIONS);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = cpds.getConnection();
        return conn;
    }

    public static void main(String[] args) throws SQLException {
        System.out.println(DBUtil.getConnection());
    }
}
