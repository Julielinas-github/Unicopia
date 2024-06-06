package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;

public interface SpellAttributes {
    Text CAST_ON_LOCATION = of(Unicopia.id("cast_on_location"));
    Text CAST_ON_PERSON = of(Unicopia.id("cast_on_person"));
    Identifier FOLLOWS_TARGET = Unicopia.id("follows_target");

    Identifier PERMIT_ITEMS = Unicopia.id("permit_items");
    Identifier PERMIT_PASSIVE = Unicopia.id("permit_passive");
    Identifier PERMIT_HOSTILE = Unicopia.id("permit_hostile");
    Identifier PERMIT_PLAYER = Unicopia.id("permit_player");

    Identifier FOCUSED_ENTITY = Unicopia.id("focused_entity");
    Identifier RANGE = Unicopia.id("range");
    Identifier DURATION = Unicopia.id("duration");
    Identifier STRENGTH = Unicopia.id("strength");
    Identifier VELOCITY = Unicopia.id("velocity");
    Identifier VERTICAL_VELOCITY = Unicopia.id("vertical_velocity");
    Identifier HANG_TIME = Unicopia.id("hang_time");
    Identifier PUSHING_POWER = Unicopia.id("pushing_power");
    Identifier CAUSES_LEVITATION = Unicopia.id("causes_levitation");
    Identifier AFFECTS = Unicopia.id("affects");
    Identifier DAMAGE_TO_TARGET = Unicopia.id("damage_to_target");
    Identifier SIMULTANIOUS_TARGETS = Unicopia.id("simultanious_targets");
    Identifier COST_PER_INDIVIDUAL = Unicopia.id("cost_per_individual");
    Identifier EXPLOSION_STRENGTH = Unicopia.id("explosion_strength");
    Identifier PROJECTILE_COUNT = Unicopia.id("projectile_count");
    Identifier ORB_COUNT = Unicopia.id("orb_count");
    Identifier WAVE_SIZE = Unicopia.id("wave_size");
    Identifier FOLLOW_RANGE = Unicopia.id("follow_range");
    Identifier LIGHT_TARGET = Unicopia.id("light_target");
    Identifier STICK_TO_TARGET = Unicopia.id("stick_to_target");
    Identifier SOAPINESS = Unicopia.id("soapiness");
    Identifier CAST_ON = Unicopia.id("cast_on");

    Identifier TARGET_PREFERENCE = Unicopia.id("target_preference");
    Identifier CASTER_PREFERENCE = Unicopia.id("caster_preference");

    @Deprecated
    static Text of(Identifier id) {
        return Text.literal(" ").append(Text.translatable(Util.createTranslationKey("spell_attribute", id))).formatted(Formatting.LIGHT_PURPLE);
    }

    @Deprecated
    static Text of(Identifier id, float value) {
        return Text.literal(" ").append(
                Text.translatable("attribute.modifier.equals.0",
                        ItemStack.MODIFIER_FORMAT.format(value),
                        Text.translatable(Util.createTranslationKey("spell_attribute", id)))
        ).formatted(Formatting.LIGHT_PURPLE);
    }

    @Deprecated
    static Text ofRelative(Identifier id, float value) {
        return EffectUtils.formatModifierChange(Util.createTranslationKey("spell_attribute", id), value, false);
    }

    @Deprecated
    static Text ofTime(Identifier id, long time) {
        return Text.literal(" ").append(Text.translatable("attribute.modifier.equals.0",
                StringHelper.formatTicks((int)Math.abs(time)),
                Text.translatable(Util.createTranslationKey("spell_attribute", id))
        ).formatted(Formatting.LIGHT_PURPLE));
    }

    @Deprecated
    public enum ValueType {
        REGULAR,
        TIME,
        PERCENTAGE,
        CONDITIONAL
    }
}
