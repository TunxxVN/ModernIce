package com.tunxxvn.modernice;

import com.tunxxvn.modernice.command.ModernIceCommand;
import com.tunxxvn.modernice.config.IceConfig;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod(modid = ModernIce.ID, name = ModernIce.NAME, version = ModernIce.VERSION,
        acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true, acceptableRemoteVersions = "*")
public final class ModernIce {
    public static final String ID = "modernice";
    public static final String NAME = "Modern Ice";
    public static final String VERSION = BuildInfo.VERSION;
    public static IceConfig config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new IceConfig(new File(event.getModConfigurationDirectory(), "modernice.cfg"));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new ModernIceCommand());
    }
}
