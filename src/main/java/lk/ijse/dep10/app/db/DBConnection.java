package lk.ijse.dep10.app.db;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static DBConnection dbConnection;
    private final Connection connection;

    private DBConnection(){
        try {
            File configFile = new File("application.properties");
            FileReader fr = new FileReader(configFile);
            Properties properties = new Properties();
            properties.load(fr);
            fr.close();

            String host = properties.getProperty("mysql.host", "localhost");
            String port = properties.getProperty("mysql.port", "3306");
            String database = properties.getProperty("mysql.database", "dep10_jdbc2");
            String username = properties.getProperty("mysql.username", "root");
            String password = properties.getProperty("mysql.password", "Mj374&Ks319");

            String url = "jdbc:mysql://"+ host +":"+ port +"/"+ database +"?createDatabaseIfNotExist=true&allowMultiQueries=true";
            connection = DriverManager.getConnection(url, username, password);

        } catch (FileNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Configuration file doesn't exist.").showAndWait();
            throw new RuntimeException(e);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to read configurations, try again!").showAndWait();
            throw new RuntimeException(e);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to establish the database connection, try again. If the problem persist please contact the technical team.").showAndWait();
            throw new RuntimeException(e);
        }
    }

    public static DBConnection getInstance() {
        return (dbConnection == null)? dbConnection = new DBConnection(): dbConnection;
    }

    public Connection getConnection(){
        return connection;
    }
}
