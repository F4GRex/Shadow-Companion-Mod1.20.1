package com.jenrex.shadowcompanion.client;

import com.jenrex.shadowcompanion.ModEntities;
import com.jenrex.shadowcompanion.ShadowCompanionMod;
import com.jenrex.shadowcompanion.client.renderer.ShadowCompanionModel;
import com.jenrex.shadowcompanion.client.renderer.ShadowCompanionRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * All the setup that only runs on the PLAYER'S COMPUTER (client side).
 * The server doesn't care about renderers or key bindings.
 */
@Mod.EventBusSubscriber(
        modid = ShadowCompanionMod.MOD_ID,
        bus   = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT  // ← CLIENT ONLY
)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Placeholder for future client-only setup
    }

    // Tell Forge which renderer to use for our entity
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SHADOW_COMPANION.get(), ShadowCompanionRenderer::new);
    }

    // Register the shape definition for the model
    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ShadowCompanionModel.LAYER_LOCATION, ShadowCompanionModel::createBodyLayer);
    }

    // Register the keybinds with Minecraft
    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent event) {
        event.register(ModKeybinds.SUMMON_KEY);
        event.register(ModKeybinds.POSSESS_KEY);
        event.register(ModKeybinds.SYSTEM_KEY);
    }
}