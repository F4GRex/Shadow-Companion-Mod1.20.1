package com.jenrex.shadowcompanion.network;

import com.jenrex.shadowcompanion.ShadowCompanionMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Sets up a communication channel between the player's game (client)
 * and the game world (server). When you press Z, the client sends
 * a message through this channel telling the server to summon/recall.
 */
public class ModNetwork {

    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;
    private static int nextId = 0;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(ShadowCompanionMod.MOD_ID, "main"),
                () -> PROTOCOL,
                PROTOCOL::equals,
                PROTOCOL::equals
        );

        // Register our summon packet (more packets will be added later)
        CHANNEL.registerMessage(
                nextId++,
                SummonCompanionPacket.class,
                SummonCompanionPacket::encode,
                SummonCompanionPacket::decode,
                SummonCompanionPacket::handle
        );
    }
}