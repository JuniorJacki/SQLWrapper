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

    public Database(String databaseName, DatabaseKey databaseKey,int connectionPoolSize, ConErrorHandler connectionErrorHandler) {
        super(databaseKey,connectionPoolSize);
        this.dbName = databaseName;
        this.connectionErrorHandler = connectionErrorHandler;
    }

    /**
     Registers a database table class.
     If the database system is already initiated, the table is immediately built and added to the active tables.
     If the database is not yet initiated, the table class is added to a waiting list and will be registered later during initialization.

     @param <T> the table type extending Table
     @param <E> the enum type that defines the table columns and implements DatabaseProperty
     @param <R> the record type that holds the table data and implements DatabaseRecord
     @param tableClass the class of the table to register
     @throws Exception if an error occurs while building the table
     */
    public <T extends Table<T,E,R>, E extends Enum<E> & DatabaseProperty, R extends Record & DatabaseRecord<R, E>> void registerTable(Class<T> tableClass) throws Exception {
        if (activeTables.containsKey(tableClass) || waitingTables.get().contains(tableClass)) throw new IllegalArgumentException("Table is already registered");
        if (isInitiated) {
            getNewHandledConnection().handleAndClose(connection -> activeTables.put((Class<Table<?,?, ?>>) tableClass,buildDatabaseTable(this,connection, (Class<Table<?,?, ?>>) tableClass)));
        } else waitingTables.get().add((Class<Table<?,?, ?>>) tableClass);
    }

    /**
     Returns the registered table instance for the given table class.

     @param <T> the table type
     @param <E> the column enum type
     @param <R> the record type
     @param tableClass the class of the table to retrieve
     @return the active Table instance, or null if the table has not been registered
     */
    public <T extends Table<T,E,R>, E extends Enum<E> & DatabaseProperty, R extends Record & DatabaseRecord<R, E>> Table<?,?,?> getTable(Class<T> tableClass) {
        return (Table<T,E, R>) activeTables.get(tableClass);
    }

    /**
     Registers a new database routine (stored procedure or function).

     @param routine the routine to register
     */
    public void registerNewRoutine(Routine routine) {
        routineRunner.registerNewRoutine(routine);
    }

    /**

     Unregisters a previously registered database routine.
     The routine will no longer be managed by the routine runner.

     @param routine the routine to unregister
     */
    public void unregisterRoutine(Routine routine) {
        routineRunner.unregisterRoutine(routine);
    }

    /**
     * Initiates the database system.
     *
     * Tests the database connection. Throws a Exception if the connection fails.
     * Builds all tables that were previously registered and stored in the waiting list.
     * Starts the routine execution engine.
     *
     * @throws Exception if table building fails or any other error occurs during initialization
     */
    public void initiate() throws Exception {
        if (isActive()) return;
        if (!testKey()) throw new RuntimeException("Database connection failed");
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
     * Returns a raw SQL Connection that is not managed by the connection pool.
     *
     * @return a new database Connection
     * @throws SQLException if the database service is not initiated or the database is not reachable
     * @throws IllegalStateException if the database has not been initiated yet
     */
    public Connection getConnection() throws SQLException {
        if (!isInitiated) throw new IllegalStateException("Database is not initiated");
        return getNewConnection();
    }

    /**
     * Returns a new HandledConnection wrapper.
     * The returned HandledConnection automatically manages the underlying connection
     * (returns it to the pool after use).
     *
     * @return a new HandledConnection
     * @throws SQLException if the database service is not initiated or the database is not reachable
     * @throws IllegalStateException if the database has not been initiated yet
     */
    public HandledConnection getHandledConnection() throws SQLException {
        if (!isInitiated) throw new IllegalStateException("Database is not initiated");
        return getNewHandledConnection();
    }


    /**
     * Shuts down the database system
     */
    public void exit() {
        if (isInitiated) {
            routineRunner.stopRoutineExecution();
            closeAllHandledConnections();
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
