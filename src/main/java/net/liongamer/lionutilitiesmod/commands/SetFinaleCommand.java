package net.liongamer.lionutilitiesmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.liongamer.lionutilitiesmod.LionUtilitiesMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
@Mod.EventBusSubscriber(modid = LionUtilitiesMod.MOD_ID)
public class SetFinaleCommand {
    private static Vec2 finaleLocation;
    private static boolean messageShown = false;
    private static final Map<UUID, Integer> titleTimers = new HashMap<>();
    private static MinecraftServer Mserver;


    public SetFinaleCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lionUtils").requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                .then(Commands.literal("chooseFinaleLocation").then(Commands.argument("from", Vec2Argument.vec2()).
                        then(Commands.argument("to", Vec2Argument.vec2())
                                .executes(SetFinaleCommand::setFinale)))));
    }

    private static int setFinale(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (DoTournamentCommand.getBool()) {

            Vec2 point1 = Vec2Argument.getVec2(context, "from");
            Vec2 point2 = Vec2Argument.getVec2(context, "to");

            double X = point1.x + (point2.x - point1.x) * Math.random();
            double Z = point1.y + (point2.y - point1.y) * Math.random();

            finaleLocation = new Vec2((float)X,(float)Z);
            showMessage(context.getSource().getPlayer(), finaleLocation);

            context.getSource().sendSuccess(() -> Component.literal("FinaleLocation has been successfully set to " + Math.round(finaleLocation.x) + " X " + Math.round(finaleLocation.y) + " Z"), false);
            return Command.SINGLE_SUCCESS;
        } else {
            throw new SimpleCommandExceptionType(
                    Component.literal("doTournament must be set to true")
            ).create();
        }
    }

    private static void showMessage(ServerPlayer player, Vec2 finaleLocation) {
        Component title = Component.literal(Math.round(finaleLocation.x) + " X " + Math.round(finaleLocation.y) + " Z");


        MinecraftServer server = player.getServer();
        Mserver = server;
        if (server != null ) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                Component bigTitle = Component.literal("The Finale Has Started");
                Component smolTitle = Component.literal("Head To The Given Location");

                p.connection.send(new ClientboundSetTitlesAnimationPacket(20, 80, 20));
                p.connection.send(new ClientboundSetTitleTextPacket(bigTitle));
                p.connection.send(new ClientboundSetSubtitleTextPacket(smolTitle));
                messageShown = true;
            }
        }
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (messageShown && DoTournamentCommand.getBool()) {
            if (Mserver != null) {
                for (ServerPlayer player : Mserver.getPlayerList().getPlayers()) {
                    showActionBar(player, finaleLocation);
                }
            }
        } else {
            messageShown = false;
        }
    }
    private static void showActionBar(ServerPlayer player, Vec2 finaleLocation) {
        Component title = Component.literal(Math.round(player.position().x) + " X " + Math.round(player.position().y) + " Y " + Math.round(player.position().z) + " Z " + Math.round(finaleLocation.x) + " X " + Math.round(finaleLocation.y) + " Z");

        player.connection.send(
                new ClientboundSetActionBarTextPacket(title)
        );
    }

    public static boolean getBool() {
        return messageShown;
    }
}
