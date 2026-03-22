package de.juniorjacki.utils;


import de.juniorjacki.utils.sql.Connector;
import de.juniorjacki.utils.sql.Database;
import de.juniorjacki.utils.sql.query.QueryBuilder;
import de.juniorjacki.utils.sql.structure.InterDefinitions;
import de.juniorjacki.utils.sql.structure.Table;

class SQLTest {

    @org.junit.jupiter.api.Test
    void newDatabase() {
        Database db = SQL.newDatabase("Ente",new Connector.dbKey(Connector.DatabaseType.MySQL,"localhost",3306,"jsql",System.getenv("dbUser"),System.getenv("dbPasswd")));
        try {
            db.registerTable(TestTable.class);
            db.initiateConnection();
            TestTable table = (TestTable) db.getTable(TestTable.class);
            table.newRowQuery().execute().ifPresent(System.out::println);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}