package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

public class MEDIUMSTRING extends DatabaseType {
    public MEDIUMSTRING() {
        super(String.class,
                ResultSet::getString,
                (ps, idx, val) -> ps.setString(idx, (String) val),
                (extendedLength) -> "MEDIUMTEXT("+extendedLength+")");
    }
    public final static MEDIUMSTRING INSTANCE = new MEDIUMSTRING();
}
