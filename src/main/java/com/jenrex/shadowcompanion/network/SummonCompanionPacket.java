package com.jenrex.shadowcompanion.network;

import com.jenrex.shadowcompanion.ModEntities;
import com.jenrex.shadowcompanion.entity.ShadowCompanionEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * This is the "message in a bottle" sent from your keyboard (client)
 * to the game world (server) when you press Z.
 *
 * Server then decides:
 *   → No companion yet?  → Spawn one at the player's feet
 *   → Companion active?  → Recall it (make invisible, freeze AI)
 *   → Companion recalled? → Resummon it at the player's feet
 */
public class SummonCompanionPacket {

    // No data needed — the server already knows who pressed the key
    public SummonCompanionPacket() {}

    public static void encode(SummonCompanionPacket packet, FriendlyByteBuf buf) {
        // Nothing to write
    }

    public static SummonCompanionPacket decode(FriendlyByteBuf buf) {
        return new SummonCompanionPacket();
    }

    public static void handle(SummonCompanionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();

            // Look for an existing companion owned by this player
            ShadowCompanionEntity existing = findCompanion(level, player.getUUID());

            if (existing != null) {
                // Toggle: recall if active, resummon if recalled
                if (existing.isActive()) {
                    // RECALL — make it invisible and stop its AI
                    existing.setActive(false);
                    existing.setInvisible(true);
                    existing.setNoAi(true);
                    // Teleport to player so it's ready for next summon
                    existing.teleportTo(player.getX(), player.getY(), player.getZ());
                } else {
                    // RESUMMON — bring it back
                    existing.setActive(true);
                    existing.setInvisible(false);
                    existing.setNoAi(false);
                    existing.teleportTo(player.getX(), player.getY(), player.getZ());
                }
            } else {
                // First time — spawn a brand new companion
                ShadowCompanionEntity companion = ModEntities.SHADOW_COMPANION.get().create(level);
                if (companion != null) {
                    companion.setOwnerUUID(player.getUUID());
                    companion.setActive(true);
                    companion.moveTo(player.getX(), player.getY(), player.getZ(),
                            player.getYRot(), 0f);
                    level.addFreshEntity(companion);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Scan all loaded entities to find this player's shadow companion.
     */
    private static ShadowCompanionEntity findCompanion(ServerLevel level, UUID ownerUUID) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof ShadowCompanionEntity companion) {
                if (ownerUUID.equals(companion.getOwnerUUID())) {
                    return companion;
                }
            }
        }
        return null;
    }
}