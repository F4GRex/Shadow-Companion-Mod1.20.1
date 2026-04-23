package com.jenrex.shadowcompanion.entity;

import com.jenrex.shadowcompanion.entity.goal.FollowOwnerGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class ShadowCompanionEntity extends PathfinderMob {

    // ------------------------------------------------------------
    // Synced data: the client (your screen) needs to know these
    // ------------------------------------------------------------
    private static final EntityDataAccessor<Boolean> IS_ACTIVE =
            SynchedEntityData.defineId(ShadowCompanionEntity.class, EntityDataSerializers.BOOLEAN);

    // ------------------------------------------------------------
    // Server-only data (not sent to client)
    // ------------------------------------------------------------
    @Nullable
    private UUID ownerUUID;

    // Companion stats (all start at 1, max is 999)
    private int companionLevel = 1;
    private float scPoints     = 0f;
    private float xpProgress   = 0f;   // 0.0 → 1.0 progress to next level
    private int strStat        = 1;
    private int agiStat        = 1;
    private int dexStat        = 1;
    private int vitStat        = 1;
    private int endStat        = 1;

    // Death cooldown in ticks (20 ticks = 1 second)
    private int deathCooldown = 0;
    public static final int MAX_DEATH_COOLDOWN = 200 * 20; // 200 seconds

    // ------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------
    public ShadowCompanionEntity(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
    }

    // ------------------------------------------------------------
    // Base attributes (starting stats for Minecraft's engine)
    // ------------------------------------------------------------
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,          20.0)  // VIT scales this up
                .add(Attributes.MOVEMENT_SPEED,       0.35)  // AGI scales this up
                .add(Attributes.ATTACK_DAMAGE,         4.0)  // STR scales this up
                .add(Attributes.FOLLOW_RANGE,          48.0)
                .add(Attributes.ARMOR,                  0.0)  // END scales this up
                .add(Attributes.KNOCKBACK_RESISTANCE,   0.5); // Hard to knock around
    }

    // ------------------------------------------------------------
    // AI Goals — what the companion does on its own
    // ------------------------------------------------------------
    @Override
    protected void registerGoals() {
        // Priority 1: Follow the owner when not in combat
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 12.0f, 3.0f));
        // Priority 2: Punch enemies
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        // Priority 8: Stare at the owner casually
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        // Priority 9: Look around randomly
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // Target the nearest hostile mob automatically
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    // ------------------------------------------------------------
    // Register synced data fields
    // ------------------------------------------------------------
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ACTIVE, false);
    }

    // ------------------------------------------------------------
    // Save stats to disk (so they survive world reloads)
    // ------------------------------------------------------------
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
        tag.putBoolean("IsActive",        this.isActive());
        tag.putInt    ("SCLevel",         this.companionLevel);
        tag.putFloat  ("SCPoints",        this.scPoints);
        tag.putFloat  ("XPProgress",      this.xpProgress);
        tag.putInt    ("STR",             this.strStat);
        tag.putInt    ("AGI",             this.agiStat);
        tag.putInt    ("DEX",             this.dexStat);
        tag.putInt    ("VIT",             this.vitStat);
        tag.putInt    ("END",             this.endStat);
        tag.putInt    ("DeathCooldown",   this.deathCooldown);
    }

    // ------------------------------------------------------------
    // Load stats from disk
    // ------------------------------------------------------------
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
        this.entityData.set(IS_ACTIVE, tag.getBoolean("IsActive"));
        this.companionLevel = tag.getInt("SCLevel");
        this.scPoints       = tag.getFloat("SCPoints");
        this.xpProgress     = tag.getFloat("XPProgress");
        this.strStat        = tag.getInt("STR");
        this.agiStat        = tag.getInt("AGI");
        this.dexStat        = tag.getInt("DEX");
        this.vitStat        = tag.getInt("VIT");
        this.endStat        = tag.getInt("END");
        this.deathCooldown  = tag.getInt("DeathCooldown");
        updateAttributesFromStats();
    }

    // ------------------------------------------------------------
    // Every tick (runs 20 times per second)
    // ------------------------------------------------------------
    @Override
    public void tick() {
        super.tick();

        // Only the server runs this logic (not the player's screen)
        if (this.level().isClientSide) return;

        // Count down death cooldown
        if (deathCooldown > 0) {
            deathCooldown--;
        }

        // Tether check — snap back if too far from owner
        Player owner = getOwner();
        if (owner != null && isActive()) {
            double distance = this.distanceTo(owner);
            float maxDistance = getMaxTetherDistance();

            if (distance > maxDistance + 10) {
                // Teleport back near the owner
                this.teleportTo(
                        owner.getX() + (this.random.nextDouble() - 0.5) * 2,
                        owner.getY(),
                        owner.getZ() + (this.random.nextDouble() - 0.5) * 2
                );
            }
        }
    }

    // ------------------------------------------------------------
    // Never auto-despawn; always save to disk
    // ------------------------------------------------------------
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    // Shadows don't get pushed around by other entities
    @Override
    public boolean isPushable() {
        return false;
    }

    // ------------------------------------------------------------
    // Update Minecraft's engine stats from our custom stats
    // (called every time a stat changes)
    // ------------------------------------------------------------
    public void updateAttributesFromStats() {
        // STR → attack damage:  base 4 + STR bonus
        Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE))
                .setBaseValue(4.0 + (strStat - 1) * 0.15);

        // AGI → movement speed: base 0.35 + AGI bonus
        Objects.requireNonNull(getAttribute(Attributes.MOVEMENT_SPEED))
                .setBaseValue(0.35 + (agiStat - 1) * 0.001);

        // VIT → max health: base 20 + VIT bonus
        double newMaxHealth = 20.0 + (vitStat - 1) * 0.5;
        Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH))
                .setBaseValue(newMaxHealth);
        // Don't let current health exceed new max
        if (this.getHealth() > newMaxHealth) {
            this.setHealth((float) newMaxHealth);
        }

        // END → armor: scales up to 30 max
        Objects.requireNonNull(getAttribute(Attributes.ARMOR))
                .setBaseValue(Math.min((endStat - 1) * 0.03, 30.0));
    }

    // ------------------------------------------------------------
    // Helper: how far can the companion roam before snapping back?
    // (scales with companion level)
    // ------------------------------------------------------------
    public float getMaxTetherDistance() {
        return 5.0f + (this.companionLevel * 0.5f);
    }

    // ------------------------------------------------------------
    // Find the owner Player in the world
    // ------------------------------------------------------------
    @Nullable
    public Player getOwner() {
        if (ownerUUID == null) return null;
        Level world = this.level();
        if (world instanceof ServerLevel serverLevel) {
            return serverLevel.getPlayerByUUID(ownerUUID);
        }
        return null;
    }

    // ------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------
    public boolean isActive()          { return this.entityData.get(IS_ACTIVE); }
    public UUID    getOwnerUUID()      { return ownerUUID; }
    public int     getCompanionLevel() { return companionLevel; }
    public float   getScPoints()       { return scPoints; }
    public float   getXpProgress()     { return xpProgress; }
    public int     getStrStat()        { return strStat; }
    public int     getAgiStat()        { return agiStat; }
    public int     getDexStat()        { return dexStat; }
    public int     getVitStat()        { return vitStat; }
    public int     getEndStat()        { return endStat; }
    public int     getDeathCooldown()  { return deathCooldown; }

    // ------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------
    public void setActive(boolean active)         { this.entityData.set(IS_ACTIVE, active); }
    public void setOwnerUUID(UUID uuid)           { this.ownerUUID = uuid; }
    public void setDeathCooldown(int ticks)       { this.deathCooldown = ticks; }
    public void addScPoints(float points)         { this.scPoints += points; }

    // Stat setters — cap at 999 and refresh Minecraft attributes
    public void setStrStat(int v) { this.strStat = Math.min(v, 999); updateAttributesFromStats(); }
    public void setAgiStat(int v) { this.agiStat = Math.min(v, 999); updateAttributesFromStats(); }
    public void setDexStat(int v) { this.dexStat = Math.min(v, 999); updateAttributesFromStats(); }
    public void setVitStat(int v) { this.vitStat = Math.min(v, 999); updateAttributesFromStats(); }
    public void setEndStat(int v) { this.endStat = Math.min(v, 999); updateAttributesFromStats(); }
}