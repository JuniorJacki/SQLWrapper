package de.juniorjacki.utils;


import de.juniorjacki.utils.sql.Connector;
import de.juniorjacki.utils.sql.Database;

import java.sql.SQLException;

class SQLTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        new SQLTest().newDatabase();
    }

    void newDatabase() {
        Database db = SQL.newDatabase("Ente",new Connector.DatabaseKey(Connector.DatabaseType.MySQL,"localhost",3306,"jsql",System.getenv("dbUser"),System.getenv("dbPasswd")),10, e -> System.out.println("Con Error " + e));
        try {
            db.registerTable(TestTable.class);
            db.registerTable(TestTable3.class);
            db.initiate();
            TestTable table = db.getTable(TestTable.class);
            TestTable3 table2 = (TestTable3) db.getTable(TestTable3.class);

            table2.newRowQuery().execute().ifPresent(System.out::println);


           // table.insert(new TestTable.Test("ente2","kannn",5));
            //table.newColumnQuery(TestTable.Property.surName).update("banane");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}