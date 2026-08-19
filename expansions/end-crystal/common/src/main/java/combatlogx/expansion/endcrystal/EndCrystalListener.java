package combatlogx.expansion.endcrystal;

import com.github.rcubedev.utils.event.api.Priority;
import com.github.rcubedev.utils.event.api.annotation.SubscribeEvent;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.entity.EntityPlaceEvent;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import com.github.sirblobman.combatlogx.api.expansion.Expansion;
import com.github.sirblobman.combatlogx.api.manager.ICrystalManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.jetbrains.annotations.NotNull;

public final class EndCrystalListener /*extends ExpansionListener*/ {

    public final Expansion expansion;

    public EndCrystalListener(@NotNull Expansion expansion) {
        this.expansion = expansion;
    }

    public ICombatLogX getCombatLogX() {
        return expansion.getCombatLogX();
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onPlace(EntityPlaceEvent e) {
        ICombatLogX mod = getCombatLogX();
        MainConfiguration configuration = mod.getConfiguration();
        if (!configuration.linkEndCrystal) return;

        if (!(e.getEntity() instanceof EndCrystal crystal)) return;

        ServerPlayer player = e.getPlayer();
        if (player == null) return;

        ICrystalManager crystalManager = mod.getCrystalManager();
        crystalManager.setPlacer(crystal, player);
    }
}
