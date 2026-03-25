package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

public class INTEGER extends DatabaseType {
    public INTEGER() {
        super(Integer.class,
                ResultSet::getInt,
                (ps, idx, val) -> ps.setInt(idx, (Integer) val),
                (extendedLength) -> "INT");
    }
    public final static INTEGER INSTANCE = new INTEGER();
}
