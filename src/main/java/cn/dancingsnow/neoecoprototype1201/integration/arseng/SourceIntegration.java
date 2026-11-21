package cn.dancingsnow.neoecoprototype1201.integration.arseng;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.integration.Integration;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoprototype1201.items.SimplifySingularityCellItem;
import gripe._90.arseng.me.key.SourceKey;
import gripe._90.arseng.me.key.SourceKeyType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional Ars Nouveau integration: the source medium at L1, gated by eco's {@link Integration}
 * machinery exactly like the omni families ({@code @Integration("arseng")}, constructed only while
 * arseng is loaded, so arseng's classes never resolve on a pack without it).
 *
 * <p>The mod that actually carries the medium is Ars Energistique ({@code arseng}), not Ars Nouveau: it
 * registers the {@code arseng:source} AE2 key type and eco hangs its {@code neoecoae:source} cell type off
 * that same key. Ars Nouveau is arseng's own hard dependency, so it is present whenever this runs.
 *
 * <p>eco ships source cells at L4/L6/L9 only, and our L1 storage host refuses a cell above its own tier -
 * so without this family an Ars player has no way to put source into our storage at all. The five rungs
 * mirror the item and fluid ladders byte for byte (256 types, 1/256 of the capacity each). The type
 * stripe reads {@code cell_type_lower}'s last rail, which is free on this line - FE stops at y9-12.
 */
@Integration("arseng")
public final class SourceIntegration {

    /** The same type budget every L1 medium carries. */
    private static final int TOTAL_TYPES = 256;

    public static final List<RegistryObject<Item>> TAB_ITEMS = new ArrayList<>();
    /** The five cells only, in {@code SOURCE_RUNGS} order, for the client's model ladder and tint list. */
    public static final List<RegistryObject<Item>> CELLS = new ArrayList<>();

    // The item ladder's own byte figures, copied from the 1.21.1 line: the five rungs mirror the item
    // and fluid ladders byte for byte, 256 types at 1/256 of the capacity each.
    private static final long[] RUNG_BYTES = {
            1L << 10, 1L << 14, 1L << 16, 1L << 20, 1L << 22,
    };
    private static final String[] RUNG_NAMES = {"1k", "16k", "64k", "1m", "4m"};

    /** The one thing the growing cell holds: arseng's singleton source key. */
    private static AEKey sourceKey() {
        return SourceKey.KEY;
    }

    /** arseng's own key type: 1000 source per AE byte, the ratio its own source cells are built on. */
    private static AEKeyType sourceKeyType() {
        return SourceKeyType.TYPE;
    }

    /** eco's own source cell type, registered only while arseng is present - this family's gate too. */
    private static ECOCellType sourceCellType() {
        return NERegistries.CELL_TYPE.get(new ResourceLocation("neoecoae", "source"));
    }

    /** The self-growing member's profile, on the same batch figures as the singularity cell. */
    public static final SimplifySingularityCellItem.SelfGrow SOURCE =
            new SimplifySingularityCellItem.SelfGrow(
                    SourceIntegration::sourceKey,
                    () -> SimplifySingularityCellItem.TICKS_PER_BATCH,
                    () -> SimplifySingularityCellItem.AMOUNT_PER_BATCH,
                    SimplifySingularityCellItem.SOURCE_CELL_TYPE);

    public void apply() {
        register(Registration.ITEMS);
    }

    private void register(DeferredRegister<Item> items) {
        TAB_ITEMS.add(items.register("simplify_source_storage_matrix_housing",
                () -> new Item(new Item.Properties())));
        TAB_ITEMS.add(items.register("simplify_source_growing_cell",
                () -> new SimplifySingularityCellItem(new Item.Properties().stacksTo(1), SOURCE)));
        for (int i = 0; i < RUNG_NAMES.length; i++) {
            final long bytes = RUNG_BYTES[i];
            final int types = TOTAL_TYPES;
            TAB_ITEMS.add(addCell(items, "simplify_source_storage_cell_" + RUNG_NAMES[i],
                    () -> new cn.dancingsnow.neoecoprototype1201.PrototypeCellItem(
                            new Item.Properties().stacksTo(1), sourceKeyType(),
                            SourceIntegration::sourceCellType, bytes, types)));
        }
    }

    private static RegistryObject<Item> addCell(DeferredRegister<Item> items, String id,
                                                java.util.function.Supplier<Item> factory) {
        RegistryObject<Item> cell = items.register(id, factory);
        CELLS.add(cell);
        return cell;
    }
}
