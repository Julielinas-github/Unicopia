package com.minelittlepony.unicopia.ability.magic.spell.attribute;

import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public record SpellAttribute<T> (
        Trait trait,
        BiFunction<SpellTraits, Float, T> valueGetter,
        TooltipFactory tooltipFactory
) implements TooltipFactory {
    @Override
    public void appendTooltip(CustomisedSpellType<?> type, List<Text> tooltip) {
        tooltipFactory.appendTooltip(type, tooltip);
    }

    public T get(SpellTraits traits) {
        return valueGetter.apply(traits, traits.get(trait));
    }

    public static <T extends Number> SpellAttribute<T> create(SpellAttributeType id, AttributeFormat format, Trait trait, BiFunction<SpellTraits, Float, @NotNull T> valueGetter) {
        return create(id, format, format, trait, valueGetter, false);
    }

    public static <T extends Number> SpellAttribute<T> create(SpellAttributeType id, AttributeFormat format, Trait trait, Float2ObjectFunction<@NotNull T> valueGetter) {
        return create(id, format, format, trait, valueGetter, false);
    }

    public static <T extends Number> SpellAttribute<T> create(SpellAttributeType id, AttributeFormat baseFormat, AttributeFormat relativeFormat, Trait trait, Float2ObjectFunction<@NotNull T> valueGetter) {
        return create(id, baseFormat, relativeFormat, trait, valueGetter, false);
    }

    public static <T extends Number> SpellAttribute<T> create(SpellAttributeType id, AttributeFormat baseFormat, AttributeFormat relativeFormat, Trait trait, BiFunction<SpellTraits, Float, @NotNull T> valueGetter) {
        return create(id, baseFormat, relativeFormat, trait, valueGetter, false);
    }

    public static <T extends @NotNull Number> SpellAttribute<T> create(SpellAttributeType id, AttributeFormat baseFormat, AttributeFormat relativeFormat, Trait trait, Float2ObjectFunction<@NotNull T> valueGetter, boolean detrimental) {
        return create(id, baseFormat, relativeFormat, trait, (traits, value) -> valueGetter.get(value.floatValue()), detrimental);
    }

    public static <T extends @NotNull Number> SpellAttribute<T> create(SpellAttributeType id, AttributeFormat baseFormat, AttributeFormat relativeFormat, Trait trait, BiFunction<SpellTraits, Float, @NotNull T> valueGetter, boolean detrimental) {
        return createRanged(id, baseFormat, relativeFormat, trait, null, valueGetter, detrimental);
    }

    public static <T extends Number> SpellAttribute<T> createRanged(SpellAttributeType id, AttributeFormat baseFormat, AttributeFormat relativeFormat, Trait trait,
            @Nullable SpellAttribute<T> maxValueGetter,
            BiFunction<SpellTraits, Float, @NotNull T> valueGetter,
            boolean detrimental) {
        final BiFunction<SpellTraits, Float, @NotNull T> clampedValueGetter = maxValueGetter == null ? valueGetter : (traits, f) -> {
            T t = valueGetter.apply(traits, f);
            T max = maxValueGetter.get(traits);
            return max.floatValue() > 0 && t.floatValue() > max.floatValue() ? max : t;
        };
        return new SpellAttribute<>(trait, clampedValueGetter, (CustomisedSpellType<?> type, List<Text> tooltip) -> {
            float traitAmount = type.traits().get(trait);
            float traitDifference = type.relativeTraits().get(trait);
            float max = maxValueGetter == null ? 0 : maxValueGetter.get(type.traits()).floatValue();
            float value = clampedValueGetter.apply(type.traits(), traitAmount).floatValue();

            var b = max > 0
                    ? baseFormat.getBase(id.name(), value, max, "equals", Formatting.LIGHT_PURPLE)
                    : baseFormat.getBase(id.name(), value, "equals", Formatting.LIGHT_PURPLE);
            if (traitDifference != 0) {
                tooltip.add(b.append(relativeFormat.getRelative(valueGetter.apply(type.traits(), traitAmount - traitDifference).floatValue(), value, detrimental)));
                tooltip.add(AttributeFormat.formatTraitDifference(trait, traitDifference));
            } else {
                tooltip.add(b);
            }
        });
    }


    public static SpellAttribute<Boolean> createConditional(SpellAttributeType id, Trait trait, Float2ObjectFunction<Boolean> valueGetter) {
        return createConditional(id, trait, (traits, value) -> valueGetter.get(value.floatValue()));
    }

    public static SpellAttribute<Boolean> createConditional(SpellAttributeType id, Trait trait, BiFunction<SpellTraits, Float, @NotNull Boolean> valueGetter) {
        return new SpellAttribute<>(trait, valueGetter, (CustomisedSpellType<?> type, List<Text> tooltip) -> {
            float difference = type.relativeTraits().get(trait);
            Text value = AttributeFormat.formatAttributeLine(id.name());
            if (!valueGetter.apply(type.traits(), type.traits().get(trait))) {
                value = value.copy().formatted(Formatting.STRIKETHROUGH, Formatting.DARK_GRAY);
            }
            tooltip.add(value);
            if (difference != 0) {
                tooltip.add(AttributeFormat.formatTraitDifference(trait, difference));
            }
        });
    }

    public static <T extends Enum<T>> SpellAttribute<T> createEnumerated(SpellAttributeType id, Trait trait, Float2ObjectFunction<T> valueGetter) {
        return createEnumerated(id, trait, (traits, value) -> valueGetter.get(value.floatValue()));
    }

    public static <T extends Enum<T>> SpellAttribute<T> createEnumerated(SpellAttributeType id, Trait trait, BiFunction<SpellTraits, Float, @NotNull T> valueGetter) {
        Function<T, Text> cache = Util.memoize(t -> Text.translatable(Util.createTranslationKey("spell_attribute", id.id().withPath(p -> p + "." + t.name().toLowerCase(Locale.ROOT)))));
        return new SpellAttribute<>(trait, valueGetter, (CustomisedSpellType<?> type, List<Text> tooltip) -> {
            T t = valueGetter.apply(type.traits(), type.traits().get(trait));

            if (t != null) {
                int max = t.getClass().getEnumConstants().length;
                tooltip.add(Text.translatable(" %s (%s/%s)", cache.apply(t), Text.literal("" + (t.ordinal() + 1)).formatted(Formatting.LIGHT_PURPLE), max).formatted(Formatting.DARK_PURPLE));
            }
            float difference = type.relativeTraits().get(trait);
            if (difference != 0) {
                tooltip.add(AttributeFormat.formatTraitDifference(trait, difference));
            }
        });
    }
}
