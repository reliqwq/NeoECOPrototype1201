package cn.dancingsnow.neoecoprototype1201.items;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IBasicECOCellItem;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A cell that grows something out of nothing and takes nothing in.
 *
 * <p>AE2's {@code StorageCell} has no tick method and the L1 drive has no ticker at all, so the bank is
 * never accumulated - it is <em>derived</em> from the world clock whenever somebody asks. Two numbers live
 * on the stack (the world tick it started from, and how much the network has drawn), everything else is
 * arithmetic, and a read never writes. That is what lets the same cell work mounted in our drive, in an
 * eco one, or lying in a chest.
 *
 * <p>The registered instance grows AE2's singularity; the sky-stone-dust member grows AE2's own sky dust;
 * the arseng-gated source member grows arseng's source key. Ported from the 1.21.1 line, where the two
 * bank numbers rode a data component - 1.20.1 has no data components, so the same two numbers ride the
 * stack's own NBT tag instead, and the codec below is kept for the netty path only.
 *
 * <p>This line carries no server config spec, so the three members run on the 1.21.1 shipped defaults
 * (one batch per 600 ticks, 120 per batch) as constants. A pack that wants different figures gets the
 * scripted constructor, which is what the matrix builder exists for anyway.
 */
public final class SimplifySingularityCellItem extends Item implements IBasicECOCellItem {

    /** The one thing the registered cell ever holds. */
    public static AEItemKey singularity() {
        return SingularityKey.SINGULARITY;
    }

    /** The one thing the meteorite-dust member of the family ever holds: AE2's own sky dust. */
    public static AEItemKey skyStoneDust() {
        return SkyStoneDustKey.SKY_STONE_DUST;
    }

    /**
     * Resolved on first use instead of in this class's initialiser. Our own item registration is what
     * loads this class, and AE2 binds its registry objects during its own RegisterEvent, so reading one
     * statically is a startup crash whenever the loader happens to run us first.
     */
    private static final class SingularityKey {
        private static final AEItemKey SINGULARITY = AEItemKey.of(AEItems.SINGULARITY);
    }

    /** The same lazy rule as {@link SingularityKey}, and the same reason. */
    private static final class SkyStoneDustKey {
        private static final AEItemKey SKY_STONE_DUST = AEItemKey.of(AEItems.SKY_DUST);
    }

    /** One type, one row. */
    public static final int TYPES = 1;

    /** Infinity in the byte column, like the other endless members of this family. */
    public static final long REPORTED_BYTES = Long.MAX_VALUE;

    public static final double IDLE_DRAIN = 1.0D;

    /** The 1.21.1 shipped defaults, as constants: one batch per 600 ticks, 120 per batch. */
    public static final long TICKS_PER_BATCH = 600L;
    public static final long AMOUNT_PER_BATCH = 120L;

    /**
     * Its own cell type rather than the shared item row: eco's storage host would otherwise read this as one
     * more ordinary matrix, and "1 / 1 types against an unbounded budget" says nothing a player can act on.
     * Scripted members of this family keep this type too, which puts them in the same "one row, no numbers"
     * place as the registered one regardless of whether they grow items, fluids or chemicals.
     */
    public static final ECOCellType CELL_TYPE = new ECOCellType(
            Component.translatable("eco_cell_type.neoecoprototype1201.singularity"), TYPES, false);

    /** The meteorite-dust member's own row, for the same reason as above. */
    public static final ECOCellType SKY_STONE_DUST_CELL_TYPE = new ECOCellType(
            Component.translatable("eco_cell_type.neoecoprototype1201.sky_stone_dust"), TYPES, false);

    /**
     * The source member's row. It is not eco's {@code neoecoae:source} type: that one belongs to the finite
     * matrices and reports a bounded byte budget, while this cell has none. Only registered while arseng is
     * present, like the item itself.
     */
    public static final ECOCellType SOURCE_CELL_TYPE = new ECOCellType(
            Component.translatable("eco_cell_type.neoecoprototype1201.source"), TYPES, false);

    /**
     * What a registered member of the family grows and which two batch figures it runs on. Suppliers rather
     * than values keeps the shape identical to the 1.21.1 record, where the figures were config reads that
     * had to be fresh; here they answer the constants, and a scripted member still carries its own.
     */
    public record SelfGrow(java.util.function.Supplier<AEKey> content,
                           java.util.function.LongSupplier ticksPerBatch,
                           java.util.function.LongSupplier amountPerBatch,
                           ECOCellType cellType) {
    }

    /** The cell that shipped first: AE2's singularity. */
    public static final SelfGrow SINGULARITY = new SelfGrow(
            SimplifySingularityCellItem::singularity,
            () -> TICKS_PER_BATCH, () -> AMOUNT_PER_BATCH, CELL_TYPE);

    /** 陨石粉, same defaults as the singularity member. */
    public static final SelfGrow SKY_STONE_DUST = new SelfGrow(
            SimplifySingularityCellItem::skyStoneDust,
            () -> TICKS_PER_BATCH, () -> AMOUNT_PER_BATCH, SKY_STONE_DUST_CELL_TYPE);

