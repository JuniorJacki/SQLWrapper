package de.juniorjacki.utils;

import de.juniorjacki.utils.sql.Connector;
import de.juniorjacki.utils.sql.Database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQL {

    public static boolean dbQueryLogging = false;

    static ConcurrentHashMap<String,Database> databaseMap = new ConcurrentHashMap<>();

    public static Database newDatabase(String databaseName, Connector.DatabaseKey databaseKey,int connectionPoolSize, Connector.ConErrorHandler conErrorHandler) {
        if (databaseMap.containsKey(databaseName)) throw new RuntimeException("Database with Name " +databaseName+ " already exists");
        Database newDB = new Database(databaseName, databaseKey,connectionPoolSize,conErrorHandler);
        databaseMap.put(databaseName, newDB);
        return newDB;
    }

    public Map<String,Database> getDatabases() {
        return databaseMap;
    }

}
