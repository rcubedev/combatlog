package com.github.sirblobman.combatlogx.api.configuration;

import java.util.List;

import com.github.sirblobman.combatlogx.api.object.KillTime;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import org.jetbrains.annotations.NotNull;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class PunishConfiguration extends WrappedConfig {

    @Comment({"Should CombatLogX punish players when they log out during combat?",
            "It is not recommended to disable this option unless you are testing something."
    })
    public boolean onDisconnect = true;

    @Comment({"Should CombatLogX punish players when they are kicked from the game?",
            "Getting kicked from the game is usually not the fault of the player."
    })
    public boolean onKick = false;

    @Comment({"Should CombatLogX punish players when their timer expires?",
            "This option is not recommended unless you are testing something."
    })
    public boolean onExpire = false;

    @Comment({"Which kick reasons should not punish players?",
            "This option is only used when the 'on_kick' option is set to true."
    })
    public List<String> kickIgnoreList = ValueList.create("", "troll", "kicked by admin");

    @Comment("Should the kick ignore list be converted into a kick requirement list?")
    public boolean kickIgnoreListInverted = false;

    @Comment({"When should CombatLogX kill the player?",
            "QUIT: CombatLogX will kill the player the moment they log out.",
            "JOIN: CombatLogX will kill the player as soon as they log back in.",
            "KEEP_ONLINE: CombatLogX won't kill the player, instead keeping it online.",
            "NEVER: CombatLogX will never kill the player."
    })
    public KillTime killTime = KillTime.QUIT;

    @Comment({"This is a list of custom death messages that CombatLogX will use when killing a player.",
            "A message will be selected from this list randomly.",
            "",
            "Valid Placeholders:",
            "{combatlogx:player} - The name of the player that died.",
            "{combatlogx:current_enemy_name} - The name of the last enemy that tagged the player. The enemy can be unknown.",
            "",
            "If you don't want any custom messages, set this to an empty list",
            "custom_death_message_list: []"
    })
    public List<String> customDeathMessageList = ValueList.create("",
            "{combatlogx:player} was killed for logging out during combat",
            "{combatlogx:player} instantly died due to logging out during combat",
            "{combatlogx:player} was scared to death by {combatlogx:current_enemy_name}");

    @Comment("Should CombatLogX keep track of the amount of times each player was punished?")
    public boolean enablePunishmentCounter = true;

    public boolean isKickIgnored(@NotNull String reason) {
        boolean ignore = isInIgnoreList(reason);
        boolean inverted = this.kickIgnoreListInverted;
        return inverted != ignore;
    }

    private boolean isInIgnoreList(@NotNull String reason) {
        List<String> kickIgnoreList = this.kickIgnoreList;
        if (kickIgnoreList.contains("*")) return true;

        for (String part : kickIgnoreList) {
            if (reason.contains(part)) return true;
        }
        return false;
    }
}
