package de.juniorjacki.utils.sql.routine;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RoutineRunner {

    private final ConcurrentHashMap<Routine, ScheduledFuture<?>> routines = new ConcurrentHashMap<>();
    private volatile ScheduledExecutorService executor;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);


    public void registerNewRoutine(Routine routine) {
        if (routine == null) {
            return;
        }
        if (isRunning.get()) {
            scheduleSingleRoutine(routine);
        } else {
            routines.putIfAbsent(routine, null);
        }
    }

    public void unregisterRoutine(Routine routine) {
        if (routine == null) {
            return;
        }
        ScheduledFuture<?> future = routines.remove(routine);
        if (future != null) {
            future.cancel(false);
            System.out.println("Cancelled and unregistered routine: " + routine);
        } else {
            System.out.println("Unregistered (was not scheduled yet): "+ routine);
        }
    }

    /**
     * Starts (or restarts) execution of all currently registered routines.
     */
    public void startRoutineExecution() {
        if (isRunning.compareAndSet(false, true)) {
            executor = createFreshExecutor();
            System.out.println("Starting SQL Routines execution.");
        } else {
            System.out.println("Restarting SQL Routines execution.");
            stopRoutineExecution();
            executor = createFreshExecutor();
        }
        routines.forEach((routine, oldFuture) -> {
            scheduleSingleRoutine(routine);
        });
    }

    private void scheduleSingleRoutine(Routine routine) {
        if (executor == null || executor.isShutdown()) {
            return;
        }
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(
                () -> {
                    try {
                        routine.execute();
                    } catch (Exception e) {
                        routineError(routine, e);
                    }
                },
                routine.delay(),
                routine.delay(),
                TimeUnit.MILLISECONDS
        );
        routines.put(routine, future);
    }

    public void stopRoutineExecution() {
        if (executor == null) {
            return;
        }

        ScheduledExecutorService old = executor;
        executor = null;
        isRunning.set(false);

        old.shutdown();
        try {
            if (!old.awaitTermination(5, TimeUnit.SECONDS)) {
                old.shutdownNow();
                System.out.println("Executor did not terminate gracefully");
            }
        } catch (InterruptedException e) {
            old.shutdownNow();
            Thread.currentThread().interrupt();
        }
        routines.replaceAll((routine, future) -> null);
    }

    private ScheduledExecutorService createFreshExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    private void routineError(Routine routine, Exception e) {
        System.out.println("Error in routine " + routine + " " + e.getMessage() + " " + e);
    }

    public int getRegisteredCount() {
        return routines.size();
    }

}
