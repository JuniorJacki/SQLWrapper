package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

/**
 * 4 gigabyte
 */
public class DOUBLE extends DatabaseType {
    public DOUBLE() {
        super(Double.class,
                ResultSet::getDouble,
                (ps, idx, val) -> ps.setDouble(idx, (Double) val),
                (extendedLength) -> "DOUBLE");
    }
    public final static DOUBLE INSTANCE = new DOUBLE();
}
