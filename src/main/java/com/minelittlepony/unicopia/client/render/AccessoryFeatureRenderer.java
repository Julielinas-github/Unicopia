package com.minelittlepony.unicopia.client.render;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides.ArmRenderer;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;

public class AccessoryFeatureRenderer<
        T extends LivingEntity,
        M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    private static final List<FeatureFactory<?, ?>> REGISTRY = new ArrayList<>();

    @SafeVarargs
    public static <T extends LivingEntity> void register(FeatureFactory<T, BipedEntityModel<T>>...factories) {
        for (var factory : factories) {
            REGISTRY.add(factory);
        }
    }

    private final Iterable<Feature<T>> features;

    @SuppressWarnings("unchecked")
    public AccessoryFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
        features = REGISTRY.stream().map(f -> ((FeatureFactory<T, M>)f).create(context)).toList();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (MineLPDelegate.getInstance().getRace(entity).isEquine()) {
            return;
        }

        features.forEach(feature -> feature.render(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch));

        Caster.of(entity).ifPresent(caster -> {
            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertexConsumers, light, caster, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
        });
    }

    public void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, ModelPart arm, Arm side) {
        features.forEach(feature -> feature.renderArm(matrices, vertexConsumers, light, entity, arm, side));
    }

    public boolean beforeRenderArms(ArmRenderer sender, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, int light) {
        Caster<?> caster = Caster.of(entity).orElse(null);
        if (caster != null) {
            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertexConsumers, light, caster, 0, 0, tickDelta, entity.age + tickDelta, 0, 0);
        }
        boolean cancelled = false;
        for (var feature : features) {
            cancelled |= feature.beforeRenderArms(sender, tickDelta, matrices, vertexConsumers, entity, light);
        }
        return cancelled;
    }

    public interface FeatureFactory<T extends LivingEntity, M extends EntityModel<T>> {
        Feature<T> create(FeatureRendererContext<T, M> context);
    }

    public interface Feature<T extends LivingEntity> {
        void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch);

        default void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, ModelPart arm, Arm side) {}

        default boolean beforeRenderArms(ArmRenderer sender, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, int light) {
            return false;
        }
    }

    public interface FeatureRoot<
            T extends LivingEntity,
            M extends EntityModel<T>> {
        AccessoryFeatureRenderer<T, M> getAccessories();
        @SuppressWarnings("unchecked")
        @Nullable
        static <T extends LivingEntity, M extends EntityModel<T>> FeatureRoot<T, M> of(T entity) {
            var renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            if (renderer instanceof FeatureRoot) {
                return (FeatureRoot<T, M>)renderer;
            }
            return null;
        }
    }
}
