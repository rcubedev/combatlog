package com.github.sirblobman.combatlogx.command;

import java.util.Locale;
import java.util.function.Function;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.command.Command;
import com.github.sirblobman.combatlogx.command.combatlogx.forgive.ForgiveCommand;

public class CommandManager {

    public static final Function<ICombatLogX, Command[]> COMMANDS = mod -> new Command[]{
        new ForgiveCommand(mod)
    };

    /**
     * CommandManager holds the list of available commands. It no longer auto-registers them
     * at construction time to avoid registering both top-level and `/iac`-attached variants.
     */
    private CommandManager() {
        // no-op
    }

    /**
     * Helper to register all commands as top-level literals. Use only if you want commands
     * available at root rather than under a literal.
     * 
     * @param dispatcher the command dispatcher
     * @param context the command registry access context
     */
    public static void registerTopLevel(ICombatLogX mod, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        Command[] commands = COMMANDS.apply(mod);
        for (Command command : commands) {
            command.register(dispatcher, context);
        }
    }

    // public static void dumpCommands(CommandDispatcher<ServerCommandSource> dispatcher, MinecraftServer server) {
    //     // Optional: implement a dump if desired
    // }

    /**
     * Register all commands as children of a given literal (for example `inertiaanticheat`).
     *
     * @param literalName the name of the literal to register under
     * @param dispatcher the command dispatcher
     * @param context the command registry access context
     * @return the built literal argument (if you want to do something with it, otherwise ignore the return value)
     */
    public static LiteralArgumentBuilder<CommandSourceStack> registerUnderLiteral(ICombatLogX mod, String literalName, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        Command[] commands = COMMANDS.apply(mod);
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder = LiteralArgumentBuilder.literal(literalName);
        for (Command command : commands) {
            literalBuilder.then(command.build(context));
        }
        dispatcher.register(literalBuilder);
        return literalBuilder;
    }

    /**
     * Convenience wrapper that registers commands under the canonical long name
     * and adds '/iac' as a short alias.
     * 
     * @param dispatcher the command dispatcher
     * @param context the command registry access context
     */
    public static void registerUnderIAC(ICombatLogX mod, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder = registerUnderLiteral(mod, "inertiaanticheat", dispatcher, context);
        LiteralCommandNode<CommandSourceStack> destination = literalBuilder.build();
        LiteralCommandNode<CommandSourceStack> aliasNode = buildRedirect("iac", destination);
        dispatcher.getRoot().addChild(aliasNode);
    }

    /**
     * Taken from Velocity <a href="https://github.com/PaperMC/Velocity/blob/8abc9c80a69158ebae0121fda78b55c865c0abad/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L38">BrigadierUtils</a>
     * <p>
     * Returns a literal node that redirects its execution to the given destination node.
     *
     * @param alias the command alias (e.g. "iac")
     * @param destination the destination node (e.g. the node for "inertiaanticheat")
     * @return the built alias node
     */
    public static LiteralCommandNode<CommandSourceStack> buildRedirect(
            final String alias, final LiteralCommandNode<CommandSourceStack> destination) {
        // Redirects only work for nodes with children, but break the top argument-less command.
        // Manually adding the root command after setting the redirect doesn't fix it.
        // See https://github.com/Mojang/brigadier/issues/46). Manually clone the node instead.
        LiteralArgumentBuilder<CommandSourceStack> builder =
                LiteralArgumentBuilder
                        .<CommandSourceStack>literal(alias.toLowerCase(Locale.ROOT))
                        .requires(destination.getRequirement())
                        .forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
                        .executes(destination.getCommand());

        // Build the alias node and attach the same children as the destination so it behaves the same.
        LiteralCommandNode<CommandSourceStack> aliasNode = builder.build();
        for (CommandNode<CommandSourceStack> child : destination.getChildren()) {
            aliasNode.addChild(child);
        }

        return aliasNode;
    }
}
