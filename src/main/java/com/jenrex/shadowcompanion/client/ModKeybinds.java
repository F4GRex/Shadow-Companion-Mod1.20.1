package com.jenrex.shadowcompanion.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Defines the three keys for the mod.
 * Think of these like telling Minecraft "hey, watch for these keys".
 */
public class ModKeybinds {

    public static final KeyMapping SUMMON_KEY = new KeyMapping(
            "key.shadowcompanion.summon",       // Translation key (en_us.json)
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,                    // Default: Z
            "key.categories.shadowcompanion"    // Category in controls menu
    );

    public static final KeyMapping POSSESS_KEY = new KeyMapping(
            "key.shadowcompanion.possess",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,                    // Default: X
            "key.categories.shadowcompanion"
    );

    public static final KeyMapping SYSTEM_KEY = new KeyMapping(
            "key.shadowcompanion.system",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,                    // Default: R
            "key.categories.shadowcompanion"
    );
}