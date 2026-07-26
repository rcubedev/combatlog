package com.github.sirblobman.combatlogx.expansion;

import com.github.sirblobman.combatlogx.api.expansion.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;import java.util.concurrent.locks.ReadWriteLock;import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public final class ExpansionRegistryImpl implements ExpansionRegistry {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    // null when frozen
    private @Nullable Queue<ExpansionFactory> registrationQueue = new ConcurrentLinkedQueue<>();

    private ExpansionRegistryImpl() {}

    public static ExpansionRegistryImpl getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerExpansion(ExpansionFactory factory) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            if (registrationQueue == null) throw new IllegalStateException("Cannot register expansion after freeze");
            registrationQueue.add(factory);
        } finally {
            readLock.unlock();
        }
    }

    public void registerExpansions(List<ExpansionFactory> factories) {
        Queue<ExpansionFactory> queue = this.registrationQueue;
        assert queue != null : "Cannot bulk register expansions after freeze";
        queue.addAll(factories);
    }

    // called once synch
    public void freeze(@NotNull Consumer<ExpansionFactory> callback) {
        Queue<ExpansionFactory> queue;

        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (registrationQueue == null) throw new IllegalStateException("Already frozen");

            queue = registrationQueue;
            registrationQueue = null;
        } finally {
            writeLock.unlock();
        }

        drain(queue, callback);
    }

    private void drain(Queue<ExpansionFactory> queue, Consumer<ExpansionFactory> callback) {
        ExpansionFactory factory;
        while ((factory = queue.poll()) != null) callback.accept(factory);
    }

    private static class Holder {
        private static final ExpansionRegistryImpl INSTANCE = new ExpansionRegistryImpl();
    }
}
