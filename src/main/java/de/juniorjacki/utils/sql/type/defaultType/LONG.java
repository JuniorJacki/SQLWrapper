package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

public class LONG extends DatabaseType {
    public LONG() {
        super(Long.class,
                List.of(long.class),
                ResultSet::getLong,
                (ps, idx, val) -> ps.setLong(idx, (Long) val),
                (extendedLength) -> "BIGINT");
    }
    public final static LONG INSTANCE = new LONG();
}
