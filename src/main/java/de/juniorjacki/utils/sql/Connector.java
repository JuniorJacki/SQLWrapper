package de.juniorjacki.utils.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

public abstract class Connector {

    public enum DatabaseType {
        MySQL
    }

    public record dbKey(DatabaseType type, String host, int port, String dataBase, String username, String passwd) {
    }

    ;

    private final dbKey dbKey;

    protected Connector(dbKey dbKey) {
        this.dbKey = dbKey;
    }

    protected boolean testKey(dbKey dbKey) throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + dbKey.host + ":" + dbKey.port + "/", dbKey.username, dbKey.passwd).isValid(5000);
    }

    protected Connection getNewConnection() throws SQLException {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + dbKey.host + ":" + dbKey.port + "/" + dbKey.dataBase, dbKey.username, dbKey.passwd);
        } catch (SQLSyntaxErrorException e) {
            DriverManager.getConnection("jdbc:mysql://" + dbKey.host + ":" + dbKey.port, dbKey.username, dbKey.passwd).createStatement().execute("CREATE DATABASE IF NOT EXISTS " + dbKey.dataBase);
            return DriverManager.getConnection("jdbc:mysql://" + dbKey.host + ":" + dbKey.port + "/" + dbKey.dataBase, dbKey.username, dbKey.passwd);
        } catch (Exception e) {
            new Thread(() -> conError(e)).start();
            throw e;
        }
    }

    protected HandledConnection getNewHandledConnection() throws SQLException {
        return new HandledConnection(getNewConnection());
    }

    abstract void conError(Exception e);

    protected boolean checkConnection(int timeout) throws SQLException {
        Connection dbConnection = getNewConnection();
        try {
            if (dbConnection != null) {
                return dbConnection.isValid(timeout);
            }
        } catch (SQLException ignored) {
        }
        return false;
    }


    public record HandledConnection(Connection connection) {
        public <T> T handleAndCloseWithResult(ExecuteQueryWithResult<T> query) throws Exception {
            try {
                return query.execute(connection);
            } finally {
                finished();
            }
        }

        public void handleAndClose(ExecuteQuery query) throws Exception {
            try {
                query.execute(connection);
            } finally {
                finished();
            }
        }

        public void finished() {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }

        public interface ExecuteQueryWithResult<T> {
            T execute(Connection connection) throws Exception;
        }

        public interface ExecuteQuery {
            void execute(Connection connection) throws Exception;
        }
    }

    public interface ConErrorHandler {
        void handle(Exception e);
    }

}
