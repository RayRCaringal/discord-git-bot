package connection.impl;

import connection.ConnectionConfiguration;
import connection.Repo;
import connection.dao.RepoDao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class RepoImpl implements RepoDao {
    @Override
    public void createRepoTable() throws SQLException {
        Connection connection = null;
        Statement statement = null;

        try{
            connection = ConnectionConfiguration.getConnection();
            statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS repo(id INT PRIMARY KEY UNIQUE AUTO_INCREMENT," +
                    "path VARCHAR(100)," +
                    "name VARCHAR(50))");
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(statement != null){
                statement.close();
            }
            if(connection != null){
                connection.close();
            }

        }

    }

    @Override
    public void store(String path, String name) throws SQLException {
        createRepoTable();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("INSERT INTO repo (path,name)" + "VALUES (?,?)");
            preparedStatement.setString(1, path);
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();
        }catch(SQLException | IOException e){
            e.printStackTrace();
        }finally{
            if ((preparedStatement != null)){
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Repo search(String field, String value) {
        return null;
    }

    @Override
    public Repo delete(String field, String value) {
        return null;
    }

    @Override
    public void update(Repo repo, String field, String value) {

    }
}
