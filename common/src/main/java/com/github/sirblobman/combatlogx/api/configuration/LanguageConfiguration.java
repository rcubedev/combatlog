package com.github.sirblobman.combatlogx.api.configuration;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.language.ILanguageConfiguration;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class LanguageConfiguration extends WrappedConfig implements ILanguageConfiguration {

    @Comment({"Should language debug messages be sent to the console?", "This option should be enabled if you are reporting an error or bug."})
    public boolean debugMode = false;

    @Comment({"Force all players to use the 'default_locale', even when they have set a different language in their client.",
            "This option also makes the 'console_locale' option useless."
    })
    public boolean enforceDefaultLocale = false;

    @Comment({"This is the default language that will be shown to players when their language is not detected.",
            "Player language can fail to detect if the file does not exist or if your server does not support locales."
    })
    public String defaultLocale = "en_us";

    @Comment("This is the language that will be used for messages sent to the server console.")
    public String consoleLocale = "en_us";

    @Comment({"Set this to true if you want language messages to include Text Placeholder API placeholders.",
            "The placeholder will be replaced relative to the player that the message is sent to.",
            "If the message is sent to a non-player, the placeholders will not be replaced."
    })
    public boolean usePlaceholderAPI = false;

    @Override
    public void reload() {
        CombatLogX.reload(CombatLogX.create(this));
    }

    @Override
    public boolean debugMode() {
        return this.debugMode;
    }

    @Override
    public boolean enforceDefaultLocale() {
        return this.enforceDefaultLocale;
    }

    @Override
    public String defaultLocale() {
        return this.defaultLocale;
    }

    @Override
    public String consoleLocale() {
        return this.consoleLocale;
    }

    @Override
    public boolean usePlaceholderAPI() {
        return this.usePlaceholderAPI;
    }
}
