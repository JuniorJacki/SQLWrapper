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
            db.registerTable(TestTable2.class);
            db.initiateConnection();
            TestTable table = (TestTable) db.getTable(TestTable.class);
            TestTable2 table2 = (TestTable2) db.getTable(TestTable2.class);
            table.newRowQuery().setCondition(new QueryBuilder.ConditionQueryBuilder<>(new QueryBuilder.Condition<>(TestTable.Property.preName, InterDefinitions.Operator.EQUALS,"ente"))).execute().ifPresent(System.out::println);

            table.newRowQuery().join(table2,new QueryBuilder.Binding<>(TestTable.Property.preName, TestTable2.Property.preName)).execute().ifPresent(System.out::println);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}