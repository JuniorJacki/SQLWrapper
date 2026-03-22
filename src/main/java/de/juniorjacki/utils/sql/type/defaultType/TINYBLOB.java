package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

/**
 * 255 bytes
 */
public class TINYBLOB extends DatabaseType {
    public TINYBLOB() {
        super(byte[].class,
                List.of(),
                ResultSet::getBytes,
                (ps, idx, val) -> ps.setBytes(idx, (byte[]) val),
                (extendedLength) -> "TINYBLOB");
    }
    public final static TINYBLOB INSTANCE = new TINYBLOB();
}
