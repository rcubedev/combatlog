package combatlogx.expansion.bossbar;

import com.github.rcubedev.utils.config.serialization.ISerializableEnum;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

public enum BossBarColor implements ISerializableEnum<BossBarColor> {

    BLUE(BossBar.Color.BLUE),

    GREEN(BossBar.Color.GREEN),

    PINK(BossBar.Color.PINK),

    PURPLE(BossBar.Color.PURPLE),

    RED(BossBar.Color.RED),

    WHITE(BossBar.Color.WHITE),

    YELLOW(BossBar.Color.YELLOW);

    private final BossBar.Color color;

    BossBarColor(BossBar.Color color) {
        this.color = color;
    }

    public @NotNull BossBar.Color getColor() {
        return this.color;
    }
}
