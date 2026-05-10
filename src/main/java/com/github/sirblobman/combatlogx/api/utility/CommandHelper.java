package com.github.sirblobman.combatlogx.api.utility;

import java.util.Locale;
import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.RunnableTaskDetails;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractRunnableTaskDetails;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class CommandHelper {
    public static void runSync(@NotNull ICombatLogX mod, @NotNull Runnable runnable) {
        // FoliaHelper foliaHelper = plugin.getFoliaHelper();
        TaskScheduler scheduler = mod.getScheduler();
        RunnableTaskDetails task = new RunnableTaskDetails(mod, TaskType.START_TICK, 0, runnable);
        // task.run(); // fixme
        scheduler.schedule(task);
    }

    public static void runAsStack(@NotNull CommandSourceStack sourceStack, @NotNull String command, @NotNull Consumer<Exception> exceptionLogger) {
        try {
            Commands commands = sourceStack.getServer().getCommands();
            CommandDispatcher<CommandSourceStack> dispatcher = commands.getDispatcher();
            ParseResults<CommandSourceStack> results = dispatcher.parse(command, sourceStack);
            commands.performCommand(results, command);
        } catch (Exception ex) {
            exceptionLogger.accept(ex);
        }
    }

    public static void runAsConsole(@NotNull ICombatLogX mod, @NotNull MinecraftServer server, @NotNull String command) {
        CommandSourceStack sourceStack = server.createCommandSourceStack();
        runAsStack(sourceStack, command, ex -> {
            String messageFormat = "Failed to execute command '/%s' as the server console:";
            String logMessage = String.format(Locale.US, messageFormat, command);

            Logger logger = mod.getLogger();
            logger.error(logMessage, ex);
        });
    }

    public static void runAsPlayer(@NotNull ICombatLogX mod, @NotNull ServerPlayer player, @NotNull String command) {
        CommandSourceStack sourceStack = player.createCommandSourceStack();
        runAsStack(sourceStack, command, ex -> {
            String playerName = player.getName().getString();
            String messageFormat = "Failed to execute command '/%s' as player '%s':";
            String logMessage = String.format(Locale.US, messageFormat, command, playerName);

            Logger logger = mod.getLogger();
            logger.error(logMessage, ex);
        });
    }

    public static void runAsOperator(@NotNull ICombatLogX mod, @NotNull ServerPlayer player, @NotNull String command) {
        CommandSourceStack playerSourceStack = player.createCommandSourceStack();
        MinecraftServer server = player.server;
        boolean isOp = server.getPlayerList().isOp(player.getGameProfile());
        CommandSourceStack sourceStack = isOp ? playerSourceStack : playerSourceStack.withPermission(server.getOperatorUserPermissionLevel());

        runAsStack(sourceStack, command, ex -> {
            String playerName = player.getName().getString();
            String messageFormat = "Failed to execute command '/%s' as player '%s' with operator permissions:";
            String logMessage = String.format(Locale.US, messageFormat, command, playerName);

            Logger logger = mod.getLogger();
            logger.error(logMessage, ex);
        });
    }
}
