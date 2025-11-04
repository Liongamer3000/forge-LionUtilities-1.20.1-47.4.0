package net.liongamer.lionutilitiesmod.events;

import net.liongamer.lionutilitiesmod.LionUtilitiesMod;
import net.liongamer.lionutilitiesmod.commands.DoTournamentCommand;
import net.liongamer.lionutilitiesmod.commands.RevealPlayerCommand;
import net.liongamer.lionutilitiesmod.commands.SetFinaleCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = LionUtilitiesMod.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new RevealPlayerCommand(event.getDispatcher());
        new DoTournamentCommand(event.getDispatcher());
        new SetFinaleCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }

}
