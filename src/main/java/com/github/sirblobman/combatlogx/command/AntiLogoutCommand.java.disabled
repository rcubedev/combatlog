package com.github.sirblobman.combatlogx.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.configuration.OldConfiguration;

import static net.minecraft.commands.Commands.literal;
import static com.github.sirblobman.combatlogx.CombatLogX.config;

public class AntiLogoutCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        config.generateReloadableConfigCommand(CombatLogX.MOD_ID, dispatcher, OldConfiguration::readConfigFile);

        var rootNode = dispatcher.getRoot().getChild("antilogout");
        dispatcher.register(literal("al").redirect(rootNode));
    }
}
