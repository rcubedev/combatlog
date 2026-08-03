package com.github.rcubedev.example.task.api.info;

import com.github.rcubedev.example.task.api.TaskOwner;
import com.github.rcubedev.example.task.api.TaskType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class EntityTaskInfo<E extends Entity> extends TaskInfo {
    private final WeakReference<E> entityReference;

    public EntityTaskInfo(@NotNull TaskOwner owner, @NotNull TaskType type, @NotNull E entity) {
        super(owner, type);
        this.entityReference = new WeakReference<>(entity);
    }

    protected final @NotNull WeakReference<E> getEntityReference() {
        return this.entityReference;
    }

    public final @Nullable E getEntity() {
        WeakReference<E> entityReference = this.getEntityReference();
        return entityReference.get();
    }
}
