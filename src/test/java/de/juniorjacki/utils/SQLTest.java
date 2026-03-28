package de.juniorjacki.utils;


import de.juniorjacki.utils.sql.Connector;
import de.juniorjacki.utils.sql.Database;
import de.juniorjacki.utils.sql.query.QueryBuilder;
import de.juniorjacki.utils.sql.structure.InterDefinitions;

import java.sql.SQLException;

class SQLTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        new SQLTest().newDatabase();
    }

    void newDatabase() {
        Database db = SQL.newDatabase("Ente",new Connector.DatabaseKey(Connector.DatabaseType.MySQL,"localhost",3306,"jsql",System.getenv("dbUser"),System.getenv("dbPasswd")),10, e -> System.out.println("Con Error " + e));
        try {
            db.registerTable(TestTable.class);
            db.registerTable(TestTable2.class);
            db.initiate();
            TestTable table = db.getTable(TestTable.class);
            TestTable2 table2 = (TestTable2) db.getTable(TestTable2.class);
            table.newRowQuery().setCondition(new QueryBuilder.ConditionQueryBuilder<>(new QueryBuilder.Condition<>(TestTable.Property.preName, InterDefinitions.Operator.EQUALS,"ente"))).execute().ifPresent(System.out::println);

            table.newRowQuery().join(table2,new QueryBuilder.Binding<>(TestTable.Property.surName, TestTable2.Property.preName)).execute().ifPresent(System.out::println);


           // table.insert(new TestTable.Test("ente2","kannn",5));
            //table.newColumnQuery(TestTable.Property.surName).update("banane");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}