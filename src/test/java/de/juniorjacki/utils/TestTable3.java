package de.juniorjacki.utils;

import de.juniorjacki.utils.sql.query.QueryBuilder;
import de.juniorjacki.utils.sql.structure.Table;
import de.juniorjacki.utils.sql.type.DatabaseProperty;
import de.juniorjacki.utils.sql.type.DatabaseRecord;
import de.juniorjacki.utils.sql.type.DatabaseType;
import de.juniorjacki.utils.sql.type.defaultType.INTEGER;
import de.juniorjacki.utils.sql.type.defaultType.UUID;

import java.util.Arrays;
import java.util.List;

public class TestTable3 extends Table<TestTable3, TestTable3.Property, TestTable3.Test3> implements QueryBuilder<TestTable3, TestTable3.Property, TestTable3.Test3> {

    @Override
    public Class<Test3> getTableRecord() {
        return Test3.class;
    }

    public record Test3(java.util.UUID uuid, Integer age) implements DatabaseRecord<Test3, Property> {

    }
    @Override
    public List<Property> getProperties() {
        return Arrays.asList(TestTable3.Property.values());
    }

    public enum Property implements DatabaseProperty {
        uuid(false, UUID.INSTANCE),
        age(true,INTEGER.INSTANCE),
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