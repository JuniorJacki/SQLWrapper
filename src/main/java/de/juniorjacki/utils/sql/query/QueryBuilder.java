package de.juniorjacki.utils.sql.query;

import de.juniorjacki.utils.SQL;
import de.juniorjacki.utils.sql.Database;
import de.juniorjacki.utils.sql.structure.InterDefinitions;
import de.juniorjacki.utils.sql.structure.Table;
import de.juniorjacki.utils.sql.type.DatabaseProperty;
import de.juniorjacki.utils.sql.type.DatabaseRecord;
import de.juniorjacki.utils.sql.type.DatabaseType;
import org.apache.logging.log4j.LogManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public interface QueryBuilder<G extends Table<E, R>,  E extends Enum<E> & DatabaseProperty,R extends java.lang.Record & DatabaseRecord<R, E>> {
    G getInstance();
    Database getDatabase();


    default RowQuery<G, R, E> newRowQuery() {
        return new RowQuery<>(getInstance(),getDatabase());
    }

    /**
     * Abstract base class for building SQL queries with conditions, grouping, ordering, and limits.
     *
     * @param <T> The type of the query class (for method chaining)
     * @param <S> The Table used for Queries
     * @param <E> The enum type representing database properties, extending DatabaseProperty
     */
    public abstract class Query<T,S extends Table, E extends Enum<E> & DatabaseProperty> {
        protected ConditionQueryBuilder<E> conditionQuery;
        protected int limit = -1;
        protected E orderBy;
        protected InterDefinitions.Order order;
        protected E groupBy;
        protected final S table;
        protected final Database database;

        /**
         * Constructs a Query with the specified table.
         *
         * @param table The table to query
         * @throws IllegalArgumentException if the table is null
         */
        protected Query(S table,Database database) {
            if (table == null || database == null) {
                throw new IllegalArgumentException("Table or Database cannot be null");
            }
            this.table = table;
            this.database = database;
        }

        /**
         * Sets the conditions for the data requested from the database.
         * This allows filtering the results based on specified conditions.
         *
         * @param conditionQuery The condition query builder containing the conditions
         * @return The current query instance for method chaining
         * @throws IllegalArgumentException if the conditionQuery is null
         */
        @SuppressWarnings("unchecked")
        public T setCondition(ConditionQueryBuilder<E> conditionQuery) {
            if (conditionQuery == null) {
                throw new IllegalArgumentException("Condition query cannot be null");
            }
            this.conditionQuery = conditionQuery;
            return (T) this;
        }

        /**
         * Limits the number of rows returned by the query.
         *
         * @param limit The maximum number of rows to return
         * @return The current query instance for method chaining
         * @throws IllegalArgumentException if the limit is negative
         */
        @SuppressWarnings("unchecked")
        public T limitBy(int limit) {
            this.limit = limit;
            return (T) this;
        }

        /**
         * Sets the sorting order for the query results.
         *
         * @param orderBy The database property to sort by
         * @param order The sort direction (ASC or DESC)
         * @return The current query instance for method chaining
         * @throws IllegalArgumentException if orderBy or order is null
         */
        @SuppressWarnings("unchecked")
        public T orderBy(E orderBy, InterDefinitions.Order order) {
            if (orderBy == null || order == null) {
                throw new IllegalArgumentException("OrderBy and order cannot be null");
            }
            this.orderBy = orderBy;
            this.order = order;
            return (T) this;
        }

        /**
         * Sets the grouping for the query results.
         *
         * @param groupBy The database property to group by
         * @return The current query instance for method chaining
         * @throws IllegalArgumentException if groupBy is null
         */
        @SuppressWarnings("unchecked")
        public T groupBy(E groupBy) {
            if (groupBy == null) {
                throw new IllegalArgumentException("GroupBy cannot be null");
            }
            this.groupBy = groupBy;
            return (T) this;
        }

        record ParameterSet(DatabaseType.TriConsumer<PreparedStatement,Integer,Object> parameterSetter,Object value){
            public void set(PreparedStatement preparedStatement,int index) throws SQLException {
                parameterSetter.accept(preparedStatement,index,value);
            }
        };
        record QuerySet(StringBuilder query,Queue<ParameterSet> parameters){};
        /**
         * Builds the base SQL query string with the specified select clause.
         * This method constructs the query including conditions, grouping, ordering, and limits.
         *
         * @param selectClause The SQL SELECT clause (e.g., "*" or "COUNT(*)")
         * @return A StringBuilder containing the constructed SQL query
         */
        protected QuerySet buildQueryBase(String selectClause) {
            StringBuilder query = new StringBuilder("SELECT ").append(selectClause)
                    .append(" FROM ").append(table.tableName()).append(" ");
            Queue<ParameterSet> parameters = new LinkedList<>();
            if (conditionQuery != null) {
                ConditionQueryBuilder.ConditionQuerySet querySet = conditionQuery.build();
                parameters.addAll(querySet.parameters);
                query.append(querySet.query);
            }
            if (groupBy != null) {
                query.append(" GROUP BY ").append(groupBy.name());
            }
            if (orderBy != null) {
                query.append(" ORDER BY ").append(orderBy.name()).append(" ").append(order.sql);
            }
            if (limit >= 0) {
                query.append(" LIMIT ").append(limit);
            }
            return new QuerySet(query, parameters);
        }

        /**
         * Builds the base SQL query string
         *
         * @return A StringBuilder containing the constructed SQL query
         */
        protected QuerySet buildBase() {
            StringBuilder query = new StringBuilder(" FROM ").append(table.tableName()).append(" ");
            Queue<ParameterSet> parameters = new LinkedList<>();
            if (conditionQuery != null) {
                ConditionQueryBuilder.ConditionQuerySet querySet = conditionQuery.build();
                parameters.addAll(querySet.parameters);
                query.append(querySet.query);
            }
            if (groupBy != null) {
                query.append(" GROUP BY ").append(groupBy.name());
            }
            if (orderBy != null) {
                query.append(" ORDER BY ").append(orderBy.name()).append(" ").append(order.sql);
            }
            if (limit >= 0) {
                query.append(" LIMIT ").append(limit);
            }
            return new QuerySet(query, parameters);
        }

        /**
         * Checks if at least one row exists for the query on the associated table.
         *
         * @return true if at least one row exists, false otherwise
         */
        public boolean exists() {
            int cLimit = limit;
            try {
                limitBy(1);
                QuerySet querySet = buildQueryBase("1");
                String query = querySet.query.toString();
                if (SQL.dbQueryLogging) LogManager.getLogger().info(query);
                try (var prepStatement = database.getConnection().get().prepareStatement(query)) {
                    for (int i = 0; i < querySet.parameters.size(); i++) {
                       querySet.parameters.poll().set(prepStatement,i+1);
                    }
                    try (ResultSet rs = prepStatement.executeQuery()) {
                        return rs.next();
                    }
                } catch (Exception e) {
                    throwDBError(e);
                    return false;
                }
            } finally {
                limitBy(cLimit);
            }
        }

        /**
         * Counts the number of rows that match the query for the associated table.
         *
         * @return The number of matching rows as a long
         */
        public long count() {
            QuerySet querySet = buildQueryBase("1");
            String query = querySet.query.toString();
            if (SQL.dbQueryLogging) LogManager.getLogger().info(query);
            try (var prepStatement = database.getConnection().get().prepareStatement(query)) {
                for (int i = 0; i < querySet.parameters.size(); i++) {
                    querySet.parameters.poll().set(prepStatement,i+1);
                }
                try (ResultSet rs = prepStatement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
                return 0L;
            } catch (Exception e) {
                throwDBError(e);
                return 0L;
            }
        }
    }

    class RowQuery<G extends Table<E, R>, R extends java.lang.Record & DatabaseRecord<R, E>, E extends Enum<E> & DatabaseProperty> extends Query<RowQuery<G,R,E>,G, E> {

        /**
         * Constructs a RowQuery for the specified table.
         *
         * @param table The table to query
         * @throws IllegalArgumentException if the table is null
         */
        public RowQuery(G table,Database database) {
            super(table,database);
        }



        /**
         * Executes the query and returns all matching rows as a list of records.
         *
         * @return An Optional containing the list of records, or empty if an error occurs
         */
        public Optional<List<R>> execute() {
            QuerySet querySet =buildQueryBase("*");
            String query = querySet.query.toString();
            if (SQL.dbQueryLogging) LogManager.getLogger().info(query);
            try (var prepStatement = database.getConnection().get().prepareStatement(query)) {
                for (int i = 0; i < querySet.parameters.size(); i++) {
                    querySet.parameters.poll().set(prepStatement,i+1);
                }
                try (ResultSet rs = prepStatement.executeQuery()) {
                    List<R> rows = new ArrayList<>();
                    while (rs.next()) {
                        rows.add((R) DatabaseRecord.populateRecord(table, rs));
                    }
                    return Optional.ofNullable(rows.isEmpty() ? null : rows);
                }
            } catch (Exception e) {
                throwDBError(e);
                return Optional.empty();
            }
        }

        /**
         * Executes the query
         *
         * @return Amount of Rows affected
         */
        public int delete() {
            QuerySet querySet = buildBase();
            StringBuilder query = querySet.query;
            query.insert(0, "DELETE");
            if (SQL.dbQueryLogging) LogManager.getLogger().info(query);
            try (var prepStatement = database.getConnection().get().prepareStatement(query.toString())) {
                for (int i = 0; i < querySet.parameters.size(); i++) {
                    querySet.parameters.poll().set(prepStatement,i+1);
                }
                return prepStatement.executeUpdate();
            } catch (Exception e) {
                throwDBError(e);
                return 0;
            }
        }

        /**
         * Executes the query and returns the first matching row.
         *
         * @return An Optional containing the first record, or empty if no rows exist or an error occurs
         */
        public Optional<R> executeOneRow() {
            int cLimit = limit;
            try {
                this.limitBy(1);
                return execute().map(List::getFirst);
            } finally {
                this.limitBy(cLimit);
            }
        }
    }

    class ConditionQueryBuilder<E extends Enum<E> & DatabaseProperty> implements Cloneable {
        Condition<E> initalCondition;
        Queue<Map.Entry<Condition<E>,ConditionType>> conditions = new LinkedList<>();

        public ConditionQueryBuilder(Condition<E> initalCondition) {
            this.initalCondition = initalCondition;
        }

        public ConditionQueryBuilder<E> AND(Condition<E> additionalCondition) {
            ConditionQueryBuilder<E> updated = clone();
            updated.conditions.add(new AbstractMap.SimpleEntry<Condition<E>,ConditionType>(additionalCondition, ConditionType.AND));
            return updated;
        }

        public ConditionQueryBuilder<E> OR(Condition<E> additionalCondition) {
            ConditionQueryBuilder<E> updated = clone();
            updated.conditions.add(new AbstractMap.SimpleEntry<Condition<E>,ConditionType>(additionalCondition, ConditionType.OR));
            return updated;
        }

        record ConditionQuerySet(StringBuilder query,Queue<Query.ParameterSet> parameters){};

        protected ConditionQuerySet build() {
            if (initalCondition == null) return new ConditionQuerySet(new StringBuilder(),new LinkedList<>());
            StringBuilder query = new StringBuilder();
            Queue<Query.ParameterSet> parametersQueue = new LinkedList<>();
            if (initalCondition.typeCheck()) {
                query = appendCondition(query,initalCondition,null,parametersQueue);
                Map.Entry<Condition<E>,ConditionType> condition;
                while((condition = conditions.poll()) != null) {
                    if (!condition.getKey().typeCheck()) {
                        condition.getKey().throwInputError();
                        return null;
                    }
                    query = appendCondition(query,condition.getKey(),condition.getValue(),parametersQueue);
                }
                query.insert(0, "WHERE ");
                return new ConditionQuerySet(query,parametersQueue);
            } else {
                initalCondition.throwInputError();
                return null;
            }
        }

        private StringBuilder appendCondition(StringBuilder currentQuery,Condition condition,ConditionType conditionType,Queue<Query.ParameterSet> parameters) {
            StringBuilder conditionQuery = new StringBuilder();
            conditionQuery.append(condition.column.name());
            conditionQuery.append(condition.operator.sql());
            if (condition.operator == InterDefinitions.INOperator.IN || condition.operator == InterDefinitions.INOperator.NOT_IN) {
                conditionQuery.append("(");
                boolean first = true;
                for (Object o : ((Set<Object>) condition.value)) {
                    if (first) {
                        first = false;
                    } else conditionQuery.append(",");
                    conditionQuery.append("?"); // VALUE
                    parameters.add(new Query.ParameterSet(SQL.forClass(o.getClass()).getParameterSetter(),o));
                }
                conditionQuery.append(")");
            } else {
                conditionQuery.append("?"); // VALUE
                parameters.add(new Query.ParameterSet(SQL.forClass(condition.value.getClass()).getParameterSetter(),condition.value));
            }


            if (conditionType != null) {
                switch (conditionType) {
                    case AND -> {
                        currentQuery.append(" AND ");
                        currentQuery.append(conditionQuery);
                    }
                    case OR ->  {
                        currentQuery.append(" OR ");
                        currentQuery.append(conditionQuery);
                    }
                }
            } else {
                currentQuery.append(conditionQuery);
            }
            return currentQuery;
        }

        @Override
        protected ConditionQueryBuilder<E> clone() {
            try {
                return (ConditionQueryBuilder<E>) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        enum ConditionType {
            AND,
            OR
        }
    }

    /**
     * Represents a binding between two database table columns, ensuring type compatibility.
     * This class links a column from a primary table to a column in a joined table,
     * verifying that both columns have the same data type.
     *
     * @param <G> The type of the primary table, extending {@link Table}.
     * @param <R> The type of the record for the primary table, extending {@link Record} and {@link DatabaseRecord}.
     * @param <E> The enum type for the primary table's properties, implementing {@link DatabaseProperty}.
     * @param <U> The type of the joined table, extending {@link Table}.
     * @param <A> The type of the record for the joined table, extending {@link Record} and {@link DatabaseRecord}.
     * @param <I> The enum type for the joined table's properties, implementing {@link DatabaseProperty}.
     */
    class Binding<G extends Table<E, R>, R extends java.lang.Record & DatabaseRecord<R, E>, E extends Enum<E> & DatabaseProperty,U extends Table<I, A>, A extends java.lang.Record & DatabaseRecord<A, I>, I extends Enum<I> & DatabaseProperty> {
        E ref;
        I joinref;

        /**
         * Constructs a new Binding between a primary table column and a joined table column.
         * Checks if the data types of both columns match. If they do not match, the query will fail
         *
         * @param ref     The column from the primary table.
         * @param joinref The column from the joined table.
         */
        public Binding(E ref, I joinref) {
            if (ref.getType() != joinref.getType()) {
                if (SQL.dbQueryLogging) LogManager.getLogger().info("Type mismatch for Binding between "+ref.name()+":"+ref.getType()+ " " +joinref.name()+":"+joinref.getType());
                this.ref = null;
                this.joinref = null;
                return;
            }
            this.ref = ref;
            this.joinref = joinref;
        }

        public E getRef() {
            return ref;
        }

        public I getJoinRef() {
            return joinref;
        }
    }


    class Condition<E extends Enum<E> & DatabaseProperty> {
        public E column;
        public Object value;
        public InterDefinitions.CompareOperator operator;

        public void throwInputError() {
            throw new RuntimeException("Invalid value type for " + column.name() + ": " + value.getClass().getName() +", expected: " + column.getType().getClass().getName());
        }

        public boolean typeCheck() {
            if (operator instanceof InterDefinitions.INOperator) {
                return ((Set<Object>)value).stream().allMatch(o -> column.getType().getTypeClass().isInstance(o));
            }

            if (column.getType().getTypeClass().isInstance(value)) {
                if (operator != InterDefinitions.Operator.EQUALS && operator != InterDefinitions.Operator.EQUALS_CASE_SENSITIVE) {
                    return column.getType().getTypeClass() != String.class;
                }
                return true;
            }
            return false;

        }

        public Condition(E conditionColumn, InterDefinitions.Operator comparisonOperator, Object conditionValue) {
            this.column = conditionColumn;
            this.value = conditionValue;
            this.operator = comparisonOperator;
        }

        public Condition(E conditionColumn, InterDefinitions.INOperator comparisonOperator, Set<?> conditionValue) {
            this.column = conditionColumn;
            this.operator = comparisonOperator;
            this.value = conditionValue;
        }

    }

    private static void throwDBError(Exception e){
        throw new RuntimeException(e);
    }
}
