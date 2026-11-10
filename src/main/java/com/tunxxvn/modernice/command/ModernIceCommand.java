package com.tunxxvn.modernice.command;

import com.tunxxvn.modernice.ModernIce;
import com.tunxxvn.modernice.config.IceConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class ModernIceCommand extends CommandBase {
    @Override public String getCommandName() { return "ice"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/ice"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // GuiChat closes after commands; open the screen on the next tick.
        FMLCommonHandler.instance().bus().register(new OpenConfig());
    }

    public static final class OpenConfig {
        @SubscribeEvent
        public void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            FMLCommonHandler.instance().bus().unregister(this);
            Minecraft.getMinecraft().displayGuiScreen(new IceConfigScreen(ModernIce.config));
        }
    }
}
