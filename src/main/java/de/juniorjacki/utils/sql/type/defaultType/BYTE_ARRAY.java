package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.ResultSet;
import java.util.List;

public class BYTE_ARRAY extends DatabaseType {
    public BYTE_ARRAY() {
        super(byte[].class,
                ResultSet::getBytes,
                (ps, idx, val) -> ps.setBytes(idx, (byte[]) val),
                (extendedLength) -> extendedLength == 0 ? "BINARY(64)" : "BINARY(" + extendedLength + ")");
    }

    public final static BYTE_ARRAY INSTANCE = new BYTE_ARRAY();
}
