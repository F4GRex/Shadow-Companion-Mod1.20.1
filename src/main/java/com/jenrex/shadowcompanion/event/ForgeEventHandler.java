package com.jenrex.shadowcompanion.event;

import com.jenrex.shadowcompanion.ShadowCompanionMod;
import com.jenrex.shadowcompanion.entity.ShadowCompanionEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Listens for Forge game events — things that happen IN the world.
 * Right now it handles: mob dies → award SC points to the companion.
 */
@Mod.EventBusSubscriber(modid = ShadowCompanionMod.MOD_ID)
public class ForgeEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dying = event.getEntity();

        // Only run on the server side
        if (!(dying.level() instanceof ServerLevel serverLevel)) return;

        Entity killer = event.getSource().getEntity();
        if (killer == null) return;

        // Case 1: The companion itself made the kill
        if (killer instanceof ShadowCompanionEntity companion) {
            float pts = scPointsFor(dying);
            companion.addScPoints(pts);
            ShadowCompanionMod.LOGGER.debug(
                    "[SC] Companion killed {} → +{} SC points", dying.getName().getString(), pts);
        }

        // Case 2: The PLAYER made the kill while companion is nearby
        if (killer instanceof Player player) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof ShadowCompanionEntity companion
                        && player.getUUID().equals(companion.getOwnerUUID())
                        && companion.isActive()
                        && companion.distanceTo(player) <= 20) {

                    // Half points for player-assisted kills
                    companion.addScPoints(scPointsFor(dying) * 0.5f);
                    break;
                }
            }
        }
    }

    /**
     * How many SC points is killing this mob worth?
     * Boss checks will be added in a later phase.
     */
    private static float scPointsFor(LivingEntity mob) {
        if (mob instanceof Monster) {
            return 1.0f; // Hostile mob
        }
        return 0.5f; // Passive / neutral mob
    }
}