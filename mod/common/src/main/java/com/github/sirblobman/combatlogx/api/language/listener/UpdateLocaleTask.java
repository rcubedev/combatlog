package com.github.sirblobman.combatlogx.api.language.listener;

import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractPlayerTaskDetails;
import com.github.rcubedev.example.task.api.info.TaskInfo;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.mixin.ServerPlayerAccessor;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class UpdateLocaleTask extends AbstractPlayerTaskDetails {
    private final LanguageManager languageManager;

    public UpdateLocaleTask(@NotNull ICombatLogX mod, @NotNull LanguageManager languageManager, long delay) {
        super(new TaskInfo(mod, TaskType.START_TICK).setDelay(delay));
        this.languageManager = languageManager;
    }

    private @NotNull LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    @Override
    public void accept(@NotNull ServerPlayer player) {
        String playerLocale = ((ServerPlayerAccessor) player).al$language();
        LanguageManager languageManager = getLanguageManager();
        languageManager.setLocale(player, playerLocale);
    }
}
