package de.juniorjacki.utils.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Connector {

    public enum DatabaseType {
        MySQL
    }

    public record DatabaseKey(DatabaseType type, String host, int port, String dataBase, String username, String passwd) {
    };

    private static class StoredConnection {

        private static final long minimumExistenceTime = 60000; // Minimum existence time is one min
        private static final long activeHandleTimeout = 600000; // Active Handles (StoredCon) get Killed after 1 + 10 min
        private static final long inactiveHandleTimeout = 300000; // InActive StoredCon get Killed after 1 + 5 min

        private final Connection connection;
        private final Connector dbConnector;
        private final long initTimestamp;
        private HandledConnection inUseHandledConnection;
        private long lastUseTimestamp;

        private Runnable poolHealthKeeper;

        StoredConnection(Connection connection,Connector dbConnector) {
            System.out.println("New Connection");
            this.connection = connection;
            this.dbConnector = dbConnector;
            initTimestamp = System.currentTimeMillis();
            lastUseTimestamp = initTimestamp;
            poolHealthKeeper = () -> {
                if (dbConnector.activeHandledConnections.contains(this)) {
                   if (autoDelete()) {
                       return;
                   }
                }
                Connector.conPoolHealthService.schedule(poolHealthKeeper,60000, TimeUnit.MILLISECONDS);
            };
            Connector.conPoolHealthService.schedule(poolHealthKeeper,minimumExistenceTime+10000, TimeUnit.MILLISECONDS);
            dbConnector.activeHandledConnections.add(this);
        }

        HandledConnection use() {
            if (this.inUseHandledConnection == null) {
                lastUseTimestamp = System.currentTimeMillis();
                this.inUseHandledConnection = new HandledConnection(this);
                return inUseHandledConnection;
            } else return null;
        }

        Connection getConnection(HandledConnection handledConnection) {
            if (this.inUseHandledConnection == handledConnection) {
                return this.connection;
            } else throw new RuntimeException("Using finished HandledConnection");
        }

        private void finished(HandledConnection inUseHandledConnection) {
            if (this.inUseHandledConnection == inUseHandledConnection) {
                this.inUseHandledConnection = null;
                dbConnector.availableOpenConnections.add(this);
            }
        }

        private boolean autoDelete() {
            long millisSinceLastUse = System.currentTimeMillis() - lastUseTimestamp;
            if (inUseHandledConnection == null) {
                if ((System.currentTimeMillis() - initTimestamp) > minimumExistenceTime) {
                    if (millisSinceLastUse > inactiveHandleTimeout) {
                        close();
                        return true;
                    }
                }
            } else if (millisSinceLastUse > activeHandleTimeout) {
                close();
                return true;
            }
            return false;
        }

        private void close() {
            inUseHandledConnection = null;
            dbConnector.activeHandledConnections.remove(this);
            try {connection.close();} catch (Exception ignored) {}
        }
    }

    static ScheduledExecutorService conPoolHealthService = Executors.newScheduledThreadPool(1);

    private CopyOnWriteArrayList<StoredConnection> activeHandledConnections = new CopyOnWriteArrayList<>();
    private ConcurrentLinkedQueue<StoredConnection> availableOpenConnections = new ConcurrentLinkedQueue<>();

    private final int maxHandledDBConnections;
    private final DatabaseKey dbKey;

    protected Connector(DatabaseKey dbKey, int maxHandledDBConnections) {
        this.dbKey = dbKey;
        this.maxHandledDBConnections = maxHandledDBConnections;
    }

    protected void closeAllHandledConnections() {
        activeHandledConnections.clone();
    }

    private StoredConnection getConnectionFromPool() throws SQLException {
        StoredConnection storedConnection = availableOpenConnections.poll();
        if (storedConnection != null) return storedConnection;
        if (activeHandledConnections.size() < maxHandledDBConnections) {
            return new StoredConnection(getNewConnection(),this);
        }
        long sTime =  System.currentTimeMillis();
        while (sTime + 1000 > System.currentTimeMillis()) {
            storedConnection = availableOpenConnections.poll();
            if (storedConnection != null) return storedConnection;
        }
        throw new SQLException("Database pool timed out");
    }

    protected HandledConnection getNewHandledConnection() throws SQLException {
        return getConnectionFromPool().use();
    }


    protected boolean testKey() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + dbKey.host + ":" + dbKey.port + "/", dbKey.username, dbKey.passwd).isValid(1000);
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

    abstract void conError(Exception e);


    public static class HandledConnection {
        final StoredConnection storedConnection;
        public HandledConnection(StoredConnection connHandle) {
            this.storedConnection = connHandle;
        }

        public <T> T handleAndCloseWithResult(ExecuteQueryWithResult<T> query) throws Exception {
            try {
                return query.execute(storedConnection.getConnection(this));
            } finally {
                finished();
            }
        }

        public void handleAndClose(ExecuteQuery query) throws Exception {
            try {
                query.execute(storedConnection.getConnection(this));
            } finally {
                finished();
            }
        }

        private void finished() {
            storedConnection.finished(this);
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
