package com.minelittlepony.unicopia.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.ability.magic.CasterUtils;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ItemWielder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements PonyContainer<Equine<?>>, ItemWielder {
    @Shadow
    protected ItemStack activeItemStack;
    @Shadow
    protected int itemUseTimeLeft;

    @Shadow
    private Optional<BlockPos> climbingPos;

    private Equine<?> caster;

    private MixinLivingEntity() { super(null, null); }

    @Shadow
    protected abstract void setLivingFlag(int mask, boolean value);

    @Override
    public Equine<?> create() {
        return new Creature((LivingEntity)(Object)this);
    }

    @Override
    public Equine<?> get() {
        if (caster == null) {
            caster = create();
        }
        return caster;
    }

    @Inject(method = "isClimbing()Z", at = @At("HEAD"), cancellable = true)
    public void onIsClimbing(CallbackInfoReturnable<Boolean> info) {
        if (get() instanceof Pony && horizontalCollision) {
            ((Pony)get()).getSpellOrEmpty(DisguiseSpell.class, false)
            .map(DisguiseSpell::getDisguise)
            .filter(Disguise::canClimbWalls)
            .ifPresent(v -> {
                climbingPos = Optional.of(getBlockPos());
                info.setReturnValue(true);
            });
        }
    }

    @Inject(method = "isPushable()Z", at = @At("HEAD"), cancellable = true)
    private void onIsPushable(CallbackInfoReturnable<Boolean> info) {
        CasterUtils.toCaster(this)
            .flatMap(c -> c.getSpellOrEmpty(DisguiseSpell.class, false))
            .map(DisguiseSpell::getDisguise)
            .map(Disguise::getAppearance)
            .filter(Entity::isPushable)
            .ifPresent(v -> {
                info.setReturnValue(false);
            });
    }

    @Inject(method = "canSee(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void onCanSee(Entity other, CallbackInfoReturnable<Boolean> info) {
        if (get().isInvisible()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "jump()V", at = @At("RETURN"))
    private void onJump(CallbackInfo info) {
        get().onJump();
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo info) {
        if (get().beforeUpdate()) {
            info.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        get().tick();
    }

    @Inject(method = "<clinit>()V", at = @At("RETURN"), remap = false)
    private static void clinit(CallbackInfo info) {
        Creature.boostrap();
    }

    @Inject(method = "writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void onWriteCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        tag.put("unicopia_caster", get().toNBT());
    }

    @Inject(method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void onReadCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        if (tag.contains("unicopia_caster")) {
            get().fromNBT(tag.getCompound("unicopia_caster"));
        }
    }

    @ModifyConstant(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", constant = {
            @Constant(doubleValue = 0.08D),
            @Constant(doubleValue = 0.01D)
    })
    private double modifyGravity(double initial) {
        return get().getPhysics().calcGravity(initial);
    }

    @Override
    public void updateItemUsage(Hand hand, ItemStack stack, int time) {
        activeItemStack = stack;
        itemUseTimeLeft = time;

        if (!world.isClient) {
            setLivingFlag(1, !stack.isEmpty());
            setLivingFlag(2, hand == Hand.OFF_HAND);
        }
    }

    @Override
    protected BlockPos getLandingPos() {
        if (get().getPhysics().isGravityNegative()) {
            return get().getPhysics().getHeadPosition();
        }
        return super.getLandingPos();
    }

    @Override
    protected void spawnSprintingParticles() {
        if (get().getPhysics().isGravityNegative()) {
            get().getPhysics().spawnSprintingParticles();
        } else {
            super.spawnSprintingParticles();
        }
    }
}
