package com.github.sirblobman.combatlogx.api.expansion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ExpansionMetadata {
    private final String name;
    private final String id;
    private final String version;
    private final String description;
    private final List<String> authorList;

    ExpansionMetadata(@NotNull String name, @NotNull String id, @NotNull String version,
                      @NotNull String description, @NotNull List<String> authorList) {
        this.name = name;
        this.id = id;
        this.version = version;

        this.description = description;
        this.authorList = authorList;
    }

    public static Builder builder(@NotNull String name, @NotNull String id, @NotNull String version) {
        return new Builder(name, id, version);
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull String getId() {
        return this.id;
    }

    public @NotNull String getVersion() {
        return this.version;
    }

    public @NotNull String getDescription() {
        return this.description;
    }

    public @NotNull List<String> getAuthors() {
        return List.copyOf(this.authorList);
    }

    public @NotNull String getVersionedName() {
        return this.name + " v" + this.version;
    }

    public static final class Builder {
        private final String name;
        private final String id;
        private final String version;

        private String description;
        private List<String> authorList;

        Builder(@NotNull String name, @NotNull String id, @NotNull String version) {
            this.name = name;
            this.id = id;
            this.version = version;

            this.description = null;
            this.authorList = List.of();
        }

        public @NotNull Builder withDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        public @NotNull Builder withAuthors(@NotNull List<String> authorList) {
            this.authorList = new ArrayList<>(authorList);
            return this;
        }

        public @NotNull ExpansionMetadata build() {
            String description = (this.description == null ? "" : this.description);
            return new ExpansionMetadata(this.name, this.id, this.version, description, this.authorList);
        }
    }
}