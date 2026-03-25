package de.juniorjacki.utils.sql;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Database extends Connector implements DatabankHandler {
    private final String dbName;

    private AtomicReference<List<Class<Table<?,?,?>>>> waitingTables = new AtomicReference<>(new ArrayList<>());
    private ConcurrentHashMap<Class<Table<?,?,?>>,Table<?,?,?>> activeTables = new ConcurrentHashMap<>();
    private RoutineRunner routineRunner = new RoutineRunner();

    private final ConErrorHandler connectionErrorHandler;

    public boolean isActive() {return isInitiated;}
    private boolean isInitiated = false;

    public Database(String databaseName, Connector.dbKey databaseKey, ConErrorHandler connectionErrorHandler) {
        super(databaseKey);
        this.dbName = databaseName;
        this.connectionErrorHandler = connectionErrorHandler;
    }

    public <T extends Table<T,E,R>, E extends Enum<E> & DatabaseProperty, R extends Record & DatabaseRecord<R, E>> void registerTable(Class<T> tableClass) throws Exception {
        if (isInitiated) {
            getNewHandledConnection().handleAndClose(connection -> activeTables.put((Class<Table<?,?, ?>>) tableClass,buildDatabaseTable(this,connection, (Class<Table<?,?, ?>>) tableClass)));
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

    public void initiate() throws Exception {
        if (isActive()) return;
        HandledConnection handleCon = getNewHandledConnection();
        try {
            handleCon.handleAndCloseWithResult(connection -> {
                for (Class<Table<?,?, ?>> tableClass : waitingTables.get()) {
                    activeTables.put(tableClass,buildDatabaseTable(this,connection, tableClass));
                }
                return null;
            });
        } catch (Exception exception) {
            exit();
            throw exception;
        }
        isInitiated = true;
        routineRunner.startRoutineExecution();
    }

    /**
     * @return New SQL Connection. May Throw Exception if service is not started or Database is not reachable
    */
    public Connection getConnection() throws SQLException {
        if (!isInitiated) throw new IllegalStateException("Database is not initiated");
        return getNewConnection();
    }

    public HandledConnection getHandledConnection() throws SQLException {
        if (!isInitiated) throw new IllegalStateException("Database is not initiated");
        return getNewHandledConnection();
    }


    public void exit() {
        if (isInitiated) {
            routineRunner.stopRoutineExecution();
            isInitiated = false;
        }
    }

    public String getDbName() {
        return dbName;
    }

    @Override
    void conError(Exception e) {
        connectionErrorHandler.handle(e);
    }
}
