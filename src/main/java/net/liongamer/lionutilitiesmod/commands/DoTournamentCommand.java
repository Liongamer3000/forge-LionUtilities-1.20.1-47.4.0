package net.liongamer.lionutilitiesmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DoTournamentCommand {
    private static boolean doTournament = false;
    public DoTournamentCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lionUtils").requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                .then(Commands.literal("doTournament").then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(DoTournamentCommand::doTournament))));
    }

    private static int doTournament(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        doTournament = BoolArgumentType.getBool(context, "enabled");

        context.getSource().sendSuccess(() -> Component.literal("doTournament has been set to " + getBool()), false);
        return Command.SINGLE_SUCCESS;
    }

    public static boolean getBool() {
        return doTournament;
    }
}
