package de.juniorjacki.utils.sql.structure;

import de.juniorjacki.utils.sql.Connector;
import de.juniorjacki.utils.sql.Database;
import de.juniorjacki.utils.sql.type.DatabaseProperty;
import de.juniorjacki.utils.sql.type.DatabaseRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static de.juniorjacki.utils.sql.structure.InterDefinitions.getSQLType;

public interface DatabankHandler {
    static final Logger logger = LogManager.getLogger(DatabankHandler.class);

    /**
     * Creates Tables for Database
     * @param con
     * @return
     */
    default <T extends Table<T,E,R>, E extends Enum<E> & DatabaseProperty, R extends Record & DatabaseRecord<R, E>> Table<?,?,?> buildDatabaseTable(Database database, Connection con, Class<Table<?,?, ?>> tableClass) throws Exception {
        Table<?,?,?> table = tableClass.getDeclaredConstructor().newInstance();
        table.setDatabase(database);
        con.createStatement().execute(generateCreateTableQuery(table.tableName(),table.tableProperties()));
        if (isTableEmpty(con, table.tableName())) table.onCreation();
        return table;
    }

    /**
     * Checks if a Table is Empty
     * @param conn Current Database Connection
     * @param tableName Table name to check if empty
     * @return If is Empty True, if not are an Error occurred False
     */
    private static boolean isTableEmpty(Connection conn, String tableName) {
        try (PreparedStatement prepStatement = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet rs = prepStatement.executeQuery()) {
            if (rs.next()) return rs.getInt(1) == 0;
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String generateCreateTableQuery(String tableName, List<Table.Property> properties) {
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        query.append(tableName);
        query.append(" (");

        List<String> primaryKeys = new ArrayList<>();

        for (int i = 0; i < properties.size(); i++) {
            Table.Property property = properties.get(i);
            query.append(property.dbName())
                    .append(" ")
                    .append(getSQLType(property.dataType(),property.extendedLength()));
            if (property.unique() && !property.key()) {
                query.append(" UNIQUE");
            }
            if (property.key()) {
                primaryKeys.add(property.dbName());
            }

            if (i < properties.size() - 1) {
                query.append(", ");
            }
        }

        if (!primaryKeys.isEmpty()) {
            query.append(", PRIMARY KEY (");
            query.append(String.join(", ", primaryKeys));
            query.append(")");
        }

        query.append(");");
        return query.toString();
    }
}