    /** Whether a panel row lookup is looking at any member of this family. */
    public static boolean isSelfGrowType(ECOCellType cellType) {
        return CELL_TYPE.equals(cellType)
                || SKY_STONE_DUST_CELL_TYPE.equals(cellType)
                || SOURCE_CELL_TYPE.equals(cellType);
    }

    /**
     * All of the cell's state. {@code startGameTime} is the world tick it began counting from; {@code drawn}
     * is how many of its own content the network has already taken out of it. On this line the two numbers
     * ride the stack's own NBT tag - 1.20.1 has no data components.
     */
    public record Bank(long startGameTime, long drawn) {
    }

    /** The bank read off the stack's NBT, or null while it has never been stamped. */
    public static Bank bankOf(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("start_game_time")) {
            return null;
        }
        return new Bank(tag.getLong("start_game_time"), tag.getLong("drawn"));
    }

    /** Writes the bank into the stack's NBT, creating the tag if this is the first stamp. */
    public static void writeBank(ItemStack stack, Bank bank) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong("start_game_time", bank.startGameTime());
        tag.putLong("drawn", bank.drawn());
    }

    /**
     * What this instance grows and how fast, or null on the registered one.
     *
     * <p>The content is a supplier whose first read is kept, because a script builds this during the item
     * registration event and registry-backed keys are not readable at that moment. Everything that asks what
     * a cell grows - a drive, a tooltip, Jade - runs after the freeze.
     */
    public static final class Growth {
        private final java.util.function.Supplier<AEKey> content;
        @Nullable
        private AEKey resolved;
        private final long perBatch;
        private final long ticksPerBatch;

        public Growth(java.util.function.Supplier<AEKey> content, long perBatch, long ticksPerBatch) {
            if (content == null) {
                throw new IllegalArgumentException("A growing cell has to say what it grows");
            }
            if (perBatch < 1L || ticksPerBatch < 1L) {
                throw new IllegalArgumentException("A growing cell's batch and interval must be positive: "
                        + perBatch + "/" + ticksPerBatch);
            }
            this.content = content;
            this.perBatch = perBatch;
            this.ticksPerBatch = ticksPerBatch;
        }

        /** How much of {@link #content()} is ready for a bank that started at {@code startGameTime}. */
        public long readyAt(long startGameTime, long drawn, long nowGameTime) {
            return bankOf(new Bank(startGameTime, drawn), nowGameTime, perBatch, ticksPerBatch);
        }

        /** Whether this cell hands out {@code what} at all. */
        public boolean pays(AEKey what) {
            return content().equals(what);
        }

        /** The one thing this cell ever holds, read on first use and then kept. */
        public AEKey content() {
            AEKey own = resolved;
            if (own == null) {
                own = java.util.Objects.requireNonNull(content.get(),
                        "a growing cell's bound key resolved to nothing");
                resolved = own;
            }
            return own;
        }

        /** How much the bank jumps per batch. */
        public long perBatch() {
            return perBatch;
        }

        /** How long between jumps, in world ticks. */
        public long ticksPerBatch() {
            return ticksPerBatch;
        }
    }

    @Nullable
    private final Growth growth;
    private final SelfGrow profile;

    /** The registered cell: it grows AE2's singularity. */
    public SimplifySingularityCellItem(Item.Properties properties) {
        this(properties, SINGULARITY);
    }

    /** A registered member that grows something else. */
    public SimplifySingularityCellItem(Item.Properties properties, SelfGrow profile) {
        super(properties);
        this.growth = null;
        this.profile = profile;
    }

    /**
     * A scripted sibling: it grows {@code content} instead of a singularity, jumping {@code perBatch} at a
     * time every {@code ticksPerBatch} world ticks. Both figures are required, because a half-set cell would
     * slow down whenever somebody edits the server config, which is not what a pack asked for.
     */
    public SimplifySingularityCellItem(Item.Properties properties,
                                       java.util.function.Supplier<AEKey> content,
                                       long perBatch, long ticksPerBatch) {
        super(properties);
        this.growth = new Growth(content, perBatch, ticksPerBatch);
        this.profile = SINGULARITY;
    }

    /** Its own figures if a pack wrote them, otherwise its member's - the constants here. */
    public Growth growth() {
        return growth != null
                ? growth
                : new Growth(profile.content(), profile.amountPerBatch().getAsLong(),
                        profile.ticksPerBatch().getAsLong());
    }

    /** The panel row this cell's family owns; a pack's member shares the registered one's row. */
    public ECOCellType cellType() {
        return profile.cellType();
    }

    /** What this cell hands out, and the only thing it will ever hold. */
    public AEKey content() {
        return growth().content();
    }

    /** How much the bank jumps per batch. */
    public long perBatch() {
        return growth().perBatch();
    }

    /** How long between jumps, in world ticks. */
    public long ticksPerBatch() {
        return growth().ticksPerBatch();
    }

    /** Whether this cell was written by a pack rather than registered by us. */
    public boolean isScripted() {
        return growth != null;
    }

    /**
     * How many are ready at {@code nowGameTime} on the registered cell. There is no bank ceiling - the
     * cumulative total saturates instead of wrapping. A batch is what the bank jumps by; it does not drip.
     */
    public static long bankOf(Bank bank, long nowGameTime) {
        return bankOf(bank, nowGameTime, AMOUNT_PER_BATCH, TICKS_PER_BATCH);
    }

    /** The same arithmetic with a cell's own figures, which is what a scripted sibling runs on. */
    public static long bankOf(Bank bank, long nowGameTime, long batch, long interval) {
        long batches = Math.max(0L, (nowGameTime - bank.startGameTime()) / interval);
        long produced = batches > Long.MAX_VALUE / batch ? Long.MAX_VALUE : batches * batch;
        long stored = produced - bank.drawn;
        return stored <= 0L ? 0L : stored;
    }

    /** How many are ready on this cell, with its own batch and interval. */
    public long readyAt(Bank bank, long nowGameTime) {
        Growth own = growth();
        return own.readyAt(bank.startGameTime(), bank.drawn(), nowGameTime);
    }

    /**
     * How many are ready on the stack in hand, or -1 while it has never been stamped. One rule for both
     * places that display it: the item's own hover text and Jade's drive panel.
     */
    public static long stockOf(ItemStack stack, long nowGameTime) {
        Bank bank = bankOf(stack);
        if (bank == null) {
            return -1L;
        }
        return stack.getItem() instanceof SimplifySingularityCellItem cell
                ? cell.readyAt(bank, nowGameTime)
                : bankOf(bank, nowGameTime);
    }

    /**
     * The cell needs one world tick to count from, and there is exactly one moment that happens: the first
     * time a server-side reader asks the mounted cell what it holds ({@link SingularityCellHandler.Cell}).
     *
     * <p>{@code mayWrite} is decided by the caller from the *level*, not from the dist: in single-player the
     * integrated server runs inside the client's dist, so a dist check there would switch off legitimate
     * stamping along with the unwanted kind - a tooltip or item-list read that walks the cell's statistics
     * stamps *its own copy* of the stack, and that write never reaches the server.
     */
    public static Bank stampIfNeeded(ItemStack stack, long nowGameTime, boolean mayWrite) {
        Bank bank = bankOf(stack);
        if (bank != null) {
            return bank;
        }
        if (nowGameTime < 0L || !mayWrite) {
            return null;
        }
        Bank fresh = new Bank(nowGameTime, 0L);
        writeBank(stack, fresh);
        return fresh;
    }

    @Override
    public IECOTier getTier() {
        return SimplifyTier.L1;
    }

    @Override
    public AEKeyType getKeyType() {
        return content().getType();
    }

    @Override
    public long getBytes() {
        return REPORTED_BYTES;
    }

    @Override
    public int getBytesPerType() {
        return 1;
    }

    @Override
    public int getTotalTypes() {
        return TYPES;
    }

    @Override
    public double getIdleDrain() {
        return IDLE_DRAIN;
    }

    @Override
    public ECOCellType getCellType() {
        return cellType();
    }

    // Nothing to partition and nothing to fuzz-match: it holds the one type it grew itself. AE2's default
    // isEditable dereferences getConfigInventory() without a null check, so the workbench stays off on purpose.
    @Override
    public boolean isEditable(ItemStack stack) {
        return false;
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack stack) {
        return null;
    }

    @Override
    public appeng.api.config.FuzzyMode getFuzzyMode(ItemStack stack) {
        return appeng.api.config.FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack stack, appeng.api.config.FuzzyMode mode) {
        // unsupported: one fixed type
    }

    /**
     * Our own stock line. eco's byte line never runs for this cell: its tooltip starts with
     * {@code stack.getItem() instanceof ECOStorageCellItem}, and this item is deliberately not one - the
     * same reason the infinite concrete matrix shows no byte figure either. No clock, or a cell that has
     * never been stamped, means no number to claim - the line stays off rather than guessing.
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level,
                                List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        long now = SingularityCellHandler.clockFor(level);
        long stock = now < 0L ? -1L : stockOf(stack, now);
        if (stock < 0L) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.neoecoprototype1201.singularity_stock",
                appeng.core.localization.Tooltips.ofNumber(stock), stockSubject())
                .withStyle(net.minecraft.ChatFormatting.AQUA));
    }

    /** What the stock line names as its content; both printing places take it from the cell itself. */
    public Component stockSubject() {
        return content().getDisplayName();
    }

    /**
     * The preview reads the same arithmetic the network does, and never stamps: this runs on the client too,
     * and a client-side write to the stack would not reach the server. An unstamped cell previews empty.
     */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!AEConfig.instance().isTooltipShowCellContent()) {
            return Optional.empty();
        }
        Bank bank = bankOf(stack);
        long now = SingularityCellHandler.serverGameTime();
        if (bank == null || now < 0L) {
            return Optional.empty();
        }
        long ready = readyAt(bank, now);
        List<GenericStack> content = ready > 0L
                ? List.of(new GenericStack(content(), ready))
                : List.of();
        return Optional.of(new StorageCellTooltipComponent(List.of(), content, false, true));
    }
}
