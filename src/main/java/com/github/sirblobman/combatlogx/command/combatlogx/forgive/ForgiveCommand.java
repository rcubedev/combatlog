package com.github.sirblobman.combatlogx.command.combatlogx.forgive;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.command.CommandProperties;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.IForgiveManager;
import com.github.sirblobman.combatlogx.api.object.CombatTag;
import com.github.sirblobman.combatlogx.api.object.UntagReason;
import com.github.sirblobman.combatlogx.api.placeholder.PlaceholderHelper;
import com.github.sirblobman.combatlogx.command.combatlogx.Command;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

// todo do all the commands
public class ForgiveCommand extends Command {

    public ForgiveCommand(@NotNull ICombatLogX mod) {
        super(mod, CommandProperties.create("forgive", 0));
    }

    @Override
    protected void registerArguments(@NotNull LiteralArgumentBuilder<CommandSourceStack> literalBuilder, @NotNull CommandBuildContext context) {
        literalBuilder.then(
                Commands.literal("accept")
                        .executes(this::accept)
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .executes(this::acceptForPlayer)
                        )

        ).then(
                Commands.literal("reject")
                        .executes(this::reject)
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .executes(this::rejectForPlayer)
                        )
        ).then(
                Commands.literal("request")
                        .then(
                                Commands.argument("player", EntityArgument.player()) // todo(parity): make all these args limited to your enemies in combat.
                                        .executes(this::request)
                        )
        ).then(
                Commands.literal("toggle")
                        .executes(this::toggle)
        );
    }

    private int accept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer caller = context.getSource().getPlayerOrException();
        ICombatLogX mod = getCombatLogX();
        IForgiveManager forgiveManager = mod.getForgiveManager();
        CombatTag activeRequest = forgiveManager.getActiveRequest(caller);
        if (activeRequest == null) {
            context.getSource().sendFailure(Component.text("You have no active forgive requests")); // todo lang
            return FAILURE;
        }

        Entity enemy = activeRequest.getEnemy();
        if (enemy == null) {
            context.getSource().sendFailure(Component.text("You have no active forgive requests")); // todo lang
            return FAILURE;
        }

        ICombatManager combatManager = mod.getCombatManager();
        combatManager.untag(caller, enemy, UntagReason.ENEMY_FORGIVE);

        net.kyori.adventure.text.Component playerName = CombatLogX.createAudiences(caller).toAdventure(caller.getName());
        net.kyori.adventure.text.Component enemyName = PlaceholderHelper.getEnemyName(mod, caller, enemy);
        context.getSource().sendSuccess(enemyName.append(Component.text(" accepted your forgive request.")), false);
        enemy.sendSystemMessage(CombatLogX.createAudiences(caller).toNative(playerName.append(Component.text(" accepted your forgive request."))));
        return SUCCESS;
    }

    private int acceptForPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ICombatLogX mod = getCombatLogX();
        IForgiveManager forgiveManager = mod.getForgiveManager();
        CombatTag activeRequest = forgiveManager.getActiveRequest(player);
        return FAILURE;
    }

    private int reject(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return FAILURE;
    }

    private int rejectForPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return FAILURE;
    }

    private int request(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return FAILURE;
    }

    private int toggle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return FAILURE;
    }
}
