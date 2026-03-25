package de.juniorjacki.utils;

import de.juniorjacki.utils.sql.Connector;
import de.juniorjacki.utils.sql.Database;
import de.juniorjacki.utils.sql.type.DatabaseType;
import de.juniorjacki.utils.sql.type.defaultType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQL {

    public static boolean dbQueryLogging = true;

    static ConcurrentHashMap<String,Database> databaseMap = new ConcurrentHashMap<>();

    public static Database newDatabase(String databaseName, Connector.dbKey databaseKey, Connector.ConErrorHandler conErrorHandler) {
        if (databaseMap.containsKey(databaseName)) throw new RuntimeException("Database with Name " +databaseName+ " already exists");
        Database newDB = new Database(databaseName, databaseKey,conErrorHandler);
        databaseMap.put(databaseName, newDB);
        return newDB;
    }

    public Map<String,Database> getDatabases() {
        return databaseMap;
    }

}
