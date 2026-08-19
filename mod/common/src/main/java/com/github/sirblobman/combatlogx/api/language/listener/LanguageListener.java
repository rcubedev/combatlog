package com.github.sirblobman.combatlogx.api.language.listener;

import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.utils.event.api.Priority;
import com.github.rcubedev.utils.event.api.annotation.SubscribeEvent;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerLocaleChangeEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerQuitEvent;
import com.github.sirblobman.combatlogx.api.language.ILanguage;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import folk.sisby.kaleido.api.WrappedConfig;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public final class LanguageListener<T extends WrappedConfig & ILanguage> {
    private final ICombatLogX mod;
    private final LanguageManager<T> languageManager;
    private final TaskScheduler scheduler;

    public LanguageListener(@NotNull ICombatLogX mod, @NotNull LanguageManager<T> languageManager) {
        this.mod = mod;
        this.scheduler = mod.getScheduler();
        this.languageManager = languageManager;
    }

    private @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    private @NotNull TaskScheduler getTaskScheduler() {
        return this.scheduler;
    }

    private @NotNull LanguageManager<T> getLanguageManager() {
        return this.languageManager;
    }

    /*@SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        ICombatLogX mod = getCombatLogX();
        ServerPlayer player = e.getPlayer();
        LanguageManager languageManager = getLanguageManager();

        UpdateLocaleTask task = new UpdateLocaleTask(mod, languageManager, 1L);

        TaskScheduler taskScheduler = getTaskScheduler();
        taskScheduler.schedulePlayer(task);
    }*/

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onSwitchLocale(PlayerLocaleChangeEvent e) {
        ServerPlayer player = e.getPlayer();
        String locale = e.getLocale();

        LanguageManager<T> languageManager = getLanguageManager();
        if (locale == null) {
            languageManager.removeLocale(player);
            return;
        }
        languageManager.setLocale(player, locale);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        ServerPlayer player = e.getPlayer();
        LanguageManager<T> languageManager = getLanguageManager();
        languageManager.removeLocale(player);
    }
}
