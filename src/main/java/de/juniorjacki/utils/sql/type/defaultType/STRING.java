package de.juniorjacki.utils.sql.type.defaultType;

import de.juniorjacki.utils.sql.structure.InterDefinitions;
import de.juniorjacki.utils.sql.type.DatabaseType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;

public class STRING extends DatabaseType {
    public STRING() {
        super(String.class, List.of(CharSequence.class),
                ResultSet::getString,
                (ps, idx, val) -> ps.setString(idx, (String) val),
                (extendedLength) -> extendedLength == 0 ? "VARCHAR(255)" : "VARCHAR(" + extendedLength + ")");
    }
    public final static STRING INSTANCE = new STRING();
}
