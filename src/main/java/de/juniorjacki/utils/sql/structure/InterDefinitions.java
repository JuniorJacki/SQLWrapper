package de.juniorjacki.utils.sql.structure;



import de.juniorjacki.utils.SQL;
import de.juniorjacki.utils.sql.type.DatabaseProperty;
import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HexFormat;

public class InterDefinitions {

    public static Object getTypedValue(ResultSet rs, DatabaseProperty returnColumn) throws SQLException {
        return InterDefinitions.getTypedValue(rs,returnColumn,null);
    }

    /**
     * Retrieves a typed value from a ResultSet for the specified column and table.
     *
     * @param rs           The ResultSet containing the query results
     * @param returnColumn The column to retrieve
     * @param table        The table associated with the column (optional, for qualifying column names)
     * @return The typed value of the column
     * @throws IllegalArgumentException if returnColumn is null
     * @throws SQLException if a database error occurs
     */
    public static  Object getTypedValue(ResultSet rs, DatabaseProperty returnColumn, Table<?,?, ?> table) throws SQLException {
        if (returnColumn == null) {
            throw new IllegalArgumentException("Return column cannot be null");
        }
        String columnName = returnColumn.name();
        if (table != null) {
            columnName = table.tableName() + "." + columnName;
        }
        return returnColumn.getType().getResultSetConverter().apply(rs, columnName);
    }

    /**
     * Sets a parameter value in a PreparedStatement at the specified index.
     *
     * @param prepStatement The PreparedStatement to set the parameter on
     * @param index        The parameter index
     * @param value        The value to set
     * @throws SQLException if a database error occurs
     */
    public static void setParameter(PreparedStatement prepStatement, int index, Object value) throws SQLException {
        try {
            SQL.forClass(value.getClass()).getParameterSetter().accept(prepStatement, index, value);
        } catch (Exception e) {
            throwDBError(e);
        }
    }

    /**
     * Sets a constructor argument from a ResultSet for a specific property and component name.
     *
     * @param resultSet      The ResultSet containing the query results
     * @param property       The property to retrieve
     * @param constructorArgs The array to store the constructor argument
     * @param index          The index in the constructorArgs array
     * @param componentName  The column name in the ResultSet
     * @param <E>           The enum type representing properties of the table
     * @throws SQLException if a database error occurs
     */
    public static <E extends Enum<E> & DatabaseProperty> void setConstructorArg(ResultSet resultSet, E property, Object[] constructorArgs, int index, String componentName) throws SQLException {
        if (property == null) {
            throw new IllegalArgumentException("Property cannot be null");
        }
        constructorArgs[index] = property.getType().getResultSetConverter().apply(resultSet, componentName);
    }

    /**
     * Returns the SQL type for a given Java class, considering an extended length for certain types.
     *
     * @param dataType      The Java class to map to an SQL type
     * @param extendedLength The length for types like VARCHAR or BINARY, or 0 for default
     * @return The SQL type as a string
     * @throws IllegalArgumentException if dataType is null
     */
    public static String getSQLType(DatabaseType dataType, int extendedLength) {
        if (dataType == null) {
            throw new IllegalArgumentException("Data type cannot be null");
        }
        return dataType.getSQLType(extendedLength);
    }


    public interface CompareOperator {
        String sql();
    }

    public enum Operator implements CompareOperator {
        EQUALS("="),
        EQUALS_CASE_SENSITIVE(" COLLATE utf8mb4_bin ="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_THAN_OR_EQUAL(">="),
        LESS_THAN_OR_EQUAL("<="),
        NOT_EQUAL("<>"),
        ;
        private final String sql;
        Operator(String sql) {
            this.sql = sql;
        }

        @Override
        public String sql() {
            return sql;
        }
    }

    public enum INOperator implements CompareOperator {
        IN(" IN "),
        NOT_IN( "NOT IN ");

        private final String sql;
        INOperator(String sql) {
            this.sql = sql;
        }

        @Override
        public String sql() {
            return sql;
        }
    }

    public enum Order{
        ASCENDING("ASC"),
        DESCENDING("DESC");

        public final String sql;
        Order(String sql) {
            this.sql = sql;
        }
    }

    private static void throwDBError(Exception e){
       e.printStackTrace();
    }
}
