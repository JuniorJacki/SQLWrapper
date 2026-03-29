package de.juniorjacki.utils;


import de.juniorjacki.utils.sql.Connector;
import de.juniorjacki.utils.sql.Database;
import de.juniorjacki.utils.sql.query.QueryBuilder;
import de.juniorjacki.utils.sql.structure.InterDefinitions;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

class SQLTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        new SQLTest().newDatabase();
    }

    void newDatabase() {
        SQL.dbQueryLogging = true;
        Database db = SQL.newDatabase("Ente",new Connector.DatabaseKey(Connector.DatabaseType.MySQL,"localhost",3306,"jsql",System.getenv("dbUser"),System.getenv("dbPasswd")),10, e -> System.out.println("Con Error " + e));
        try {
            db.registerTable(TestTable.class);
            db.registerTable(TestTable3.class);
            db.initiate();
            TestTable table = db.getTable(TestTable.class);
            TestTable3 table2 = (TestTable3) db.getTable(TestTable3.class);

            table2.newRowQuery().execute().ifPresent(System.out::println);

            System.out.println(table2.newRowQuery().setCondition(new QueryBuilder.ConditionQueryBuilder<>(new QueryBuilder.Condition<>(TestTable3.Property.uuid, InterDefinitions.Operator.EQUALS,UUID.randomUUID()))).exists());
            table2.newRowQuery().setCondition(new QueryBuilder.ConditionQueryBuilder<>(new QueryBuilder.Condition<>(TestTable3.Property.age, InterDefinitions.INOperator.IN, Set.of(0,5))).AND(new QueryBuilder.Condition<>(TestTable3.Property.age, InterDefinitions.Operator.EQUALS,5))).exists();

            Optional<List<?>> age = table2.newColumnQuery(TestTable3.Property.age).execute();

           // table.insert(new TestTable.Test("ente2","kannn",5));
            //table.newColumnQuery(TestTable.Property.surName).update("banane");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}