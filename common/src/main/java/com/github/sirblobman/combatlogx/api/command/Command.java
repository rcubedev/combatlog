package com.github.sirblobman.combatlogx.api.command;

import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Base command type for server commands.
 */
public abstract class Command {

    protected final CommandProperties commandProperties;
    protected final Logger logger;

    public Command(@NotNull CommandProperties commandProperties, Logger logger) {
        this.commandProperties = commandProperties;
        this.logger = logger;
    }

    public void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull CommandBuildContext context) {
        String[] aliasLiterals = commandProperties.alias();
        LiteralArgumentBuilder<CommandSourceStack> builder = build(context);
        // Register as a top-level literal (preserve original behavior)
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(builder);
        for (String aliasLiteral : aliasLiterals) {
            dispatcher.register(
                    Commands.literal(aliasLiteral)
                            .requires(builder.getRequirement())
                            .executes(builder.getCommand())
                            .redirect(root)
            );
        }
    }

    /**
     * Build the literal for this command without registering it. Useful when attaching
     * the command as a child of another literal (for example attaching under a literal {@code /example}).
     */
    // fixme this doesn't handle aliases.
    public LiteralArgumentBuilder<CommandSourceStack> build(@NotNull CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(commandProperties.literal())
                .requires(require(null, commandProperties.defaultRequiredLevel()));
        registerArguments(builder, context);
        return builder;
    }

    protected abstract void registerArguments(@NotNull LiteralArgumentBuilder<CommandSourceStack> literalBuilder, @NotNull CommandBuildContext context);

    public Predicate<CommandSourceStack> require(@Nullable String permission) {
        return require(permission, 2);
    }

    public Predicate<CommandSourceStack> require(@Nullable String permission, int defaultRequiredLevel) {
        return src -> {
            try {
                // return Permissions.check(src, permission(permission), defaultRequiredLevel);
                return true; // fixme temp
            } catch (Throwable ignored) {
                // Fallback for datapack compatibility / absence of permissions API
                return src.hasPermission(defaultRequiredLevel);
            }
        };
    }

    public Predicate<CommandSourceStack> require(@Nullable String permission, boolean fallback) {
        return src -> {
            try {
                // return Permissions.check(src, permission(permission), fallback);
                return true; // fixme temp
            } catch (Throwable ignored) {
                return fallback;
            }
        };
    }

    public boolean check(CommandSourceStack src, String permission) {
        return require(permission).test(src);
    }

    public boolean check(CommandSourceStack src, String permission, int defaultRequiredLevel) {
        return require(permission, defaultRequiredLevel).test(src);
    }

    public boolean check(CommandSourceStack src, String permission, boolean fallback) {
        return require(permission, fallback).test(src);
    }

    public CommandProperties defaultProperties() {
        return this.commandProperties;
    }

    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

}