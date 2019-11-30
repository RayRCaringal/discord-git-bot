package connection.dao;

import connection.Repo;

import java.sql.SQLException;

public interface RepoDao{
    void createRepoTable() throws SQLException;

    void store(String path, String name) throws SQLException;

    Repo search(String field, String value);

    Repo delete(String field, String value);

    void update(Repo repo, String field, String value);

}
