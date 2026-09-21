package cn.dancingsnow.neoecoprototype1201;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.ECOTier;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * One capacity step of the prototype item-cell family.
 *
 * <p>eco 20.5.0 only offers the eager {@code AEKeyType} constructor and takes the type count in the
 * signature, so unlike the 1.21.1 line there is nothing to override. It rides on {@link ECOTier#L4}
 * because this project has no L1 multiblock yet.
 */
public final class PrototypeCellItem extends ECOStorageCellItem {
    public static final int TOTAL_TYPES = 256;

    public PrototypeCellItem(Item.Properties properties, Supplier<ECOCellType> cellType, long bytes) {
        super(properties, ECOTier.L4, AEKeyType.items(), cellType,
                bytes, (int) (bytes >> 8), TOTAL_TYPES, (double) bytes / (1L << 20));
    }
}
