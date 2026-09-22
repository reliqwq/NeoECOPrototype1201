package cn.dancingsnow.neoecoprototype1201.multiblock.cluster;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECluster;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyStorageCasingBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyStorageInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Cluster of the L1 storage subsystem. The layout / semantics mirror eco's own
 * {@code NEStorageCluster}, but bound to this addon's block entities and to the
 * eco generic base {@link NECluster} (grid + multiblock runtime inherited).
 */
public class SimplifyStorageCluster extends NECluster<SimplifyStorageCluster> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyStorageCluster.class);

    private SimplifyStorageHostBlockEntity controller = null;
    private final List<SimplifyDriveBlockEntity> drives = new ArrayList<>();
    private final List<SimplifyEnergyCellBlockEntity> energyCells = new ArrayList<>();
    private SimplifyStorageInterfaceBlockEntity theInterface = null;
    private final List<SimplifyStorageCasingBlockEntity> casings = new ArrayList<>();

    public SimplifyStorageCluster(BlockPos boundMin, BlockPos boundMax) {
        super(boundMin, boundMax);
    }

    public SimplifyStorageHostBlockEntity getController() {
        return controller;
    }

    public List<SimplifyDriveBlockEntity> getDrives() {
        return drives;
    }

    public List<SimplifyEnergyCellBlockEntity> getEnergyCells() {
        return energyCells;
    }

    public SimplifyStorageInterfaceBlockEntity getTheInterface() {
        return theInterface;
    }

    @Override
    public void addBlockEntity(NEBlockEntity<SimplifyStorageCluster, ?> blockEntity) {
        super.addBlockEntity(blockEntity);
        if (blockEntity instanceof SimplifyDriveBlockEntity driveBlockEntity) {
            drives.add(driveBlockEntity);
        } else if (blockEntity instanceof SimplifyEnergyCellBlockEntity energyCellBlockEntity) {
            energyCells.add(energyCellBlockEntity);
        } else if (blockEntity instanceof SimplifyStorageInterfaceBlockEntity interfaceBlockEntity) {
            theInterface = interfaceBlockEntity;
        } else if (blockEntity instanceof SimplifyStorageHostBlockEntity systemBlockEntity) {
            controller = systemBlockEntity;
        } else if (blockEntity instanceof SimplifyStorageCasingBlockEntity casingBlockEntity) {
            casings.add(casingBlockEntity);
        }
    }

    /**
     * Never hide a casing on this line. eco's 1.20.1 {@code NECluster} base returns {@code true}, which
     * would make the whole formed shell see-through, and its own storage rule (hide the casings touching
     * the controller) only looks right because the 1.21 line redraws hidden casings through
     * {@code shouldCasingRenderInClassic} - a hook 1.20.1 does not have, so the recess would just be a
     * hole of black inner faces.
     */
    @Override
    public boolean shouldCasingHide(NEBlockEntity<SimplifyStorageCluster, ?> blockEntity) {
        return false;
    }

    @Override
    public void destroy() {
        LOGGER.info("SimplifyStorageCluster DESTROYED at {} (min={}, max={})",
                controller != null ? controller.getBlockPos() : BlockPos.ZERO,
                getBoundsMin(), getBoundsMax());
        super.destroy();
        this.controller = null;
        this.theInterface = null;
        this.drives.clear();
        this.energyCells.clear();
        this.casings.clear();
    }
}
