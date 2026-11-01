package cn.dancingsnow.neoecoprototype1201.block;

import cn.dancingsnow.neoecoae.blocks.CasingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A plain material casing that keeps eco's casing collision shape without its formed-state logic. It is a
 * crafting ingredient and a decoration only: no multiblock calculator ever looks for it, so a player can
 * place the L1 shell material without accidentally completing a machine.
 */
public class SimplifyCasingBlock extends Block {
    public SimplifyCasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CasingBlock.SHAPE;
    }
}
