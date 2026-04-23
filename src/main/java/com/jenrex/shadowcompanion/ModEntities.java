package com.jenrex.shadowcompanion;

import com.jenrex.shadowcompanion.entity.ShadowCompanionEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// This class listens to the MOD event bus (setup events)
@Mod.EventBusSubscriber(modid = ShadowCompanionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {

    // A "deferred register" is like a waiting list — Forge fills it in at the right time
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ShadowCompanionMod.MOD_ID);

    // This registers our shadow companion entity type with Forge
    public static final RegistryObject<EntityType<ShadowCompanionEntity>> SHADOW_COMPANION =
            ENTITY_TYPES.register("shadow_companion", () ->
                    EntityType.Builder.<ShadowCompanionEntity>of(ShadowCompanionEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.9f)        // Half the size of a player
                            .clientTrackingRange(64)  // Clients can see it from 64 blocks
                            .build("shadow_companion")
            );

    // Forge needs us to manually attach stats (health, speed, etc.) to our entity
    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(SHADOW_COMPANION.get(), ShadowCompanionEntity.createAttributes().build());
    }
}