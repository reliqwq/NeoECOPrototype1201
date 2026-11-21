package cn.dancingsnow.neoecoprototype1201.integration.ae2omnicells;

import cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniCellTypes;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** L1 quantum matrix using eco's unlimited-type OmniCells backend. Ported unchanged from 1.21.1
 * (bytes and types included); see {@link UniversalIntegration} for the gating. */
public final class SimplifyQuantumStorageCellItem extends ECOUniversalStorageCellItem {
    // The surcharged 1K as of 1.4.6; the 1M is where this family stands alone: +50% (1572864 bytes)
    // while every other omni family stays at the raw 1048576 - the quantum rung is worth half again
    // a universal 1M, which is the point of it.
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    public static final long BYTES_1M = (1L << 20) * 3 / 2;
    public static final int UNLIMITED_TYPES = -1;
    public static final double IDLE_DRAIN = 8.0D;

    public static ECOCellType getQuantumCellType() {
        return NERegistries.CELL_TYPE.get(new ResourceLocation("neoecoae", "quantum_omni"));
    }

    public SimplifyQuantumStorageCellItem(Item.Properties properties, long bytes) {
        super(properties, SimplifyTier.L1, SimplifyQuantumStorageCellItem::getQuantumCellType,
                IDLE_DRAIN, UNLIMITED_TYPES, bytes);
    }
}
