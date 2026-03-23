package de.juniorjacki.utils;

import de.juniorjacki.utils.sql.query.QueryBuilder;
import de.juniorjacki.utils.sql.structure.Table;
import de.juniorjacki.utils.sql.type.DatabaseProperty;
import de.juniorjacki.utils.sql.type.DatabaseRecord;
import de.juniorjacki.utils.sql.type.DatabaseType;
import de.juniorjacki.utils.sql.type.defaultType.INTEGER;
import de.juniorjacki.utils.sql.type.defaultType.STRING;

import java.util.Arrays;
import java.util.List;

public class TestTable2 extends Table<TestTable2, TestTable2.Property, TestTable2.Test2> implements QueryBuilder<TestTable2, TestTable2.Property, TestTable2.Test2> {

    @Override
    public Class<Test2> getTableRecord() {
        return Test2.class;
    }

    public record Test2(String preName, Integer age) implements DatabaseRecord<Test2, Property> {

    }
    @Override
    public List<Property> getProperties() {
        return Arrays.asList(TestTable2.Property.values());
    }

    public enum Property implements DatabaseProperty {
        preName(true, STRING.INSTANCE),
        age(false,INTEGER.INSTANCE),
        ;

        private final boolean key;
        private final boolean unique;
        private final DatabaseType type;
        private final int extendedLength;


        Property(boolean key, DatabaseType type) {
            this.key = key;
            unique = false;
            this.type = type;
            this.extendedLength = 0;
        }

        Property(boolean key,boolean unique, DatabaseType type) {
            this.key = key;
            this.unique = unique;
            this.type = type;
            this.extendedLength = 0;
        }
        @Override public boolean isKey() {
            return key;
        }
        @Override public boolean isUnique() {
            return unique;
        }
        @Override public DatabaseType getType() {
            return type;
        }
        @Override public int extendLength() {
            return extendedLength;
        }
    }
}