package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.type.DatabaseType;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.util.List;

public class UUID extends DatabaseType {
    public UUID() {
        super(java.util.UUID.class,
                (rs, col) -> {
                    byte[] bytes = rs.getBytes(col);
                    if (bytes != null) {
                        return convertBytesToUUID(rs.getBytes(col));
                    }
                    return null;
                },
                (ps, idx, val) -> ps.setBytes(idx, convertUUIDToBytes((java.util.UUID) val)),
                (extendedLength) -> "LONGTEXT");
    }
    public final static UUID INSTANCE = new UUID();

    public static byte[] convertUUIDToBytes(java.util.UUID uuid) {
        if (uuid == null) return null;
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static java.util.UUID convertBytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new java.util.UUID(high, low);
    }



}
