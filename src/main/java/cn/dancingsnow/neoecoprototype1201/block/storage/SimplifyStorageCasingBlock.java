package cn.dancingsnow.neoecoprototype1201.block.storage;

import cn.dancingsnow.neoecoae.blocks.ECOMachineCasing;
import cn.dancingsnow.neoecoprototype1201.multiblock.cluster.SimplifyStorageCluster;

/** Casing block of the L1 storage subsystem (reuses eco's casing rendering logic). */
public class SimplifyStorageCasingBlock extends ECOMachineCasing<SimplifyStorageCluster> {

    public SimplifyStorageCasingBlock(Properties properties) {
        super(properties);
    }
}
