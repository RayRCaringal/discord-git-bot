package connection.impl;

import connection.ConnectionConfiguration;
import connection.Repo;
import connection.dao.RepoDao;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepoImpl implements RepoDao {
    /**
     * Creates Repository Table if it doesn't exist
     * @throws SQLException
     */
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

    /***
     *
     * @param path repository path formatted as <username>/<path>
     * @param name user keyword up to 50 characthers
     * @throws SQLException
     */
    @Override
    public void store(String path, String name) throws SQLException {
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

    /***
     * Searches if value is in the specified field of the database
     * @param field either name or path
     * @param value the name or path being searched
     * @throws SQLException
     * @return Repo of specified value
     */
    @Override
    public Repo search(String field, String value) throws SQLException {
        Repo repo = new Repo();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try{
            connection = ConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM repo WHERE " + field + "= ?");
            preparedStatement.setString(1, value);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next() == false){
                repo.setPath("empty");
                repo.setName("empty");
            }else{
                repo.setId(resultSet.getInt("id"));
                repo.setPath(resultSet.getString("path"));
                repo.setName(resultSet.getString("name"));
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(resultSet != null){
                resultSet.close();;
            }
            if(preparedStatement != null){
                preparedStatement.close();
            }
            if(connection != null){
                connection.close();
            }

        }
        return  repo;
    }

    /**
     * Lists all Repos.
     * @throws SQLException
     * @return List of Repos, if there are none returns an empty list.
     */
    @Override
    public List<Repo> getAll() throws SQLException {
        List<Repo> result = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try{
            result = new ArrayList<>();
            connection = ConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM repo");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Repo repo = new Repo();
                repo.setId(resultSet.getInt("id"));
                repo.setPath(resultSet.getString("path"));
                repo.setName(resultSet.getString("name"));
                result.add(repo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(resultSet != null){
                resultSet.close();;
            }
            if(preparedStatement != null){
                preparedStatement.close();
            }
            if(connection != null){
                connection.close();
            }
        }
        return  result;
    }

    /**
     * Deletes repository from database based on the keyword, does not check if keyword is available
     * @param key keyword to be deleted
     * @throws SQLException
     */
    @Override
    public void delete(String key) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try{
            connection = ConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("DELETE FROM repo WHERE name = ?");
            preparedStatement.setString(1, key);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(preparedStatement != null){
                preparedStatement.close();
            }
            if(connection != null){
                connection.close();
            }
        }
    }

    @Override
    public void update(Repo repo, String field, String value) {

    }
}
