package cn.dancingsnow.neoecoprototype1201.blockentity.storage;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.storage.ECOStorageSystemBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype1201.multiblock.definition.SimplifyStorageDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Controller of the L1 storage matrix: eco's own storage host with our tier handed in.
 *
 * <p>Everything the machine does is eco's - the panel (its {@code StorageHostUI} plus the priority strip and
 * the build controls, assembled by eco from a 20-field config), the storage priority, the cell mounting,
 * the interface transfer mode, and the power buffer of the energy cells beside it. What eco cannot answer
 * for an addon is the shape: its {@code getBuildDefinition()} is a {@code tableswitch} over its own tier
 * indexes (1..3 = L4/L6/L9) whose default is {@code aconst_null}, and every caller null-checks that answer,
 * which is what leaves the build controls inert on a tier-0 host. So this class exists for the override.
 *
 * <p>Subclassing is affordable here and that is measured: on eco 20.5.3 the machine is reached through
 * {@code grid.NEMachineLookup}, which enumerates {@code IGrid.getMachineClasses()} and matches with
 * {@code isAssignableFrom}, so a subclass stays visible to every consumer.
 */
public class SimplifyStorageHostBlockEntity extends ECOStorageSystemBlockEntity {
    public SimplifyStorageHostBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                          IECOTier tier) {
        super(type, pos, state, tier);
    }

    @Override
    public void onReady() {
        super.onReady();
        // After eco's own number, which is what this member's 64.0 AE/t is written as: an L1 frame of the
        // same size is meant to idle lower than an L4 one.
        getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.storageControllerIdlePower());
    }

    /** L1 is a shell of addon blocks, so its geometry is our own definition, not eco's L4 layout. */
    @Override
    public MultiBlockDefinition getBuildDefinition() {
        return SimplifyStorageDefinition.L1;
    }

    /**
     * Derived from the geometry on every validation, like eco's own {@code mirrored}: never written to NBT,
     * because the interface block that answers for it is itself in the world.
     */
    private boolean communicationInterface;

    public void setCommunicationInterface(boolean communicationInterface) {
        if (this.communicationInterface != communicationInterface) {
            this.communicationInterface = communicationInterface;
            updateState(false);
        }
    }

    /**
     * eco 20.5.3 already writes the formed state through this member - its own host sets
     * {@code MIRRORED = formed && mirrored} and {@code STORAGE_MODE} from the interface here, and
     * {@code NECluster} calls it virtually, so a subclass is part of the contract rather than a workaround.
     * We add the one property eco has no concept of, after super has done its pass.
     */
    @Override
    public void updateState(boolean gridSidesChanged) {
        super.updateState(gridSidesChanged);
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(SimplifyStorageControllerBlock.COMMUNICATION_INTERFACE)) {
            return;
        }
        boolean wanted = isFormed() && communicationInterface;
        if (state.getValue(SimplifyStorageControllerBlock.COMMUNICATION_INTERFACE) != wanted) {
            level.setBlock(worldPosition, state.setValue(
                    SimplifyStorageControllerBlock.COMMUNICATION_INTERFACE, wanted), Block.UPDATE_CLIENTS);
        }
    }
}
