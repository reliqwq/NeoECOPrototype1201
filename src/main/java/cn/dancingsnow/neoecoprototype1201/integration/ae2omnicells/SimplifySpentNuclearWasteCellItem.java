package cn.dancingsnow.neoecoprototype1201.integration.ae2omnicells;

import cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniWasteCells;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** L1 spent-nuclear-waste matrix, on eco's own 乏核废料 row. The cell type is eco's, and it declares
 * {@code typeCount(1)} - every tier of this family holds one substance and only the byte total moves.
 * Its triple gate lives in {@link UniversalIntegration}: the cell type is eco's, but the substance the
 * family stores comes out of Mekanism via Applied Mekanistics. */
public final class SimplifySpentNuclearWasteCellItem extends ECOUniversalStorageCellItem {
    // The surcharged 1K, same as the item / fluid / chemical / FE families as of 1.4.6; 1M stays level.
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    public static final long BYTES_1M = 1L << 20;
    public static final int TOTAL_TYPES = 1;
    public static final double IDLE_DRAIN = 8.0D;

    public static ECOCellType getSpentWasteCellType() {
        return NERegistries.CELL_TYPE.get(new ResourceLocation("neoecoae", "spent_nuclear_waste"));
    }

    public SimplifySpentNuclearWasteCellItem(Item.Properties properties, long bytes) {
        super(properties, SimplifyTier.L1, SimplifySpentNuclearWasteCellItem::getSpentWasteCellType,
                IDLE_DRAIN, TOTAL_TYPES, bytes);
    }
}
