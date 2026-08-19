package combatlogx.expansion.endcrystal;

import com.github.rcubedev.utils.event.api.Identity;
import com.github.rcubedev.utils.event.api.buses.MainBus;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.expansion.Expansion;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionMetadata;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public final class EndCrystalExpansion extends Expansion {

    public static final String MOD_ID = "clx_endcrystal";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public EndCrystalExpansion(@NotNull ICombatLogX mod, @NotNull ExpansionMetadata metadata) {
        super(mod, metadata);
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void onLoad() {}

    @Override
    public void onEnable(@NotNull MinecraftServer server) {
        reloadConfig();
        MainBus.get().register(new EndCrystalListener(this), Identity.of(MethodHandles.lookup()));
    }

    @Override
    public void onDisable(@NotNull MinecraftServer server) {}

    @Override
    public void reloadConfig() {}
}
