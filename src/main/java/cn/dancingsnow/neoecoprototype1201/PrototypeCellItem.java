package cn.dancingsnow.neoecoprototype1201;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.ECOTier;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * An item cell on our own panel row.
 *
 * <p>eco 20.5.0 only offers the eager {@code AEKeyType} constructor and takes the type count in the
 * signature, so unlike the 1.21.1 line there is nothing to override and no lazy key handle to pass.
 * It rides on {@link ECOTier#L4} because this project has no L1 multiblock yet.
 */
public final class PrototypeCellItem extends ECOStorageCellItem {
    public static final int TOTAL_TYPES = 64;
    private static final long BYTES = 1L << 20;

    public PrototypeCellItem(Item.Properties properties, Supplier<ECOCellType> cellType) {
        super(properties, ECOTier.L4, AEKeyType.items(), cellType,
                BYTES, (int) (BYTES / TOTAL_TYPES), TOTAL_TYPES, 1.0D);
    }
}
