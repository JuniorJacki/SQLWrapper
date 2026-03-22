package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

/**
 * 65 kilobyte
 */
public class BOOLEAN extends DatabaseType {
    public BOOLEAN() {
        super(Boolean.class,
                List.of(boolean.class),
                ResultSet::getBoolean,
                (ps, idx, val) -> ps.setBoolean(idx, (Boolean) val),
                (extendedLength) -> "BOOLEAN");
    }

    public final static BOOLEAN INSTANCE = new BOOLEAN();
}
