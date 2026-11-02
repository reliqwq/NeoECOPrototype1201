package cn.dancingsnow.neoecoprototype1201.items;

import appeng.api.stacks.AEKeyType;
import appeng.api.storage.cells.ISaveProvider;
import appeng.items.contents.CellConfig;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import cn.dancingsnow.neoecoae.storage.ECOStorageCell;
import cn.dancingsnow.neoecoae.storage.bulk.ECOBulkStorageCell;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * One small-bulk (小宗) cell: an unbounded byte budget with only a few marked types.
 *
 * <p>The medium is a constructor argument, not a subclass - the same choice
 * {@link cn.dancingsnow.neoecoprototype1201.PrototypeCellItem} makes, so the item, fluid and chemical
 * members are this one class with three key types and three cell-type rows.</p>
 *
 * <p>The contents run through eco's own {@link ECOBulkStorageCell} rather than the {@code ECOStorageCell}
 * this class' parent would build: the family's whole point is {@link #BULK_BYTES}, and eco's bulk engine is
 * the one that keeps per-item accounts in {@code BigInteger} (its member list carries
 * {@code getStoredItemCountExact} / {@code getExactUsedBytes}) together with the marker and chain-filter
 * handling this cell needs. Writing that accounting here would be the third engine-level transform this
 * project has been told not to hand-roll.</p>
 *
 * <p>Its filter slots come from an override instead of from the inherited implementation because AE2's
 * two-argument {@code CellConfig.create} - the one eco's base class calls - is hard-coded to 63 slots
 * (measured: {@code bipush 63} at offset 11 of that method), so inheriting it would hand a three-type cell
 * sixty-three slots. The three-argument form takes the count and keeps AE2's own NBT persistence, so the
 * marks survive a relog without us writing a tag.</p>
 *
 * <p>No legacy-payload migration sits here, unlike the 1.21.1 twin of this class: that code moved contents
 * out of eco's MEGA long-bulk payload, which only exists in worlds that already ran the 1.21.1 family. This
 * line has never shipped one, so there is nothing to move and no payload format to read.</p>
 *
 * <p>The superclass chain stops at {@code Item} (measured on the published eco 20.5.3 jar), which is what
 * makes it safe to define here at all: Forge 1.20.1 force-loads every class in our jar while building the
 * event table, so a class whose parent reached MEGA Cells would crash any pack without it. The registration
 * is still gated on that mod, because the rows this family names - {@code neoecoae:mega_item},
 * {@code mega_fluid}, {@code mega_chemical} - are only registered by eco's megacells integration.</p>
 */
public final class SimplifySmallBulkCellItem extends ECOStorageCellItem {
    /** The byte budget a small-bulk cell reports: no ceiling, only the marks limit it. */
    public static final long BULK_BYTES = Long.MAX_VALUE;

    /** Marks on a cell straight out of the crafting table. */
    public static final int MARKED_TYPES = 3;

    /** Marks after one trip through a crafting table with the expansion card. */
    public static final int EXPANDED_MARKED_TYPES = 10;

    private final int markedTypes;

    public SimplifySmallBulkCellItem(Item.Properties properties, AEKeyType keyType,
                                     Supplier<ECOCellType> cellType, int markedTypes) {
        super(properties, SimplifyTier.L1, keyType, cellType, BULK_BYTES, 1, 0.0D);
        if (markedTypes <= 0) {
            throw new IllegalArgumentException("A small bulk cell needs at least one marked type");
        }
        this.markedTypes = markedTypes;
    }

    /**
     * The parent reads this number off the cell-type row, which for eco's MEGA rows is not our ceiling -
     * so the count stays ours, exactly as {@link cn.dancingsnow.neoecoprototype1201.PrototypeCellItem}
     * keeps the 256.
     */
    @Override
    public int getTotalTypes() {
        return markedTypes;
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack stack) {
        AEKeyType type = getKeyType();
        return CellConfig.create(key -> key.getType() == type, stack, markedTypes);
    }

    @Override
    protected ECOStorageCell createCellInventory(ItemStack stack, ISaveProvider saveProvider) {
        return new ECOBulkStorageCell(stack, saveProvider);
    }
}
