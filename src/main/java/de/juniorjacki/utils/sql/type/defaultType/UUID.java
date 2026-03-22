package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

public class UUID extends DatabaseType {
    public UUID() {
        super(String.class,
                List.of(CharSequence.class),
                ResultSet::getString,
                (ps, idx, val) -> ps.setString(idx, (String) val),
                (extendedLength) -> "LONGTEXT");
    }
    public final static UUID INSTANCE = new UUID();
}
