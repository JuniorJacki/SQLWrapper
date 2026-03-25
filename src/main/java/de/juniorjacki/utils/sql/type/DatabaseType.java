package de.juniorjacki.utils.sql.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

public abstract class DatabaseType {
    private final Class<?> javaTypeClass;
    private final TriFunction<ResultSet, String, Object> extractData;
    private final TriConsumer<PreparedStatement, Integer, Object> setData;
    private final Function<Integer, String> sqlTypeMapper;

    public DatabaseType(Class<?> javaTypeClass,
                        TriFunction<ResultSet, String, Object> extractData,
                        TriConsumer<PreparedStatement, Integer, Object> parameterSetter,
                        Function<Integer, String> sqlTypeMapper) {
        this.javaTypeClass = javaTypeClass;
        this.extractData = extractData;
        this.setData = parameterSetter;
        this.sqlTypeMapper = sqlTypeMapper;
    }


    public Class<?> getTypeClass() {
        return javaTypeClass;
    }

    public TriConsumer<PreparedStatement, Integer, Object> getSetData() {
        return setData;
    }

    public Function<Integer, String> getSqlTypeMapper() {
        return sqlTypeMapper;
    }

    public TriFunction<ResultSet, String, Object> getExtractData() {
        return extractData;
    }

    /**
     * Returns the SQL type for this DatabaseType, considering the extended length.
     *
     * @param extendedLength The length for types like VARCHAR or BINARY, or 0 for default
     * @return The SQL type as a string
     */
    public String getSQLType(int extendedLength) {
        return sqlTypeMapper.apply(extendedLength);
    }

    /**
     * Functional interface for setting PreparedStatement parameters.
     */
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v) throws SQLException;
    }

    /**
     * Functional interface for Getting Results.
     */
    @FunctionalInterface
    public interface TriFunction<T, U, V> {
        V apply(T t, U u) throws SQLException;
    }


}
