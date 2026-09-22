package cn.dancingsnow.neoecoprototype1201.blockentity.storage;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.multiblock.cluster.SimplifyStorageCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Structure member of the L1 matrix. The validator only checks its block state, so this holds no
 * behaviour yet: eco's interface transfer mode lives in eco's own host, which this controller does
 * not use. Kept as a distinct type because the cluster sorts members by class.
 */
public class SimplifyStorageInterfaceBlockEntity
        extends NEBlockEntity<SimplifyStorageCluster, SimplifyStorageInterfaceBlockEntity> {

    public SimplifyStorageInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }
}
