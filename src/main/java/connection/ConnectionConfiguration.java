package connection;

import configs.Config;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionConfiguration {

    public static Connection getConnection() throws IOException {
        Connection connection = null;

        Config config = new Config(new File("config.json"));

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(config.getString("url").toString(),
                    config.getString("user").toString(),
                    config.getString("password").toString());
        }catch(Exception e){
            e.printStackTrace();
        }
        return connection;
    }

}
