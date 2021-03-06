package connection.dao;

import connection.Repo;

import java.sql.SQLException;
import java.util.List;

public interface RepoDao{
    void createRepoTable() throws SQLException;

    void store(String path, String name) throws SQLException;

    Repo search(String field, String value) throws SQLException;

    List<Repo> getAll() throws SQLException;

    void delete(String key) throws SQLException;

    void update(Repo repo, String field, String value);

}
