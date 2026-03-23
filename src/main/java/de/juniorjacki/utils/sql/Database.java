package de.juniorjacki.utils.sql;

import de.juniorjacki.utils.sql.query.QueryBuilder;
import de.juniorjacki.utils.sql.routine.Routine;
import de.juniorjacki.utils.sql.routine.RoutineRunner;
import de.juniorjacki.utils.sql.structure.DatabankHandler;
import de.juniorjacki.utils.sql.structure.Table;
import de.juniorjacki.utils.sql.type.DatabaseProperty;
import de.juniorjacki.utils.sql.type.DatabaseRecord;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Database extends Connector implements DatabankHandler {
    private final String dbName;
    private final Connector.dbKey databaseKey;
    private AtomicReference<Connection> currentConnection = new AtomicReference<>();
    private AtomicReference<CompletableFuture<Connection>> currentConnectionProcess = new AtomicReference<>();

    private AtomicReference<List<Class<Table<?,?,?>>>> waitingTables = new AtomicReference<>(new ArrayList<>());
    private ConcurrentHashMap<Class<Table<?,?,?>>,Table<?,?,?>> activeTables = new ConcurrentHashMap<>();
    private RoutineRunner routineRunner = new RoutineRunner();

    private boolean isActive() {return currentConnection.get() != null;}
    private boolean isInitiated = false;

    public Database(String databaseName, Connector.dbKey databaseKey) {
        this.dbName = databaseName;
        this.databaseKey = databaseKey;
    }

    public <T extends Table<T,E,R>, E extends Enum<E> & DatabaseProperty, R extends Record & DatabaseRecord<R, E>> void registerTable(Class<T> tableClass) throws Exception {
        if (isInitiated) {
            activeTables.put((Class<Table<?,?, ?>>) tableClass,buildDatabaseTable(this,getActiveConnection(), (Class<Table<?,?, ?>>) tableClass));
        } else waitingTables.get().add((Class<Table<?,?, ?>>) tableClass);
    }

    public <T extends Table<T,E,R>, E extends Enum<E> & DatabaseProperty, R extends Record & DatabaseRecord<R, E>> Table<?,?,?> getTable(Class<T> tableClass) {
        return (Table<T,E, R>) activeTables.get(tableClass);
    }

    public void registerNewRoutine(Routine routine) {
        routineRunner.registerNewRoutine(routine);
    }

    public void unregisterRoutine(Routine routine) {
        routineRunner.unregisterRoutine(routine);
    }

    public boolean initiateConnection() throws Exception {
        if (isActive()) return true;
        currentConnection.set(getNewConnection(databaseKey));
        if (!checkConnection(currentConnection.get())) {
            logger.error("Connection is unstable! Database Ping is Higher than 5000ms");
            return false;
        }
        try {
            for (Class<Table<?,?, ?>> tableClass : waitingTables.get()) {
                activeTables.put(tableClass,buildDatabaseTable(this,currentConnection.get(), tableClass));
            }
        } catch (Exception exception) {
            exit();
            throw exception;
        }
        isInitiated = true;
        routineRunner.startRoutineExecution();
        return true;
    }

    /**
     * @return Current SQL Connection. May Throw Exception if service is not started
     */
    public Optional<Connection> getConnection() {
        return Optional.ofNullable(getActiveConnection());
    }


    private Connection getActiveConnection() {
        if (currentConnection.get() != null) {
            if (checkConnection(currentConnection.get())) return currentConnection.get();
            closeConnection(currentConnection.get());
            currentConnection.set(null);
        }
        if (currentConnectionProcess.get() == null) {
            currentConnectionProcess.set(getNewConnectionAsync(databaseKey));
        }
        try {
            Connection newConnection = currentConnectionProcess.get().get(10000, TimeUnit.MILLISECONDS);
            if (newConnection != null) {
                currentConnection.set(newConnection);
                currentConnectionProcess = null;
            }
            return newConnection;
        } catch (Exception exception) {
            logger.error("Database request Failed, Timeout! Database Offline");
            return null;
        }
    }

    public void exit() {
        stopAsyncConnectionGetter();
        if (currentConnection != null) {
            routineRunner.stopRoutineExecution();
            closeConnection(currentConnection.get());
            currentConnection.set(null);
        }
    }

}
