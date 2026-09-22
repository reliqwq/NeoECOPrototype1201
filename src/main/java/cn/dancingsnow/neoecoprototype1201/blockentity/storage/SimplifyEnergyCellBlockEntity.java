package cn.dancingsnow.neoecoprototype1201.blockentity.storage;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.multiblock.cluster.SimplifyStorageCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Structure member of the L1 matrix. Carries no power buffer yet: the validator only requires its
 * block and facing, and the drives mount storage through their own grid nodes, so the matrix works
 * without a buffer. Costs idle power like eco's cells so the structure stays expensive.
 */
public class SimplifyEnergyCellBlockEntity
        extends NEBlockEntity<SimplifyStorageCluster, SimplifyEnergyCellBlockEntity> {

    public SimplifyEnergyCellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }

    @Override
    public void onReady() {
        super.onReady();
        getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.highIdleComponentPower());
    }
}
