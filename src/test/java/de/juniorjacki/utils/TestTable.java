package de.juniorjacki.utils;

import de.juniorjacki.utils.sql.Database;
import de.juniorjacki.utils.sql.query.QueryBuilder;
import de.juniorjacki.utils.sql.structure.Table;
import de.juniorjacki.utils.sql.type.DatabaseProperty;
import de.juniorjacki.utils.sql.type.DatabaseRecord;
import de.juniorjacki.utils.sql.type.DatabaseType;
import de.juniorjacki.utils.sql.type.defaultType.INTEGER;
import de.juniorjacki.utils.sql.type.defaultType.STRING;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestTable extends Table<TestTable.Property, TestTable.Test> implements QueryBuilder<TestTable, TestTable.Property,TestTable.Test> {
    public TestTable(Database database) {
        super(database);
    }

    @Override
    public Class<TestTable.Test> getTableRecord() {
        return TestTable.Test.class;
    }

    @Override
    public List<Property> getProperties() {
        return Arrays.asList(TestTable.Property.values());
    }



    @Override
    public TestTable getInstance() {
        return this;
    }

    @Override
    public Database getDatabase() {
        return database;
    }


    public record Test(String preName, String surName, Integer age) implements DatabaseRecord<Test, Property> {
        @Override
        public TestTable.Test getInstance() {
            return this;
        }
    }

    public enum Property implements DatabaseProperty {
        preName(true, STRING.INSTANCE),
        surName(false,STRING.INSTANCE),
        age(false,INTEGER.INSTANCE),
        ;

        private final boolean key;
        private final boolean unique;
        private final DatabaseType type;


        Property(boolean key, DatabaseType type) {
            this.key = key;
            unique = false;
            this.type = type;
        }

        Property(boolean key,boolean unique, DatabaseType type) {
            this.key = key;
            this.unique = unique;
            this.type = type;
        }

        @Override
        public boolean isKey() {
            return key;
        }

        @Override
        public boolean isUnique() {
            return unique;
        }

        @Override
        public DatabaseType getType() {
            return type;
        }

        @Override
        public int extendLength() {
            return 0;
        }
    }
}