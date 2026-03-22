package de.juniorjacki.utils.sql.type;


public interface DatabaseProperty {
    /**
     * Defines If Column is a Primary Key
     */
    boolean isKey();

    /**
     * Defines if Column is Unique
     */
    boolean isUnique();
    /**
     * Defines if Column Datatype
     */
    DatabaseType getType();
    /**
     * Defines if Column name
     */
    String name();

    /**
     * Defines extra Information for Column Datatype
     */
    int extendLength();
}
