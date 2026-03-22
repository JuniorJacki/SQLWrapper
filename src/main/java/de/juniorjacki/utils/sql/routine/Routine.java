package de.juniorjacki.utils.sql.routine;

public interface Routine {
    void execute();
    String description();
    long delay();
}
