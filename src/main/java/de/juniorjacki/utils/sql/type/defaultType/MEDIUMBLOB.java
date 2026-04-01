package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

/**
 * 16 megabyte
 */
public class MEDIUMBLOB extends DatabaseType {
    public MEDIUMBLOB() {
        super(byte[].class,
                ResultSet::getBytes,
                (ps, idx, val) -> ps.setBytes(idx, (byte[]) val),
                (extendedLength) -> "MEDIUMBLOB("+extendedLength+")");
    }
    public final static MEDIUMBLOB INSTANCE = new MEDIUMBLOB();
}
