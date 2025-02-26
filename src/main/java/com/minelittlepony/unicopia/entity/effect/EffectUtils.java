package com.minelittlepony.unicopia.entity.effect;

import com.minelittlepony.unicopia.InteractionManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

public interface EffectUtils {
    static boolean isPoisoned(LivingEntity entity) {
        return getAmplifier(entity, UEffects.FOOD_POISONING) > 1;
    }

    static boolean hasABrokenWing(LivingEntity entity) {
        return entity.hasStatusEffect(UEffects.BROKEN_WINGS);
    }

    static boolean hasBothBrokenWing(LivingEntity entity) {
        return getAmplifier(entity, UEffects.BROKEN_WINGS) > 1;
    }

    static int getAmplifier(LivingEntity entity, StatusEffect effect) {
        return entity.hasStatusEffect(effect) ? entity.getStatusEffect(effect).getAmplifier() + 1 : 0;
    }

    static boolean isChangingRace(LivingEntity entity) {
        return entity.getStatusEffects().stream().anyMatch(effect -> effect.getEffectType() instanceof RaceChangeStatusEffect);
    }

    static boolean hasExtraDefenses(LivingEntity entity) {
        return entity.hasStatusEffect(UEffects.FORTIFICATION);
    }

    static boolean applyStatusEffect(LivingEntity entity, StatusEffect effect, boolean apply) {
        if (entity.getWorld().isClient) {
            return false;
        }
        boolean has = entity.hasStatusEffect(effect);
        if (has != apply) {
            if (has) {
                if (entity.getStatusEffect(effect).getDuration() == StatusEffectInstance.INFINITE) {
                    entity.removeStatusEffect(effect);
                }
            } else {
                entity.addStatusEffect(new StatusEffectInstance(effect, StatusEffectInstance.INFINITE, 0, false, false, true));
            }
            return true;
        }
        return false;
    }

    static Text formatModifierChange(String modifierName, int time, boolean isDetrimental) {
        return Text.literal(" ").append(Text.translatable("attribute.modifier." + (time > 0 ? "plus" : "take") + ".0",
                StringHelper.formatTicks(Math.abs(time), InteractionManager.getInstance().getTickRate()),
                Text.translatable(modifierName)
        ).formatted((isDetrimental ? time : -time) < 0 ? Formatting.DARK_GREEN : Formatting.RED));
    }

    static Text formatModifierChange(String modifierName, float change, boolean isDetrimental) {
        return Text.literal(" ").append(Text.translatable("attribute.modifier." + (change > 0 ? "plus" : "take") + ".0",
                ItemStack.MODIFIER_FORMAT.format(Math.abs(change)),
                Text.translatable(modifierName)
        ).formatted((isDetrimental ? change : -change) < 0 ? Formatting.DARK_GREEN : Formatting.RED));
    }

    static Text formatModifierChange(Text modifierName, float change, boolean isDetrimental) {
        return Text.literal(" ").append(Text.translatable("attribute.modifier." + (change > 0 ? "plus" : "take") + ".0",
                ItemStack.MODIFIER_FORMAT.format(Math.abs(change)),
               modifierName
        ).formatted((isDetrimental ? change : -change) < 0 ? Formatting.DARK_GREEN : Formatting.RED));
    }
}
