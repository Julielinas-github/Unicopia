package com.minelittlepony.unicopia.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;

@Mixin(BoatEntity.class)
abstract class MixinBoatEntity extends Entity implements LavaAffine {
    private static final TrackedData<Boolean> IS_LAVA_BOAT = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    MixinBoatEntity() { super(null, null); }

    @Redirect(
            method = {
                    "getWaterHeightBelow",
                    "checkBoatInWater",
                    "getUnderWaterLocation",
                    "fall",
                    "canAddPassenger"
            },
            at = @At(
                    value = "FIELD",
                    target = "net/minecraft/registry/tag/FluidTags.WATER:Lnet/minecraft/registry/tag/TagKey;",
                    opcode = Opcodes.GETSTATIC
            ),
            require = 0 // Forge
    )
    private TagKey<Fluid> redirectFluidTag() {
        return isLavaAffine() ? FluidTags.LAVA : FluidTags.WATER;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
        nbt.putBoolean("IsLavaAffine", isLavaAffine());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
        setLavaAffine(nbt.getBoolean("IsLavaAffine"));
    }

    @Inject(method = "initDataTracker", at = @At("HEAD"))
    private void onInitDataTracker(CallbackInfo info) {
        dataTracker.startTracking(IS_LAVA_BOAT, false);
    }

    @Override
    public void setLavaAffine(boolean lavaAffine) {
        dataTracker.set(IS_LAVA_BOAT, lavaAffine);
        if (lavaAffine) {
            ParticleUtils.spawnParticles(ParticleTypes.CLOUD, this, 10);
        }
    }

    @Override
    public boolean isLavaAffine() {
        return dataTracker.get(IS_LAVA_BOAT);
    }
}
