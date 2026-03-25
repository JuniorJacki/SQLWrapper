package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

/**
 * 65 kilobyte
 */
public class BLOB extends DatabaseType {
    public BLOB() {
        super(byte[].class,
                ResultSet::getBytes,
                (ps, idx, val) -> ps.setBytes(idx, (byte[]) val),
                (extendedLength) -> "BLOB");
    }

    public final static BLOB INSTANCE = new BLOB();

}
