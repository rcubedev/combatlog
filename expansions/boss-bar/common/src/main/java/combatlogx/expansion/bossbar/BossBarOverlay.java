package combatlogx.expansion.bossbar;

import com.github.rcubedev.example.ISerializableEnum;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

public enum BossBarOverlay implements ISerializableEnum<BossBarOverlay> {

    PROGRESS(BossBar.Overlay.PROGRESS),

    NOTCHED_6(BossBar.Overlay.NOTCHED_6),

    NOTCHED_10(BossBar.Overlay.NOTCHED_10),

    NOTCHED_12(BossBar.Overlay.NOTCHED_12),

    NOTCHED_20(BossBar.Overlay.NOTCHED_20);

    private final BossBar.Overlay overlay;

    BossBarOverlay(BossBar.Overlay overlay) {
        this.overlay = overlay;
    }

    public @NotNull BossBar.Overlay getOverlay() {
        return this.overlay;
    }
}
