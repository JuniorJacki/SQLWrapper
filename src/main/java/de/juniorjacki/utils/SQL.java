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

    static List<DatabaseType> registeredTypes = new ArrayList<>();

    static {
        registeredTypes.add(STRING.INSTANCE);
        registeredTypes.add(MEDIUMSTRING.INSTANCE);
        registeredTypes.add(LONGSTRING.INSTANCE);
        registeredTypes.add(UUID.INSTANCE);
        registeredTypes.add(INTEGER.INSTANCE);
        registeredTypes.add(LONG.INSTANCE);
        registeredTypes.add(BYTE_ARRAY.INSTANCE);
        registeredTypes.add(TINYBLOB.INSTANCE);
        registeredTypes.add(BLOB.INSTANCE);
        registeredTypes.add(MEDIUMBLOB.INSTANCE);
        registeredTypes.add(LONGBLOB.INSTANCE);
    }

    static void registerDataType(DatabaseType type) {
        registeredTypes.add(type);
    }

    /**
     * Finds the DatabaseType for a given class, mapping primary types, alias types, and primitive types to their
     * corresponding DatabaseType, defaulting to STRING for unknown types.
     *
     * @param type The class to find the DatabaseType for
     * @return The corresponding DatabaseType
     */
    public static DatabaseType forClass(Class<?> type) {
        for (DatabaseType dbType : registeredTypes) {
            if (dbType.getTypeClass() == type || dbType.getAliasTypes().contains(type)) {
                return dbType;
            }
        }
        return STRING.INSTANCE; // Fallback for unknown types
    }

    static ConcurrentHashMap<String,Database> databaseMap = new ConcurrentHashMap<>();

    public static Database newDatabase(String databaseName, Connector.dbKey databaseKey) {
        System.out.println(databaseKey);
        if (databaseMap.containsKey(databaseName)) throw new RuntimeException("Database with Name " +databaseName+ " already exists");
        Database newDB = new Database(databaseName, databaseKey);
        databaseMap.put(databaseName, newDB);
        return newDB;
    }

    public Map<String,Database> getDatabases() {
        return databaseMap;
    }

}
