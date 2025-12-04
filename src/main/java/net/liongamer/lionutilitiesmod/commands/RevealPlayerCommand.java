package net.liongamer.lionutilitiesmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.liongamer.lionutilitiesmod.LionUtilitiesMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = LionUtilitiesMod.MOD_ID)
public class RevealPlayerCommand {
    private static int interval = -1;
    private static int tickCounter = 0;
    private static MinecraftServer server;
    private static List<ServerPlayer> players;
    private static int loopNumber = 0;
    public RevealPlayerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lionUtils").requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                .then(Commands.literal("revealPlayers").then(Commands.argument("interval", IntegerArgumentType.integer(1))
                        .executes(RevealPlayerCommand::revealInterval))));
    }

    private static int revealInterval(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DoTournamentCommand.getBool() && !SetFinaleCommand.getBool()) {
            interval = IntegerArgumentType.getInteger(context, "interval") * 60 * 20;
            LivingEntity player = context.getSource().getPlayer();
            server = context.getSource().getServer();

            context.getSource().sendSuccess(() -> Component.literal("Reveals random player's location every " + interval / 60 / 20 + " minutes"), false);
            return Command.SINGLE_SUCCESS;
        } else if(!DoTournamentCommand.getBool()) {
            throw new SimpleCommandExceptionType(
                    Component.literal("doTournament must be set to true")
            ).create();
        } else if(SetFinaleCommand.getBool()) {
            throw new SimpleCommandExceptionType(
                    Component.literal("Can't do while finale is ongoing")
            ).create();
        } else {
            throw new SimpleCommandExceptionType(
                    Component.literal("IDK what's wrong")
            ).create();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if(DoTournamentCommand.getBool() && !SetFinaleCommand.getBool()) {
            if (event.phase == TickEvent.Phase.END) { // Good practice
                if (interval > 0) {
                    if (tickCounter >= interval) {
                        tickCounter = 0;
                    }
                    if (tickCounter == 0) {
                        players = server.getPlayerList().getPlayers().stream().filter(player -> player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL)
                                .collect(Collectors.toList());

                        for (ServerPlayer player : players) {
                            showRandomPlayer(player);
                        }
                    }
                    tickCounter++;
                    for (ServerPlayer player : players) {
                        showMessage(player);
                    }
                }
            }
        }
    }

    private static void showRandomPlayer(ServerPlayer player) {
        ServerPlayer targetPlayer = players.get((int) (Math.random()*players.size()));
        if(loopNumber >= 100) {
                    player.sendSystemMessage(Component.literal("You are the only player in survival right now, there must be at least 2 players on the server for this command to work"));
                    return;
        }
        if (targetPlayer == player) {
            loopNumber++;
            showRandomPlayer(player);
        } else {
            loopNumber = 0;
            player.getPersistentData().putString(LionUtilitiesMod.MOD_ID + "message", new String(Math.round(targetPlayer.position().x) + " X " + Math.round(targetPlayer.position().y) + " Y " + Math.round(targetPlayer.position().z) + " Z"));
        }
    }

    private static void showMessage(ServerPlayer player) {
        Component title = Component.literal(Math.round(player.position().x) + " X " + Math.round(player.position().y) + " Y " + Math.round(player.position().z) + " Z " + player.getPersistentData().getString(LionUtilitiesMod.MOD_ID + "message"));

        player.connection.send(
                new ClientboundSetActionBarTextPacket(title)
        );
    }
}
