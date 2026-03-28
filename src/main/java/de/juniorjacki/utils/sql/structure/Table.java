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

    /**
     * The parent Database this table belongs to.
     */
    private Database database;

    /**
     * Init Method (Final) SYSTEM METHOD
     * @param database Database to init this Table to
     */
    public void setDatabase(Database database) {if (this.database == null) this.database = database;}
    public String tableName() {
        return getTableRecord().getSimpleName();
    }
    /**
     * Returns the record class used to hold the data of one row in this table.
     * This is the concrete record implementation that implements DatabaseRecord.
     *
     * @return the Class of the table's record type
     */
    public abstract Class<R> getTableRecord();

    /**
     * Returns all properties (columns) of this table as defined in the Property enum.
     * Each property contains metadata such as type, whether it is a key, unique constraint, etc.
     *
     * @return an unmodifiable list of all column properties for this table
     */
    public abstract List<E> getProperties();

    /**
     * This method is called automatically after a new table has been created in the database.
     * It is only invoked when the table did not exist before and was newly created during
     * the initialization or registration process.
     * Override this method in your table class to perform custom actions after table creation,
     * such as inserting default data, creating indexes, or setting up triggers.
     * @throws Exception if an error occurs during post-creation logic
     */
    public void onCreation(Table<T,E,R> table) throws Exception {}

    public List<Property> tableProperties() {
        List<Property> properties = new ArrayList<>();
        for (DatabaseProperty property : getProperties()) {
            properties.add(new Property(property.name(), property.isKey(), property.isUnique(), property.getType(), property.extendLength()));
        }
        return properties;
    }
    public record Property(String dbName, boolean key, boolean unique, DatabaseType dataType, int extendedLength) {};
}


