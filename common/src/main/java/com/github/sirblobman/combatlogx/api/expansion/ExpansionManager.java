package com.github.sirblobman.combatlogx.api.expansion;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.expansion.ExpansionRegistryImpl;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public final class ExpansionManager {

    private final Map<String, Expansion> expansionMap = new LinkedHashMap<>();
    private final ICombatLogX mod;

    private State state = State.UNLOADED;

    public ExpansionManager(@NotNull ICombatLogX mod) {
        this.mod = mod;
    }

    public @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    // fixme tight coupling but can be fairly easily fixed: registry only used for freeze & loading from there
    public void loadExpansions(@NotNull ExpansionRegistryImpl registry) {
        if (this.state != State.UNLOADED) throw new IllegalStateException("Cannot load expansions from state: " + this.state);

        ICombatLogX mod = getCombatLogX();
        Logger logger = mod.getLogger();
        logger.info("Loading expansions...");

        registry.freeze(this::loadExpansion);

        this.state = State.LOADED;

        int size = getLoadedExpansions().size();
        logger.info("Successfully loaded {} expansion{}.", size, size == 1 ? "" : "s");
    }

    private void loadExpansion(ExpansionFactory factory) {
        ICombatLogX mod = getCombatLogX();
        Logger logger = mod.getLogger();
        Expansion expansion;
        try {
            expansion = factory.create(mod);
        } catch (Exception e) {
            logger.warn("An expansion failed to load because an error occurred.");
            logger.warn("If debug-mode is enabled, the full error will be displayed below.");
            mod.printDebug(e);
            return;
        }

        String id = expansion.getId();
        if (this.expansionMap.containsKey(id)) {
            // cannot keep a duplicate here
            //  todo maybe we could register with a suffix if that is even wanted
            logger.error("Duplicate expansion id '{}'.", id);
            return;
        }
        this.expansionMap.put(id, expansion);

        try {
            ExpansionMetadata description = expansion.getDescription();

            logger.info("Loading expansion '{}'...", description.getVersionedName());
            logger.info("");

            expansion.onLoad();
            expansion.setState(State.LOADED);
        } catch (Exception e) {
            expansion.setState(State.DISABLED);
            logger.error("An error occurred while loading expansion '{}'", id, e);
            // intentionally keep in expansionMap
        }
    }

    public void enableExpansions(@NotNull MinecraftServer server) {
        if (this.state != State.LOADED) throw new IllegalStateException("Cannot enable expansions from state: " + this.state);

        Logger logger = getCombatLogX().getLogger();
        logger.info("Enabling expansions...");
        this.state = State.ENABLING;

        List<Expansion> loadedExpansionList = getLoadedExpansions();
        if (loadedExpansionList.isEmpty()) {
            logger.info("There were no expansions to enable.");
            this.state = State.ENABLED;
            return;
        }

        for (Expansion expansion : loadedExpansionList) enableExpansion(expansion, server);
        this.state = State.ENABLED;

        List<Expansion> enabledExpansionList = getEnabledExpansions();
        int expansionListSize = enabledExpansionList.size();
        logger.info("Successfully enabled {} expansion{}.", expansionListSize, expansionListSize == 1 ? "" : "s");
    }

    private void enableExpansion(@NotNull Expansion expansion, @NotNull MinecraftServer server) {
        State state = expansion.getState();
        if (state != State.LOADED) return;

        Logger logger = getCombatLogX().getLogger();
        try {
            ExpansionMetadata description = expansion.getDescription();
            logger.info("Enabling expansion '{}'...", description.getVersionedName());
            logger.info("");

            expansion.setState(State.ENABLING);
            expansion.onEnable(server);
            if (expansion.getState() == State.ENABLING) expansion.setState(State.ENABLED);
        } catch (Exception e) {
            expansion.setState(State.DISABLED);
            logger.error("An error occurred while enabling an expansion", e);
        }
    }

    public void disableExpansions(@NotNull MinecraftServer server) {
        if (this.state != State.LOADED && this.state != State.ENABLING && this.state != State.ENABLED)
            throw new IllegalStateException("Cannot disable expansions from state: " + this.state);

        Logger logger = getCombatLogX().getLogger();
        logger.info("Disabling expansions...");

        this.state = State.DISABLING;

        List<Expansion> expansions = getAllExpansions();
        if (expansions.isEmpty()) {
            logger.info("There were no expansions to disable.");
        } else {
            for (Expansion expansion : expansions) disableExpansion(expansion, server);
        }

        this.expansionMap.clear();
        this.state = State.DISABLED;
        logger.info("Successfully disabled all expansions.");
    }

    private void disableExpansion(@NotNull Expansion expansion, @NotNull MinecraftServer server) {
        State state = expansion.getState();
        if (state == State.UNLOADED || state == State.DISABLED) return;

        Logger logger = getCombatLogX().getLogger();

        try {
            ExpansionMetadata description = expansion.getDescription();
            logger.info("Disabling expansion '{}'...", description.getVersionedName());

            expansion.setState(State.DISABLING);
            expansion.onDisable(server);
            expansion.setState(State.DISABLED);
        } catch (Exception e) {
            logger.error("An error occurred while disabling an expansion:", e);
        }
    }

    public void reloadConfigs() {
        List<Expansion> expansionList = getEnabledExpansions();
        expansionList.forEach(Expansion::reloadConfig);
    }

    public @NotNull State getState() {
        return this.state;
    }

    public @NotNull Optional<Expansion> getExpansion(String id) {
        if (id == null) return Optional.empty();

        Expansion expansion = this.expansionMap.get(id);
        return Optional.ofNullable(expansion);
    }

    // includes failed expansions; this is intentional
    public @NotNull List<Expansion> getAllExpansions() {
        Collection<Expansion> expansionCollection = this.expansionMap.values();
        return new ArrayList<>(expansionCollection);
    }

    public @NotNull List<Expansion> getLoadedExpansions() {
        return this.expansionMap.values().stream()
                .filter(expansion -> expansion.getState() == State.LOADED)
                .toList();
    }

    public @NotNull List<Expansion> getEnabledExpansions() {
        return this.expansionMap.values().stream()
                .filter(expansion -> expansion.getState() == State.ENABLED)
                .toList();
    }

    /*private ExpansionDescription fromModMetadata(ModMetadata meta) {
        return ExpansionDescription.builder(meta.getName(), meta.getId(), meta.getVersion().getFriendlyString())
                .withDescription(meta.getDescription())
                .withAuthors(meta.getAuthors().stream().map(Person::getName).toList())
                .build();
    }*/
}
