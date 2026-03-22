package de.juniorjacki.utils.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Connector {

    public enum DatabaseType {
        MySQL
    }

    private static final Logger logger = LogManager.getLogger(Connector.class);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static Future<?> connectionTask;

    public record dbKey(DatabaseType type,String host, int port, String dataBase, String username, String passwd) { };

    protected boolean testKey(dbKey dbKey) throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + dbKey.host+":"+ dbKey.port + "/", dbKey.username, dbKey.passwd).isValid(5000);
    }


    protected Connection getNewConnection(dbKey dbKey) throws SQLException {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + dbKey.host+":"+ dbKey.port + "/" + dbKey.dataBase, dbKey.username, dbKey.passwd);
        } catch (SQLSyntaxErrorException e) {
            DriverManager.getConnection("jdbc:mysql://" + dbKey.host+":"+ dbKey.port, dbKey.username, dbKey.passwd).createStatement().execute("CREATE DATABASE IF NOT EXISTS " + dbKey.dataBase);
            return DriverManager.getConnection("jdbc:mysql://" + dbKey.host+":"+ dbKey.port + "/" + dbKey.dataBase, dbKey.username, dbKey.passwd);
        } catch (Exception e) {
            throw e;
        }
    }

    protected void closeConnection(Connection con)  {
        try {
            con.close();
        } catch (SQLException ignored) {}
    }

    protected boolean checkConnection(Connection dbConnection) {
        try {
            if (dbConnection != null) {
                return dbConnection.isValid(5000);
            }
        } catch (SQLException ignored) {}
        return false;
    }


    protected CompletableFuture<Connection> getNewConnectionAsync(dbKey key) {
        CompletableFuture<Connection> newConnection = new CompletableFuture<>();
        connectionTask = executorService.submit(() -> {
            while (running.get()) {
                try {
                    Connection conn = getNewConnection(key);
                    if (conn != null && conn.isValid(2)) {
                        synchronized (Connector.class) {
                            newConnection.complete(conn);
                            running.set(false);
                            logger.info("Database reconnected successfully.");
                        }
                    }
                } catch (SQLException e) {
                    logger.warn("Failed to reconnect to the Database: {}", e.getMessage());
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        return newConnection;
    }

    protected static void stopAsyncConnectionGetter () {
        running.set(false);
        if (connectionTask != null) {
            connectionTask.cancel(true);
        }
        executorService.shutdown();
        logger.info("Reconnect connection attempts stopped.");
    }
}
