/*package com.github.github.sirblobman.combatlogx.configuration;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import org.samo_lego.config2brigadier.common.IBrigadierConfigurator;
import org.samo_lego.config2brigadier.common.annotation.BrigadierDescription;

import java.io.File;
import java.util.List;

public class OldConfiguration implements IBrigadierConfigurator {
    public static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("antilogout.json").toFile();

    public static OldConfiguration readConfigFile() {
        return IBrigadierConfigurator.loadConfigFile(CONFIG_FILE, OldConfiguration.class, OldConfiguration::new);
    }

    @Override
    public void save() {
        this.writeToFile(CONFIG_FILE);
    }
    
    @SerializedName("// Disables all logouts, regardless of the other configuration.")
    public final String _comment_disableAllLogouts0 = "";
    @SerializedName("disable_all_logouts")
    @BrigadierDescription(defaultOption = "false")
    public boolean disableAllLogouts = false;

    public Afk afk = new Afk();

    public static class Afk {
        
        @SerializedName("afk_kick_message")
        @BrigadierDescription(defaultOption = "You are now AFK.")
        public String afkMessage = "You are now AFK.";
        
        @SerializedName("// Permission level for /afk command. 0 := everyone, 4 := op, 2 := permission level 2")
        public final String _comment_permissionLevel0 = "";
        @SerializedName("// Used only if permission is not set by e.g. luckperms.")
        public final String _comment_permissionLevel1 = "";
        @SerializedName("permission_level")
        @BrigadierDescription(defaultOption = "2")
        public int permissionLevel = 2;

        @SerializedName("// Max time limit for /afk command, in seconds. Can be -1 for unlimited.")
        public final String _comment_maxAfkTime = "";
        @SerializedName("afk_time_limit")
        @BrigadierDescription(defaultOption = "-1")
        public double maxAfkTime = -1;
    }

    public CombatLog combatLog = new CombatLog();

    public static class CombatLog {
        @SerializedName("// After how many seconds player leaves 'combat' mode and can disconnect, in seconds.")
        public final String _comment_combatTimeout = "";
        @SerializedName("combat_end_timeout")
        @BrigadierDescription(defaultOption = "30.0")
        public double combatTimeout = 30.0D;
        
        @SerializedName("// If true, player will only be affected after being hurt by another player.")
        public final String _comment_playerHurtOnly0 = "";
        @SerializedName("// Otherwise, all damage sources will count.")
        public final String _comment_playerHurtOnly1 = "";
        @SerializedName("player_hurt_only")
        @BrigadierDescription(defaultOption = "true")
        public boolean playerHurtOnly = true;

        @SerializedName("// Which permission level is required to bypass combat log.")
        public final String _comment_bypassPermissionLevel0 = "";
        @SerializedName("// Only active if not set by a permission mod.")
        public final String _comment_bypassPermissionLevel1 = "";
        @SerializedName("required_bypass_permission_level")
        @BrigadierDescription(defaultOption = "4")
        public int bypassPermissionLevel = 4;

        @SerializedName("link_tnt")
        @BrigadierDescription(defaultOption = "true")
        public boolean linkTnt = true;

        @SerializedName("link_pets")
        @BrigadierDescription(defaultOption = "true")
        public boolean linkPets = true;

        @SerializedName("link_projectiles")
        @BrigadierDescription(defaultOption = "true")
        public boolean linkProjectiles = true;

        @SerializedName("link_fishing_rod")
        @BrigadierDescription(defaultOption = "true")
        public boolean linkFishingRod = true; // TODO :: impl

        @SerializedName("// If true, player will be notified when combat starts / ends.")
        public final String _comment_notifyOnCombat = "";
        @SerializedName("notify_combat")
        public boolean notifyInCombat = true;

        @SerializedName("bar_count")
        @BrigadierDescription(defaultOption = "30")
        public int barCount = 30;

        @SerializedName("left_bars_symbol")
        public String barLeftSymbol = "<green>|";

        @SerializedName("right_bars_symbol")
        public String barRightSymbol = "<red>|";

        @SerializedName("combat_mode_message")
        public String combatEnterMessage = "<dark_red>[AL] <red>Combat <white><bars><duration>s";
        @SerializedName("safe_disconnect_message")
        public String combatEndMessage = "<dark_green>[AL] <green>You can now disconnect safely.";

        @SerializedName("ignored_projectiles")
        public List<String> ignoredProjectiles = List.of("minecraft:egg", "minecraft:ender_pearl", "minecraft:snowball");
    }
}*/
