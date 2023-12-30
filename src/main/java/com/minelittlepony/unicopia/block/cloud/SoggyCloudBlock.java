package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SoggyCloudBlock extends CloudBlock implements Soakable {

    private final Supplier<Block> dryBlock;

    public SoggyCloudBlock(Settings settings, Supplier<Block> dryBlock) {
        super(settings.ticksRandomly(), false);
        setDefaultState(getDefaultState().with(MOISTURE, 7));
        this.dryBlock = dryBlock;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(MOISTURE);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return dryBlock.get().getPickStack(world, pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateWithMoisture(BlockState state, int moisture) {
        if (moisture <= 0) {
            return Soakable.copyProperties(state, dryBlock.get().getDefaultState());
        }
        return Soakable.copyProperties(state, getDefaultState()).with(MOISTURE, moisture);
    }

    @Override
    @Deprecated
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return Soakable.tryCollectMoisture(state, world, pos, player, hand, hit);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        Soakable.addMoistureParticles(state, world, pos, random);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Soakable.tickMoisture(state, world, pos, random);
    }
}
