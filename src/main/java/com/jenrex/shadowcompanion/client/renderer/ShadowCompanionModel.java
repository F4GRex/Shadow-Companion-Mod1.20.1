package com.jenrex.shadowcompanion.client.renderer;

import com.jenrex.shadowcompanion.ShadowCompanionMod;
import com.jenrex.shadowcompanion.entity.ShadowCompanionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Defines the SHAPE of the shadow companion — like a paper doll.
 * It looks like a small humanoid figure (half player size).
 * We'll make it fancier with Blockbench in a later phase.
 */
public class ShadowCompanionModel<T extends ShadowCompanionEntity> extends EntityModel<T> {

    // Forge uses this to find our model definition
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    new ResourceLocation(ShadowCompanionMod.MOD_ID, "shadow_companion"),
                    "main"
            );

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ShadowCompanionModel(ModelPart root) {
        this.head     = root.getChild("head");
        this.body     = root.getChild("body");
        this.leftArm  = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg  = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    /**
     * Defines the actual box shapes that make up the model.
     * Coordinates are in "texels" (1 texel = 1/16 of a block).
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Head: 6 wide × 6 tall × 6 deep, sitting at the top
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3f, -6f, -3f,   6, 6, 6),
                PartPose.offset(0f, 0f, 0f)
        );

        // Body: 5 wide × 7 tall × 3 deep
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-2.5f, 0f, -1.5f,   5, 7, 3),
                PartPose.offset(0f, 0f, 0f)
        );

        // Left arm: offset to the right side
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(16, 0)
                        .addBox(-1f, -1f, -1f,   2, 7, 2),
                PartPose.offset(3.5f, 1.5f, 0f)
        );

        // Right arm: offset to the left side
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(16, 0)
                        .addBox(-1f, -1f, -1f,   2, 7, 2),
                PartPose.offset(-3.5f, 1.5f, 0f)
        );

        // Left leg
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-1f, 0f, -1f,   2, 7, 2),
                PartPose.offset(1.5f, 7f, 0f)
        );

        // Right leg
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-1f, 0f, -1f,   2, 7, 2),
                PartPose.offset(-1.5f, 7f, 0f)
        );

        // Texture sheet is 32×32 pixels
        return LayerDefinition.create(mesh, 32, 32);
    }

    /**
     * Called every frame to animate the model.
     * limbSwing/limbSwingAmount control leg & arm swing while walking.
     */
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Head follows your gaze
        this.head.xRot = headPitch    * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw   * ((float) Math.PI / 180F);

        // Arms swing opposite each other when walking
        this.rightArm.xRot = (float) Math.cos(limbSwing * 0.6662f)             * 1.4f * limbSwingAmount;
        this.leftArm.xRot  = (float) Math.cos(limbSwing * 0.6662f + Math.PI)   * 1.4f * limbSwingAmount;

        // Legs swing opposite to arms (natural walk cycle)
        this.rightLeg.xRot = (float) Math.cos(limbSwing * 0.6662f + Math.PI)   * 1.4f * limbSwingAmount;
        this.leftLeg.xRot  = (float) Math.cos(limbSwing * 0.6662f)              * 1.4f * limbSwingAmount;

        // Subtle floating bob (shadow entity feels weightless)
        this.body.y = (float) Math.sin(ageInTicks * 0.08f) * 0.4f;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float r, float g, float b, float a) {
        head    .render(pose, buffer, packedLight, packedOverlay, r, g, b, a);
        body    .render(pose, buffer, packedLight, packedOverlay, r, g, b, a);
        leftArm .render(pose, buffer, packedLight, packedOverlay, r, g, b, a);
        rightArm.render(pose, buffer, packedLight, packedOverlay, r, g, b, a);
        leftLeg .render(pose, buffer, packedLight, packedOverlay, r, g, b, a);
        rightLeg.render(pose, buffer, packedLight, packedOverlay, r, g, b, a);
    }
}