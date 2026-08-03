package combatlogx.expansion.bossbar.configuration;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.language.ILanguage;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class BossBarLanguageFileConfiguration extends WrappedConfig implements ILanguage {

    @Comment({"Messages use the MiniMessage format in non-strict mode.",
            "More information about MiniMessage can be found here:",
            "https://docs.adventure.kyori.net/minimessage/format.html",
            "",
            "The format for decimal numbers.",
            "The United States uses the number and two decimal places"
    })
    public String decimalFormat = "0.00";

    @Comment("Shown on top of the screen while a player is in combat.")
    public String timer = "<bold><gold>Combat</gold> <gray>\u00BB</gray></bold> <white><red>{combatlogx:time_left}</red> seconds.</white>";

    @Comment("Shown on top of the screen for a brief period when combat ends.")
    public String ended = "<bold><gold>Combat</gold> <gray>\u00BB</gray></bold> <white>You are no longer in combat.</white>";

    @Override
    public String decimalFormat() {
        // todo js go to the equiv lang??
        return this.decimalFormat;
    }
}
