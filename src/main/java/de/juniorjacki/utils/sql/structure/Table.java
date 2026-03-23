package de.juniorjacki.utils.sql.structure;

import de.juniorjacki.utils.sql.Database;
import de.juniorjacki.utils.sql.type.DatabaseProperty;
import de.juniorjacki.utils.sql.type.DatabaseRecord;
import de.juniorjacki.utils.sql.type.DatabaseType;

import java.util.ArrayList;
import java.util.List;

public abstract class Table<T extends Table<T,E, R>,E extends Enum<E> & DatabaseProperty,R extends Record & DatabaseRecord<R,E>> {
    public Database getDatabase() {
        return database;
    }

    public T getInstance()
    {
        return (T) this;
    }

    private Database database;

    /**
     * Init Method (Final)
     * @param database Database to init this Table to
     */
    public void setDatabase(Database database) {if (this.database == null) this.database = database;}
    public String tableName() {
        return getTableRecord().getSimpleName();
    }
    public abstract Class<R> getTableRecord();
    public abstract List<E> getProperties();
    public void onCreation() throws Exception {}

    public List<Property> tableProperties() {
        List<Property> properties = new ArrayList<>();
        for (DatabaseProperty property : getProperties()) {
            properties.add(new Property(property.name(), property.isKey(), property.isUnique(), property.getType(), property.extendLength()));
        }
        return properties;
    }
    public record Property(String dbName, boolean key, boolean unique, DatabaseType dataType, int extendedLength) {};
}


