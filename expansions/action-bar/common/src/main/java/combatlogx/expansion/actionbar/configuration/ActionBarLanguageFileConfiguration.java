package combatlogx.expansion.actionbar.configuration;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.language.ILanguage;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class ActionBarLanguageFileConfiguration extends WrappedConfig implements ILanguage {

    @Comment({"Messages use the MiniMessage format in non-strict mode.",
            "More information about MiniMessage can be found here:",
            "https://docs.adventure.kyori.net/minimessage/format.html",
            "",
            "The format for decimal numbers.",
            "The United States uses the number and two decimal places"
    })
    public String decimalFormat = "0.00";

    @Comment("The prefix for CombatLogX Action Bar that is shown in front of all messages.")
    public String prefix = "<white><bold>[<gold>CombatLogX</gold>]</bold></white>";

    @Comment("Shown above the hotbar while a player is in combat.")
    public String timer = "<bold><gold>Combat</gold> <gray>\u00BB</gray></bold> <white>{bars} <red>{combatlogx:time_left}</red> seconds.</white>";

    @Comment("Shown above the hotbar for a brief period when combat ends.")
    public String ended = "<bold><gold>Combat</gold> <gray>\u00BB</gray></bold> <white>You are no longer in combat.</white>";

    @Override
    public String decimalFormat() {
        // todo js go to the equiv lang??
        return this.decimalFormat;
    }
}
