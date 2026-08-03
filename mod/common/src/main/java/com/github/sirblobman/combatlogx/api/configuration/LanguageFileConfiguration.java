package com.github.sirblobman.combatlogx.api.configuration;

import com.github.sirblobman.combatlogx.api.language.ILanguage;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class LanguageFileConfiguration extends WrappedConfig implements ILanguage {

    @Comment({"Messages use the MiniMessage format in non-strict mode.",
            "More information about MiniMessage can be found here:",
            "https://docs.adventure.kyori.net/minimessage/format.html",
            "",
            "The format for decimal numbers.",
            "The United States uses the number and two decimal places"
    })
    public String decimalFormat = "0.00";

    @Comment("The prefix for CombatLogX that is shown in front of all messages.")
    public String prefix = "<white><bold>[<gold>CombatLogX</gold>]</bold></white>";

    // fixme if MainConfiguration broadcast readded, readd this.
    // public BroadcastSection broadcast = new BroadcastSection();
    // public static class BroadcastSection implements Section {
    //     public String onLoad = "<green><bold>CombatLogX was loaded successfully.</bold></green>";
    //     public String onEnable = "<green><bold>CombatLogX was enabled successfully.</bold></green>";
    //     public String onDisable = "<red><bold>CombatLogX was disabled successfully.</bold></red>";
    // }
    // // stopped using map as users may remove entries & add useless ones.
    // public Map<String, String> broadcast = ValueMap.builder("")
    //         .put("on-load", "<green><bold>CombatLogX was loaded successfully.</bold></green>")
    //         .put("on-enable", "<green><bold>CombatLogX was enabled successfully.</bold></green>")
    //         .put("on-disable", "<red><bold>CombatLogX was disabled successfully.</bold></red>")
    //         .build();

    public PlaceholderSection placeholder = new PlaceholderSection();
    public static class PlaceholderSection implements Section {
        @Comment({"This text is used for the {combatlogx:time_left}",
                "This allows server configurations to change the display value of the zero to something like \"Not in combat\""
        })
        public String timeLeftZero = "0";

        @Comment({"This text is used when a player does not have an enemy.",
                "This can happen when players are tagged by a custom expansion or the tag command."
        })
        public String unknownEnemy = "Unknown";

        public StatusPlaceholderSection status = new StatusPlaceholderSection();
        public static class StatusPlaceholderSection implements Section {
            @Comment("Shown when the player is in combat.")
            public String fighting = "<red>Fighting</red>";

            public String inCombat = "<green>Yes</green>";

            @Comment("Shown when the player is not in combat")
            public String idle = "<green>Idle</green>";

            public String notInCombat = "<red>No</red>";
        }

        @Comment("These placeholders are shown when a player changes a value such as whether or not their bossbar is enabled.")
        public TogglePlaceholderSection toggle = new TogglePlaceholderSection();
        public static class TogglePlaceholderSection implements Section {
            public String enabled = "<green>ON</green>";

            public String disabled = "<red>OFF</red>";
        }

        public PvpStatusPlaceholderSection pvpStatus = new PvpStatusPlaceholderSection();
        public static class PvpStatusPlaceholderSection implements Section {
            public String enabled = "<green>ON</green>";

            public String disabled = "<red>OFF</red>";
        }
    }

    // todo maybe reimpl ability to change location of the msgs.
    public CombatTimerSection combatTimer = new CombatTimerSection();
    public static class CombatTimerSection implements Section {
        public String expire = "<green>You are no longer in combat.</green>";

        public String enemyDeath = "<green>You are no longer in combat because your enemy died.</green>";

        public String selfDeath = "<green>You are no longer in combat because you died.</green>";
    }

    public ErrorSection error = new ErrorSection();
    public static class ErrorSection implements Section {
        @Comment("Shown when the console tries to execute a command made for players.")
        public String playerOnly = "<red>Only players can execute this command</red>";

        @Comment("Shown when a player tries to execute a command made for the server console.")
        public String consoleOnly = "<red>This command can only be executed in the server console.</red>";

        @Comment("Shown when a command that requires a player has invalid input.")
        public String invalidTarget = "<red><gray>{target}</gray> is not online or does not exist.</red>";

        @Comment("Shown when a command that requires a number has invalid input.")
        public String invalidInteger = "<red><gray>{value}</gray> is not a valid integer.</red>";

        @Comment("Shown when a player does not have access to something that requires a permission.")
        public String noPermission = "<red>Missing Permission: <gray>{permission}</gray></red>";

        @Comment("Shown when a player executes a command in a world that is disabled in the configuration.")
        public String disabledWorld = "<red>That command is not available in this dimension.</red>";

        @Comment("Shown when a command requires a player in combat but the target player is not in combat.")
        public String targetNotInCombat = "<red><gray>{target}</gray> is not in combat.</red>";

        @Comment("Shown when a player executes a command that requires them to be in combat.")
        public String selfNotInCombat = "<red>You are not in combat.</red>";

        @Comment("Shown when a command that requires an expansion has invalid input.")
        public String unknownExpansion = "<red><gray>{target}</gray> is not an expansion or is not installed.</red>";

        public String forgiveNotEnemy = "<red><gray>{target}</gray> is not one of your enemies.</red>";
        public String enemyNotForgiving = "<red>That enemy is not in the mood to forgive you.</red>";
    }

    public CommandSection command = new CommandSection();
    public static class CommandSection implements Section {
        public CombatLogXCommandSection combatlogx = new CombatLogXCommandSection();
        public static class CombatLogXCommandSection implements Section {
            @Comment("Shown as the command output for '/combatlogx help'.")
            public List<String> helpMessageList = ValueList.create("",
                    "",
                    "<gold><bold>CombatLogX Command Help:</bold></gold>",
                    "  <white><bold>/combatlogx help</bold></white><gray>: View this help page.</gray>",
                    "  <white><bold>/combatlogx reload</bold></white><gray>: Reload the config.yml, language.yml, and all expansion config files.</gray>",
                    "  <white><bold>/combatlogx about \\<expansion></bold></white><gray>: Check information about an expansion.</gray>",
                    "  <white><bold>/combatlogx tag \\<player> [seconds]</bold></white><gray>: Force a player into combat.</gray>",
                    "  <white><bold>/combatlogx toggle bossbar/actionbar/scoreboard</bold></white><gray>: Enable or disable a notification type.</gray>",
                    "  <white><bold>/combatlogx untag \\<player></bold></white><gray>: Force a player out of combat.</gray>",
                    "  <white><bold>/combatlogx version</bold></white><gray>: Check your version of CombatLogX.</gray>",
                    "  <white><bold>/combatlogx forgive request \\<player></bold></white><gray>: Send a request to an enemy to remove their tag from you.</gray>",
                    "  <white><bold>/combatlogx forgive accept \\<player></bold></white><gray>: Allow an enemy's request to escape from combat.</gray>",
                    "  <white><bold>/combatlogx forgive reject \\<player></bold></white><gray>: Ignore an enemy's request to escape from combat.</gray>",
                    "  <white><bold>/combatlogx forgive toggle</bold></white><gray>: Enable or disable requests for stopping combat.</gray>",
                    ""
            );

            @Comment("Shown as the command output for '/combatlogx reload' when reloading is successful.")
            public List<String> reloadSuccess = ValueList.create("",
                    "<green>Successfully reloaded all configuration files from CombatLogX.</green>",
                    "<green>Successfully reloaded all language files from CombatLogX.</green>",
                    "<green>Successfully reloaded all configuration files from CombatLogX expansions.</green>"
            );

            @Comment("Shown as the command output for '/combatlogx reload' when reloading fails")
            public List<String> reloadFailure = ValueList.create("",
                    "<red>An error occurred while reloading the configuration.</red>",
                    "<red>Please check your server log and fix the broken file.</red>"
            );

            @Comment("Shown as the command output for '/combatlogx tag <player>' when a player is tagged successfully.")
            public String tagPlayer = "<green>Successfully forced player <gray>{target}</gray> into combat.</green>";

            @Comment("Shown as the command output for '/combatlogx tag <player>' when the plugin failed to tag a player.")
            public String tagFailure = "<red><gray>{target}</gray> could not be placed into combat. (Maybe they have a bypass?)</red>";

            @Comment("Shown as the command output for '/combatlogx untag <player>'.")
            public String untagPlayer = "<green>Successfully forced player <gray>{target}</gray> out of combat.</green>";

            @Comment("Shown as the command output for '/combatlogx toggle bossbar'.")
            public String toggleBossbar = "<gray><bold>Boss Bar:</bold></gray> {status}";

            @Comment("Shown as the command output for '/combatlogx toggle actionbar'.")
            public String toggleActionbar = "<gray><bold>Action Bar:</bold></gray> {status}";

            @Comment("Shown as the command output for '/combatlogx toggle scoreboard'.")
            public String toggleScoreboard = "<gray><bold>Scoreboard:</bold></gray> {status}";

            @Comment("Shown as the command output for '/combatlogx about <expansion>'.")
            public List<String> expansionInformation = ValueList.create(
                    "",
                    "<white><bold>Expansion Information for</bold> <green>{name}</green><bold>:</bold></white>",
                    "<white><bold>Display Name:</bold></white> <gray>{prefix}</gray>",
                    "<white><bold>Version:</bold></white> <gray>{version}</gray>",
                    "<white><bold>State:</bold></white> <gray>{state}</gray>",
                    "",
                    "<white><bold>Description:</bold></white> <gray>{description}</gray>",
                    "<white><bold>Website:</bold></white> <gray>{website}</gray>",
                    "<white><bold>Authors:</bold></white> <gray>{authors}</gray>"
            );

            public ForgiveCombatLogXCommandSection forgive = new ForgiveCombatLogXCommandSection();
            public static class ForgiveCombatLogXCommandSection implements Section {
                public String toggleDisable = "<green>You can no longer receive requests for forgiveness.</green>";
                public String toggleEnable = "<green>You can now receive forgiveness requests.</green>";
                public String requestSent = "<green>You sent a forgive request to <gray>{target}</gray>.</green>";

                public List<String> requestReceive = ValueList.create(
                        "<green><gray>{player}</gray> sent a forgive request to you.</green>",
                        "<green>Type <click:run_command:/combatlogx forgive accept><gray>/clx forgive accept</gray></click> to accept or.</green>",
                        "<green><click:run_command:/combatlogx forgive reject><gray>/clx forgive reject</gray></click> to deny.</green>"
                );
            }
        }

        public CombatTimerCommandSection combatTimer = new CombatTimerCommandSection();
        public static class CombatTimerCommandSection implements Section {
            @Comment("Shown as the command output for '/combat-timer'.")
            public String timeLeftSelf = "<green>You have <gray>{time_left}</gray> seconds remaining.</green>";

            @Comment("Shown as the command output for '/combat-timer <player>'.")
            public String timeLeftOther = "<green><gray>{target}</gray> has <gray>{time_left} seconds</gray> remaining.</green>";
        }
    }

    @Comment("These messages are shown a player is tagged into combat.") // todo in future if bring back changing loc add that
    public TaggedSection tagged = new TaggedSection();
    public static class TaggedSection implements Section {

        public TaggedTypeSection unknown = new TaggedTypeSection(
                "<red>You are now in combat with <white>{enemy}</white> for an unknown reason. Do not log out!</red>",
                "<red>You are now in combat with a(n) <white>{enemy}</white> for an unknown reason. Do not log out!</red>",
                "<red>You are now in combat with a(n) <white>{mob_type}</white> for an unknown reason. Do not log out!</red>",
                "<red>You are now in combat due to taking damage. Do not log out!</red>",
                "<red>You were placed into combat without a reason. Do not log out.</red>"
        );

        public TaggedTypeSection attacked = new TaggedTypeSection(
                "<red>You are being attacked by <white>{enemy}</white>. Do not log out!</red>",
                "<red>You are being attacked by a(n) <white>{mob_type}</white>. Do not log out!</red>",
                "<red>You are being attacked by a(n) <white>{enemy}</white>. Do not log out!</red>",
                "<red>You are now in combat due to taking damage. Do not log out!</red>",
                "<red>You are being attacked by an unknown force. Do not log out!</red>"
        );

        public TaggedTypeSection attacker = new TaggedTypeSection(
                "<red>You are attacking <white>{enemy}</white>. Do not log out!</red>",
                "<red>You are attacking a(n) <white>{mob_type}</white>. Do not log out!</red>",
                "<red>You are attacking a(n) <white>{enemy}</white>. Do not log out!</red>",
                "<red>You are now in combat due to taking damage. Do not log out!</red>",
                "<red>You are attacking an unknown force. Do not log out!</red>"
        );

        // todo no clue if this works but am hoping
        public static class TaggedTypeSection implements Section {
            public String player;
            public String mob;
            public String mythicMob;
            public String damage;
            public String unknown;

            public TaggedTypeSection() {
                // required for config deserialization
            }

            public TaggedTypeSection(String player, String mob, String mythicMob, String damage, String unknown) {
                this.player = player;
                this.mob = mob;
                this.mythicMob = mythicMob;
                this.damage = damage;
                this.unknown = unknown;
            }
        }
    }

    // public ExpansionSection expansion = new ExpansionSection();
    @ApiStatus.Internal
    @Deprecated(forRemoval = true) // todo expansions should use their own language files.
    public static class ExpansionSection implements Section {

        public AngelChestExpansionSection angelChest = new AngelChestExpansionSection();
        public ActionBarExpansionSection actionBar = new ActionBarExpansionSection();
        public BossBarExpansionSection bossBar = new BossBarExpansionSection();
        public ScoreboardExpansionSection scoreboard = new ScoreboardExpansionSection();

        public static class AngelChestExpansionSection implements Section {
            @Comment("Shown when opening an AngelChest is prevented during combat.")
            public String preventOpening = "<red>You are not allowed to to open angel chests during combat.</red>";

            @Comment("Shown when breaking an AngelChest is prevented during combat.")
            public String preventBreaking = "<red>You are not allowed to break angel chests during combat.</red>";

            @Comment("Shown when fast looting an AngelChest is prevented during combat.")
            public String preventFastLooting = "<red>You are not allowed to fast loot angel chests during combat.</red>";
        }

        public static class ActionBarExpansionSection implements Section {
            @Comment("Shown above the hotbar while a player is in combat.")
            public String timer = "<bold><gold>Combat</gold> <gray>\u00BB</gray></bold> <white>{bars} <red>{combatlogx:time_left}</red> seconds.</white>";

            @Comment("Shown above the hotbar for a brief period when combat ends.")
            public String ended = "<bold><gold>Combat</gold> <gray>\u00BB</gray></bold> <white>You are no longer in combat.</white>";
        }

        public static class BossBarExpansionSection implements Section {
            @Comment("Shown on top of the screen while a player is in combat.")
            public String timer = "<bold><gold>Combat</gold> <gray>\u00BB</gray></bold> <white><red>{combatlogx:time_left}</red> seconds.</white>";

            @Comment("Shown on top of the screen for a brief period when combat ends.")
            public String ended = "<bold><gold>Combat</gold> <gray>\u00BB</gray></bold> <white>You are no longer in combat.</white>";
        }

        public static class ScoreboardExpansionSection implements Section {
            @Comment({"The scoreboard title for the sidebar.",
                    "Make sure to follow the scoreboard title limits for your version."
            })
            public String title = "<gold><bold>CombatLogX</bold></gold>";

            @Comment({"The scoreboard lines for the sidebar.",
                    "Make sure to follow the scoreboard line and character limits for your version."
            })
            public List<String> lines = ValueList.create(
                    " ",
                    "<white><bold>Combat Stats:</bold></white>",
                    "<dark_gray>\u00BB</dark_gray> <white><bold>Time Left:</bold></white> <gray>{combatlogx:time_left}</gray>",
                    "<dark_gray>\u00BB</dark_gray> <white><bold>Enemies:</bold></white> <gray>{combatlogx:enemy_count}</gray>",
                    "<dark_gray>\u00BB</dark_gray> <white><bold>Status:</bold></white> <gray>{combatlogx:status}</gray>",
                    " ",
                    "<white><bold>Enemies</bold></white>",
                    "<dark_gray>\u00BB</dark_gray> <gray>{combatlogx:specific_enemy_1_name}</gray>",
                    "<dark_gray>\u00BB</dark_gray> <gray>{combatlogx:specific_enemy_2_name}</gray>",
                    "<dark_gray>\u00BB</dark_gray> <gray>{combatlogx:specific_enemy_3_name}</gray>",
                    " "
            );
        }

        // didn't finish as unused now.
    }

    @Override
    public String decimalFormat() {
        return this.decimalFormat;
    }
}
