package com.github.sirblobman.combatlogx.api.configuration;

import java.util.List;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class CommandConfiguration extends WrappedConfig {

    @Comment({"All commands in this configuration have some prefixes available:",
            "[PLAYER] - Run the command as the player.",
            "[OP] - Run the command as the player, but with \"Server Operator\" permissions (NOT recommended).",
            "Any commands without a prefix will be executed by the server console.",
            "",
            "All Commands in this configuration have some placeholders available:",
            "Valid Placeholders:",
            "{combatlogx:player} - The name of the player that was tagged.",
            "{combatlogx:current_enemy_name} - The name of the enemy that tagged the player. The enemy can be unknown.",
            "",
            "",
            "This is a list of commands that will be executed whenever a player is tagged.",
            "Set this to an empty list to disable all tag commands.",
            "tag_command_list: []",
            "",
            "Use the list format to add commands.",
            "tag_command_list: [\"command1\", \"command2 with spaces\", 'tellraw {player} {\"text\":\"command 3 with internal quotes\"}']"
    })
    public List<String> tagCommandList = ValueList.create("");

    @Comment({"This is a list of commands that will be executed whenever a player is untagged.",
            "Valid Placeholders",
            "{combatlogx:player} - The name of the player that was untagged.",
            "{combatlogx:enemy} - The name of the last known enemy that tagged the player. The enemy can be unknown.",
            "",
            "Set this to an empty list to disable all untag commands.",
            "untag_command_list: []",
            "",
            "Use the list format to add commands.",
            "untag_command_list = [\"command1\", \"command2 with spaces\", 'tellraw {player} {\"text\":\"command 3 with internal quotes\"}']" //fixme
    })
    public List<String> untagCommandList = ValueList.create("");

    @Comment({"This is a list of commands that will be used to punish players that log out during combat.",
            "Valid Placeholders",
            "{combatlogx:player} - The name of the player that is being punished.",
            "",
            "Set this to an empty list to disable all punishment commands.",
            "punish_command_list: []",
            "",
            "Use the list format to add commands.",
            "punish_command_list = [\"command1\", \"command2 with spaces\", 'tellraw {player} {\"text\":\"command 3 with internal quotes\"}']" //fixme
    })
    public List<String> punishCommandList = ValueList.create("");
}
