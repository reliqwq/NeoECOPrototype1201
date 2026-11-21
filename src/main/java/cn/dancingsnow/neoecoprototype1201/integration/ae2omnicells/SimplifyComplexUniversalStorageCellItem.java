package cn.dancingsnow.neoecoprototype1201.integration.ae2omnicells;

import cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniCellTypes;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** L1 complex-omni matrix on eco's {@code complex_omni} cell type; same gating as the rest of the
 * family - constructed only inside the {@code @Integration}-gated integration class. */
public final class SimplifyComplexUniversalStorageCellItem extends ECOUniversalStorageCellItem {
    // The surcharged 1K, same as the item / fluid / chemical / FE families as of 1.4.6; 1M stays level.
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    public static final long BYTES_1M = 1L << 20;
    public static final int TOTAL_TYPES = 1024;
    public static final double IDLE_DRAIN = 8.0D;

    public static ECOCellType getComplexUniversalCellType() {
        return NERegistries.CELL_TYPE.get(new ResourceLocation("neoecoae", "complex_omni"));
    }

    public SimplifyComplexUniversalStorageCellItem(Item.Properties properties, long bytes) {
        super(properties, SimplifyTier.L1, SimplifyComplexUniversalStorageCellItem::getComplexUniversalCellType,
                IDLE_DRAIN, TOTAL_TYPES, bytes);
    }
}
