package combatlogx.expansion.bossbar;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.expansion.Expansion;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionInitializer;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionMetadata;
import org.jetbrains.annotations.NotNull;

public final class BossBarExpansionInitializer implements ExpansionInitializer {

    @Override
    public @NotNull Expansion onInitializeExpansion(@NotNull ICombatLogX api, @NotNull ExpansionMetadata description) {
        return new BossBarExpansion(api, description);
    }
}
