package cn.dancingsnow.neoecoprototype1201;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * One capacity step of the prototype item-cell family.
 *
 * <p>The panel row is eco's own medium row, not a row we register: on this line
 * {@code getTotalTypes()} reads {@link ECOCellType#typeCount()}, so the count is overridden below and the
 * row is left to say what the medium is. See {@link #TOTAL_TYPES} for what borrowing the count would cost.
 *
 * <p>The tier is not cosmetic: {@code ECODriveBlockEntity.mountInventories} returns without mounting
 * unless the host's tier compares {@code >=} the cell's, and {@code IECOTier#compareTo} is
 * {@code Integer.compare(getTier(), ...)} - so a cell claiming a tier above our L1 host's index 0 sits
 * in the drive, shows its capacity on the panel, and never reaches the network.
 */
public final class PrototypeCellItem extends ECOStorageCellItem {
    /**
     * The type ceiling the item and fluid families carry: 256 types, each costing one 256th of the cell.
     *
     * <p>It is a number we hold rather than one we borrow from the panel row. eco 20.5.3's
     * {@code ECOStorageCellItem.getTotalTypes()} answers {@code cellType().typeCount()}, and the row the
     * 1.21.1 line rides ({@code neoecoae:items}) carries 315 - so reading the row would quietly move the
     * ceiling. Overriding the getter keeps the wording of the row (物品) and the capacity of our own tier.
     * The chemical family passes its own number instead; see {@code AppMekIntegration#TOTAL_TYPES}.
     */
    public static final int TOTAL_TYPES = 256;

    private final int totalTypes;

    public PrototypeCellItem(Item.Properties properties, Supplier<ECOCellType> cellType, long bytes) {
        this(properties, AEKeyType.items(), cellType, bytes, TOTAL_TYPES);
    }

    /**
     * The medium is a constructor argument, not a subclass: eco's drive reads whatever key type the cell
     * reports, and its own fluid and chemical cells are the same item class with a different one.
     */
    public PrototypeCellItem(Item.Properties properties, AEKeyType keyType,
                             Supplier<ECOCellType> cellType, long bytes) {
        this(properties, keyType, cellType, bytes, TOTAL_TYPES);
    }

    public PrototypeCellItem(Item.Properties properties, AEKeyType keyType,
                             Supplier<ECOCellType> cellType, long bytes, int totalTypes) {
        super(properties, SimplifyTier.L1, keyType, cellType,
                bytes, (int) (bytes >> 8), (double) bytes / (1L << 20));
        this.totalTypes = totalTypes;
    }

    @Override
    public int getTotalTypes() {
        return totalTypes;
    }
}
