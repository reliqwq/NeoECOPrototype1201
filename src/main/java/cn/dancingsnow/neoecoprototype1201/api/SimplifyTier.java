package cn.dancingsnow.neoecoprototype1201.api;

import cn.dancingsnow.neoecoae.api.IECOTier;
import net.minecraft.resources.ResourceLocation;

/**
 * Tier definition of the "1 级" storage subsystem.
 *
 * <p>eco's own tiers are L4/L6/L9 whose internal tier indexes are 1/2/3
 * (see {@code cn.dancingsnow.neoecoae.api.ECOTier}). This addon adds the
 * lower tier L1 (internal index 0):
 * <ul>
 *   <li>per storage cell bytes : 1 MiB (1 &lt;&lt; 20)</li>
 *   <li>per energy cell       : 100 000 AE</li>
 * </ul>
 * Because {@code IECOTier#compareTo} compares {@link #getTier()}, an L1 host
 * (index 0) accepts only cells whose tier index is &le; 0 (i.e. our own L1
 * cells), while eco's L4+ hosts (index &ge; 1) keep accepting our L1 cells -
 * the intended upgrade path.
 */
public enum SimplifyTier implements IECOTier {
    L1(0, 1L << 20, 100_000L, 1L << 20, 1, 1, 16, 1),
    /** The energized computation cell: same tier index, four times the CPU bytes. */
    L1_REINFORCED(0, 1L << 20, 100_000L, 5_242_880L, 1, 1, 128, 4),
    /**
     * The energized threading core: sixteen real threads, because eco allocates one
     * {@code ECOCraftingCPU} per thread in the core's constructor.
     */
    L1_ENERGIZED_THREADING(0, 1L << 20, 100_000L, 1L << 20, 1, 1, 16, 16),
    /**
     * The shell-mounted parallel member; only {@link #getCPUAccelerators()} is read for it, since eco
     * sums that over the parallel cores it finds in the bounding box.
     */
    L1_PARALLEL_SWITCH(0, 1L << 20, 100_000L, 1L << 20, 1, 1, 1024, 1);

    private final int tier;
    private final long storageTotalBytes;
    private final long powerStorageSize;
    private final long cpuTotalBytes;
    private final int crafterParallel;
    private final int overclockedCrafterParallel;
    private final int cpuAccelerators;
    private final int cpuThreads;

    SimplifyTier(int tier, long storageTotalBytes, long powerStorageSize, long cpuTotalBytes,
                 int crafterParallel, int overclockedCrafterParallel, int cpuAccelerators, int cpuThreads) {
        this.tier = tier;
        this.storageTotalBytes = storageTotalBytes;
        this.powerStorageSize = powerStorageSize;
        this.cpuTotalBytes = cpuTotalBytes;
        this.crafterParallel = crafterParallel;
        this.overclockedCrafterParallel = overclockedCrafterParallel;
        this.cpuAccelerators = cpuAccelerators;
        this.cpuThreads = cpuThreads;
    }

    @Override
    public int getTier() {
        return tier;
    }

    // L1 computation capacity is one eighth of the previous 8 MiB setting.

    @Override
    public int getCrafterParallel() {
        return crafterParallel;
    }

    @Override
    public int getOverclockedCrafterParallel() {
        return overclockedCrafterParallel;
    }

    @Override
    public int getCPUAccelerators() {
        return cpuAccelerators;
    }

    @Override
    public int getCPUThreads() {
        return cpuThreads;
    }

    @Override
    public long getCPUTotalBytes() {
        return cpuTotalBytes;
    }

    @Override
    public long getStorageTotalBytes() {
        return storageTotalBytes;
    }

    @Override
    public long getPowerStorageSize() {
        return powerStorageSize;
    }

    @Override
    public ResourceLocation getCPUOverlayTexture() {
        return ResourceLocation.fromNamespaceAndPath(
                cn.dancingsnow.neoecoprototype1201.Prototype1201.MOD_ID, "textures/gui/tier/l1.png");
    }

    /**
     * eco's default spells the tier number, and our tier index is 0, so without this the computation
     * panel labels every L1 cell "L0" - a tier that does not exist.
     */
    @Override
    public net.minecraft.network.chat.Component getPanelTierName() {
        return net.minecraft.network.chat.Component.literal("L1");
    }
}
