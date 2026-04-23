package com.jenrex.shadowcompanion;

import com.jenrex.shadowcompanion.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ShadowCompanionMod.MOD_ID)
public class ShadowCompanionMod {

    public static final String MOD_ID = "shadowcompanion";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ShadowCompanionMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Tell Forge to register our entity type
        ModEntities.ENTITY_TYPES.register(modBus);

        // Run common setup on both client and server
        modBus.addListener(this::commonSetup);

        // Register for Forge game events (kills, ticks, etc.)
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Register our network channel (used for Z key → server communication)
        event.enqueueWork(ModNetwork::register);
    }
}