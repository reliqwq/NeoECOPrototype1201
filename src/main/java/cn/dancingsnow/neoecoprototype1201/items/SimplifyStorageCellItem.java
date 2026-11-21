package cn.dancingsnow.neoecoprototype1201.items;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * L1 storage matrix cells for script-made finite matrices (1 KiB / 16 KiB / 64 KiB / 1 MiB / 4 MiB).
 *
 * <p>Extends eco's {@link ECOStorageCellItem} so that eco's registered
 * {@code IECOCellHandler} already recognises it ({@code instanceof} check) and
 * routes all cell-engine work (persistence, partition list, upgrades, tooltips)
 * to eco's {@code ECOStorageCell} implementation - we never re-implement the
 * cell itself. The byte / per-type / type-count values are overridden so the L1
 * cells have their own lower capacity regardless of how the base class derives
 * them from the tier. Ported from the 1.21.1 line; the cell-type lookup goes
 * through {@link Registration#ecoCellType} here, which is this line's answer to
 * 1.21.1's {@code EcoCellTypes}.
 *
 * <p>Capacity ladder uses 256 storage types with a bytes-per-type ratio of 1/256:
 * 1 KiB = 1 &lt;&lt; 10, 16 KiB = 1 &lt;&lt; 14, 64 KiB = 1 &lt;&lt; 16,
 * 1 MiB = 1 &lt;&lt; 20, 4 MiB = 1 &lt;&lt; 22.
 */
public class SimplifyStorageCellItem extends ECOStorageCellItem {

    /** 1 KiB + 25% = 1280 bytes. */
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    /** 16 KiB + 25% = 20480 bytes. */
    public static final long BYTES_16K = (1L << 14) * 5 / 4;
    /** 64 KiB + 25% = 81920 bytes. */
    public static final long BYTES_64K = (1L << 16) * 5 / 4;
    /** 1 MiB */
    public static final long BYTES_1M = 1L << 20;
    /** 4 MiB */
    public static final long BYTES_4M = 1L << 22;

    /** The type cap every L1 matrix is written with; {@link #typeFee} prices against it. */
    public static final int MATRIX_TYPES = 256;

    /**
     * The one rule for what a type costs: filling the whole type cap spends a fifth of the cell.
     *
     * <p>eco bills a type by reserving {@code bytesPerType} for it for the life of the cell, so this number
     * is not a per-item price - it is the price of the type existing at all. Our rungs were written as
     * {@code bytes / totalTypes}, which reserves the entire cell and leaves a fully typed 1M matrix unable
     * to hold one single item; eco's own ladder lands far lower. A fifth is the same shape as eco's bottom
     * rung. The floor of one byte keeps a small cell from letting types cost nothing.</p>
     */
    public static int typeFee(long bytes, int totalTypes) {
        return (int) Math.max(1L, bytes / (5L * Math.max(1, totalTypes)));
    }

    private final long bytes;
    private final int bytesPerType;
    private final int totalTypes;

    /** Resolve after eco's custom registry has finished loading. */
    public static ECOCellType getItemCellType() {
        return Registration.ecoCellType("items");
    }

    /** Resolve after eco's custom registry has finished loading. */
    public static ECOCellType getFluidCellType() {
        return Registration.ecoCellType("fluids");
    }

    /** 1 MiB cell with the original L1 tuning. */
    public SimplifyStorageCellItem(Item.Properties properties, AEKeyType keyType, Supplier<ECOCellType> cellType) {
        this(properties, keyType, cellType, BYTES_1M, 1 << 12, 256);
    }

    /** Cell with the L1 type layout: 256 types, each costing bytes / 256. */
    public SimplifyStorageCellItem(Item.Properties properties, AEKeyType keyType, Supplier<ECOCellType> cellType,
                                   long bytes) {
        this(properties, keyType, cellType, bytes, typeFee(bytes, MATRIX_TYPES), MATRIX_TYPES);
    }

    public SimplifyStorageCellItem(Item.Properties properties, AEKeyType keyType, Supplier<ECOCellType> cellType,
                                   long bytes, int bytesPerType, int totalTypes) {
        // Idle drain scales with capacity exactly like eco's tier-derived cells
        // (totalBytes / 1 MiB AE per tick) instead of staying at the tier's 1.0.
        super(properties, SimplifyTier.L1, keyType, cellType, bytes, bytesPerType, (double) bytes / (1L << 20));
        this.bytes = bytes;
        this.bytesPerType = bytesPerType;
        this.totalTypes = totalTypes;
    }

    @Override
    public long getBytes() {
        return bytes;
    }

    @Override
    public int getBytesPerType() {
        return bytesPerType;
    }

    @Override
    public int getTotalTypes() {
        return totalTypes;
    }
}
