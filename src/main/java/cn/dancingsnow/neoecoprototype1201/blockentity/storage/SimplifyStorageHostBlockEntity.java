package cn.dancingsnow.neoecoprototype1201.blockentity.storage;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.multiblock.cluster.SimplifyStorageCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Controller of the L1 storage matrix.
 *
 * <p>eco's own host carries the LDLib panel, the interface transfer mode and the infinite-storage
 * engine; none of those have a counterpart we ported yet, so this controller keeps only what the
 * cluster, the drives and the structure validator actually ask for. The drives read the tier and the
 * AE2 storage priority from here, which is the whole contract of a controller in this structure.</p>
 */
public class SimplifyStorageHostBlockEntity
        extends NEBlockEntity<SimplifyStorageCluster, SimplifyStorageHostBlockEntity> {

    private static final String TAG_STORAGE_PRIORITY = "storagePriority";

    private int storagePriority = 0;
    private boolean mirrored;

    public SimplifyStorageHostBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }

    public IECOTier getTier() {
        return SimplifyTier.L1;
    }

    public int getStoragePriority() {
        return storagePriority;
    }

    public void setStoragePriority(int priority) {
        if (this.storagePriority != priority) {
            this.storagePriority = priority;
            setChanged();
            markForUpdate();
        }
    }

    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
    }

    public boolean isMirrored() {
        return mirrored;
    }

    @Override
    protected void writeUiSyncTag(CompoundTag tag) {
        super.writeUiSyncTag(tag);
        tag.putInt(TAG_STORAGE_PRIORITY, storagePriority);
        tag.putBoolean("mirrored", mirrored);
    }

    @Override
    protected void readUiSyncTag(CompoundTag tag) {
        super.readUiSyncTag(tag);
        storagePriority = tag.getInt(TAG_STORAGE_PRIORITY);
        mirrored = tag.getBoolean("mirrored");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_STORAGE_PRIORITY, storagePriority);
    }

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
        storagePriority = tag.getInt(TAG_STORAGE_PRIORITY);
    }
}
