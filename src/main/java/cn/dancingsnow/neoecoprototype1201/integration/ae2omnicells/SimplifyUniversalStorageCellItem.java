package cn.dancingsnow.neoecoprototype1201.integration.ae2omnicells;

import cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniCellTypes;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * L1 Omni cell using eco's native OmniCells storage backend.
 *
 * <p>It rides eco's registered {@code neoecoae:omni} cell type, so the host panel labels this matrix's row
 * exactly as it labels eco's own omni matrices. Ported from the 1.21.1 line against eco 20.5.3, whose
 * {@code NEOmniCellTypes} carries the same three entries and whose {@code ECOUniversalStorageCellItem} has
 * the same six-argument constructor - the superclass chain reaches OmniCells, which is exactly why this
 * class is only ever constructed inside the {@code @Integration}-gated {@link UniversalIntegration}:
 * eco's own integration machinery is what keeps it from ever loading on a pack without OmniCells.
 */
public final class SimplifyUniversalStorageCellItem extends ECOUniversalStorageCellItem {
    // The surcharged 1K, same as the item / fluid / chemical / FE families as of 1.4.6; 1M stays level.
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    public static final long BYTES_1M = 1L << 20;
    public static final int TOTAL_TYPES = 512;
    public static final double IDLE_DRAIN = 8.0D;

    public static ECOCellType getUniversalCellType() {
        return NERegistries.CELL_TYPE.get(new ResourceLocation("neoecoae", "omni"));
    }

    public SimplifyUniversalStorageCellItem(Item.Properties properties, long bytes) {
        super(properties, SimplifyTier.L1, SimplifyUniversalStorageCellItem::getUniversalCellType,
                IDLE_DRAIN, TOTAL_TYPES, bytes);
    }
}
