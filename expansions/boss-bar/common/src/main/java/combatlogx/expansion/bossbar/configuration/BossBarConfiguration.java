package combatlogx.expansion.bossbar.configuration;

import combatlogx.expansion.bossbar.BossBarColor;
import combatlogx.expansion.bossbar.BossBarOverlay;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class BossBarConfiguration extends WrappedConfig {

    @Comment({"Messages use the MiniMessage format in non-strict mode.",
            "More information about MiniMessage can be found here:",
            "https://docs.adventure.kyori.net/minimessage/format.html",
            "",
            "This option is here to quickly toggle this expansion on a live server that must stay online.",
            "We recommend that you remove the Boss Bar expansion jar if you will not use the feature."
    })
    public boolean enabled = true;

    @Comment("The color of the boss bar")
    public BossBarColor color = BossBarColor.YELLOW;

    @Comment("The style of the boss bar")
    public BossBarOverlay style = BossBarOverlay.PROGRESS;

    @Comment("The amount of symbols for the {bars} placeholder.")
    public int scale = 15;

    @Comment({"Left Symbol is the symbol used for the left part of the {bars} placeholder",
            "Unicode is supported, but must be converted.",
            "Example: \"<green>\u00A7</green>\""
    })
    public String leftSymbol = "<green>|</green>";

    @Comment({"Right Symbol is the symbol used for the right part of the {bars} placeholder",
            "Unicode is supported, but must be converted.",
            "Example: \"<red>\u00A7</red>\""
    })
    public String rightSymbol = "<red>|</red>";

    // todo cache? will need invalidation on reload
    public @NotNull Component getLeftSymbol(MiniMessage miniMessage) {
        return miniMessage.deserialize(this.leftSymbol);
    }

    public @NotNull Component getRightSymbol(MiniMessage miniMessage) {
        return miniMessage.deserialize(this.rightSymbol);
    }
}
