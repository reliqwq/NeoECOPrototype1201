package cn.dancingsnow.neoecoprototype1201.integration.appflux;

import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.appflux.item.ECOFeStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * FE storage at L1.
 *
 * <p>Cell sizes are counted in AE2 bytes, and appflux turns a byte into FE with its own config
 * ({@code flux_cell.amount}, 1 Mi FE per byte), so appflux's {@code fe_1m_cell} is 1024 of its KiB units
 * = {@value #BYTES_1M} bytes. Our L1 tier already reports that same byte count for items, so the 1M and
 * 4M rungs sit level with appflux's own rather than under them. The 1K rung is ours alone: it takes
 * AE2's own 1K component, the same ingredient our L1 item cells take at that size.
 *
 * <p>Extends eco's FE cell, not its plain cell, because eco's version prints the stored FE number in
 * the tooltip and ours should read like the rest of the family. Nothing in this class names appflux:
 * the key type comes from eco's own constructor, which reaches for {@code FluxKeyType.TYPE} inside its
 * body - a method-body reference, so the class loads cleanly and only the gated call site can ever
 * resolve it.
 */
public final class SimplifyFeStorageCellItem extends ECOFeStorageCellItem {
    /** AE2's 1K component's byte count, one rung under appflux's own ladder. */
    public static final long BYTES_1K = 1L << 10;
    /** appflux's own 1M rung, and the byte count {@code SimplifyTier.L1} already carries. */
    public static final long BYTES_1M = 1L << 20;
    /** appflux's own 4M rung. */
    public static final long BYTES_4M = 1L << 22;

    private final long bytes;

    public SimplifyFeStorageCellItem(Item.Properties properties, Supplier<ECOCellType> cellType, long bytes) {
        // The tier-derived layout already lands on the 1M rung; the overrides below re-scale it for the
        // 4M rung and leave the 1M one equal to what eco would have computed anyway.
        super(properties, SimplifyTier.L1, cellType);
        this.bytes = bytes;
    }

    @Override
    public long getBytes() {
        return bytes;
    }

    @Override
    public int getBytesPerType() {
        return (int) (bytes >> 8);
    }

    /** Idle drain tracks capacity like eco's tier-derived cells do. */
    @Override
    public double getIdleDrain() {
        return (double) bytes / (1L << 20);
    }

    /** eco registers the flux cell type only when appflux is present, which is this family's gate too. */
    public static ECOCellType getFluxCellType() {
        ECOCellType type = NERegistries.CELL_TYPE.get(
                ResourceLocation.fromNamespaceAndPath("neoecoae", "flux"));
        if (type == null) {
            throw new IllegalStateException("Missing eco cell type: flux");
        }
        return type;
    }
}
