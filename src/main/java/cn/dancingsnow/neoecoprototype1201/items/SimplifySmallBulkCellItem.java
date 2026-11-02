package cn.dancingsnow.neoecoprototype1201.items;

import appeng.api.stacks.AEKeyType;
import appeng.items.contents.CellConfig;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * One small-bulk (小宗) cell: an unbounded byte budget with only a few marked types.
 *
 * <p>The medium is a constructor argument, not a subclass - the same choice
 * {@link cn.dancingsnow.neoecoprototype1201.PrototypeCellItem} makes, so the item and fluid members are
 * this one class with two key types and two cell-type rows.</p>
 *
 * <p>The contents stay on the cell this class' parent builds: its per-key capacity comes out of
 * {@code bytes / bytesPerType}, so {@link #BULK_BYTES} with one byte per type is what makes the amount
 * effectively uncapped, exactly as on the 1.21.1 twin. eco's own bulk engine was tried first and cannot be
 * used here - {@code ECOBulkStorageCell.getTotalItemTypes} casts the stack's item to
 * {@code ECOBulkStorageCellItem}, which is written for the item medium alone (both of its constructors take
 * {@code (Properties, IECOTier, Supplier<ECOCellType>[, int])} with {@code AEKeyType.items()} inside, its
 * expansion card is fixed to {@code neoecoae:eco_mega_upgrade_card} and its type limit is validated to
 * 1..25, not to this family's numbers). Measured cost of ignoring that: a cell returning that engine
 * crashed the moment any tooltip was built - client crash 2026-11-02 23:10:43, {@code ClassCastException}
 * naming this class, thrown while the creative tab filled its search tree - which is every inventory screen,
 * not just ours, and a fluid cell could never have used it at all.</p>
 *
 * <p>Its filter slots come from an override instead of from the inherited implementation because AE2's
 * two-argument {@code CellConfig.create} - the one eco's base class calls - is hard-coded to 63 slots
 * (measured: {@code bipush 63} at offset 11 of that method), so inheriting it would hand a three-type cell
 * sixty-three slots. The three-argument form takes the count and keeps AE2's own NBT persistence, so the
 * marks survive a relog without us writing a tag, and eco's {@code ECOStorageCell.getConfigInventory} is
 * what reads them back when the cell decides what may enter.</p>
 *
 * <p>No legacy-payload migration sits here, unlike the 1.21.1 twin of this class: that code moved contents
 * out of eco's MEGA long-bulk payload, which only exists in worlds that already ran the 1.21.1 family. This
 * line has never shipped one, so there is nothing to move and no payload format to read.</p>
 *
 * <p>The superclass chain stops at {@code Item} (measured on the published eco 20.5.3 jar), which is what
 * makes it safe to define here at all: Forge 1.20.1 force-loads every class in our jar while building the
 * event table, so a class whose parent reached MEGA Cells would crash any pack without it. The registration
 * is still gated on that mod, because the rows this family names - {@code neoecoae:mega_item} and
 * {@code mega_fluid} - are only registered by eco's megacells integration.</p>
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
}
