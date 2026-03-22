package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

/**
 * 4 gigabyte
 */
public class LONGBLOB extends DatabaseType {
    public LONGBLOB() {
        super(byte[].class,
                List.of(),
                ResultSet::getBytes,
                (ps, idx, val) -> ps.setBytes(idx, (byte[]) val),
                (extendedLength) -> "LONGBLOB");
    }
    public final static LONGBLOB INSTANCE = new LONGBLOB();
}
