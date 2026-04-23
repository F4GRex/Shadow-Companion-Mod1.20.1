package com.jenrex.shadowcompanion.client.renderer;

import com.jenrex.shadowcompanion.ShadowCompanionMod;
import com.jenrex.shadowcompanion.entity.ShadowCompanionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Tells Minecraft HOW to draw our entity on screen.
 * It uses the model (shape) + texture (skin) to draw the companion.
 */
public class ShadowCompanionRenderer extends MobRenderer<ShadowCompanionEntity, ShadowCompanionModel<ShadowCompanionEntity>> {

    // Path to our texture PNG file
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ShadowCompanionMod.MOD_ID,
            "textures/entity/shadow_companion.png"
    );

    public ShadowCompanionRenderer(EntityRendererProvider.Context context) {
        super(context,
                new ShadowCompanionModel<>(context.bakeLayer(ShadowCompanionModel.LAYER_LOCATION)),
                0.3f  // Shadow radius (the oval shadow on the ground)
        );
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowCompanionEntity entity) {
        return TEXTURE;
    }
}