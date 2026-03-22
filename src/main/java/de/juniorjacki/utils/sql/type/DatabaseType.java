package de.juniorjacki.utils.sql.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public abstract class DatabaseType {
    private final Class<?> type;
    private final List<Class<?>> aliasTypes;
    private final TriFunction<ResultSet, String, Object> resultSetConverter;
    private final TriConsumer<PreparedStatement, Integer, Object> parameterSetter;
    private final Function<Integer, String> sqlTypeMapper;

    public DatabaseType(Class<?> type,
                        List<Class<?>> aliasTypes,
                        TriFunction<ResultSet, String, Object> resultSetConverter,
                        TriConsumer<PreparedStatement, Integer, Object> parameterSetter,
                        Function<Integer, String> sqlTypeMapper) {
        this.type = type;
        this.aliasTypes = aliasTypes;
        this.resultSetConverter = resultSetConverter;
        this.parameterSetter = parameterSetter;
        this.sqlTypeMapper = sqlTypeMapper;
    }


    public Class<?> getTypeClass() {
        return type;
    }

    public List<Class<?>> getAliasTypes() {
        return aliasTypes;
    }

    public TriConsumer<PreparedStatement, Integer, Object> getParameterSetter() {
        return parameterSetter;
    }

    public Function<Integer, String> getSqlTypeMapper() {
        return sqlTypeMapper;
    }

    public TriFunction<ResultSet, String, Object> getResultSetConverter() {
        return resultSetConverter;
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
