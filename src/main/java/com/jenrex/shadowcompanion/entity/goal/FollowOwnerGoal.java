package com.jenrex.shadowcompanion.entity.goal;

import com.jenrex.shadowcompanion.entity.ShadowCompanionEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Makes the shadow companion walk toward its owner when they get too far apart.
 * Think of it like an invisible leash that pulls the companion back.
 */
public class FollowOwnerGoal extends Goal {

    private final ShadowCompanionEntity companion;
    private final PathNavigation        navigation;
    private Player owner;

    private int    recalcPathTicks = 0;
    private final float startDistance; // Start following at this distance
    private final float stopDistance;  // Stop following at this distance

    public FollowOwnerGoal(ShadowCompanionEntity companion, float startDistance, float stopDistance) {
        this.companion     = companion;
        this.navigation    = companion.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance  = stopDistance;
        // This goal controls movement AND looking
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    // Should we START following right now?
    @Override
    public boolean canUse() {
        Player owner = this.companion.getOwner();
        if (owner == null)            return false; // No owner found
        if (owner.isSpectator())      return false; // Don't follow ghosts
        if (companion.getTarget() != null) return false; // We're busy fighting
        // Only follow if we're actually too far away
        if (companion.distanceToSqr(owner) <= (startDistance * startDistance)) return false;
        this.owner = owner;
        return true;
    }

    // Should we KEEP following?
    @Override
    public boolean canContinueToUse() {
        if (companion.getTarget() != null)     return false; // Start fighting instead
        Player owner = companion.getOwner();
        if (owner == null)                     return false;
        // Keep following until we're close enough
        return companion.distanceToSqr(owner) > (stopDistance * stopDistance);
    }

    @Override
    public void start() {
        this.recalcPathTicks = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
    }

    @Override
    public void tick() {
        if (owner == null) return;

        // Always look at the owner
        companion.getLookControl().setLookAt(owner, 10.0f, companion.getMaxHeadXRot());

        // Recalculate movement path every 10 ticks (0.5 seconds)
        // (Doing it every tick is wasteful)
        if (--recalcPathTicks <= 0) {
            recalcPathTicks = 10;
            if (!companion.isLeashed() && !companion.isPassenger()) {
                navigation.moveTo(owner, 1.2); // 1.2× normal speed
            }
        }
    }
}