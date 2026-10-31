package cn.dancingsnow.neoecoprototype1201.blockentity.storage;

import cn.dancingsnow.neoecoae.blocks.entity.storage.ECODriveBlockEntity;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyPowerProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * L1 storage drive: eco's own drive, so the cell handling, the grid mounting, the render update and the
 * drops are eco's. Two things are ours.
 *
 * <p>The constructor: eco declares {@code ECODriveBlockEntity(BlockEntityType<ECODriveBlockEntity>, ...)},
 * an invariant type parameter, while a {@code BlockEntityType} built by this addon's helper arrives as
 * {@code BlockEntityType<?>}. Widening it here keeps the cast in one place instead of at every call site -
 * and the cluster accepts this instance because {@code NEStorageCluster.addBlockEntity} matches drives with
 * {@code instanceof ECODriveBlockEntity}.
 *
 * <p>The idle draw: eco writes 64.0 AE/t for this member in {@code onReady}, so the number is pushed again
 * after super, the same way the crafting members do it.
 */
public final class SimplifyDriveBlockEntity extends ECODriveBlockEntity {
    @SuppressWarnings("unchecked")
    public SimplifyDriveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super((BlockEntityType<ECODriveBlockEntity>) type, pos, state);
    }

    @Override
    public void onReady() {
        super.onReady();
        getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.storageDriveIdlePower());
    }
}
