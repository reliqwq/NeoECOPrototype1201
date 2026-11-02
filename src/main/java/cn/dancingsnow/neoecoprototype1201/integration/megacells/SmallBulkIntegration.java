package cn.dancingsnow.neoecoprototype1201.integration.megacells;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoprototype1201.items.SimplifySmallBulkCellItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The small-bulk (小宗) family: L1 cells with no byte ceiling and only a handful of marked types.
 *
 * <p>The whole family registers or does not: the cell types it names - {@code neoecoae:mega_item} and
 * {@code neoecoae:mega_fluid} - are registered by eco's MEGA Cells integration, so a pack without that mod
 * has no rows to point at and {@link Registration#ecoCellType} would throw the moment the registry event
 * asked an item for its row. The caller in {@link Prototype1201} holds the
 * {@code ModList.isLoaded("megacells")} check, which is the shape this line already uses for the chemical
 * and FE families.</p>
 *
 * <p>Loading this class is safe either way: everything it names is Minecraft, AE2 or eco - all hard
 * dependencies - and no MEGA Cells type appears in any signature or field here. Nothing about the family
 * needs a MEGA class at all: the items are ours, the cell types come from eco's registry by id, and the
 * MEGA items appear only in recipe JSONs, which the loader skips because every recipe in this family
 * carries a {@code forge:mod_loaded} condition.</p>
 *
 * <p>What is not here yet: the chemical pair. It needs appmek's chemical {@code AEKeyType} as well as the
 * {@code mega_chemical} row, so it carries a second gate - and this folder's name is how the data guard
 * learns which optional mod gates an id, so two gates need a rule of their own first.</p>
 */
public final class SmallBulkIntegration {
    /** Creative-tab entries, in the order the family's items are registered. */
    public static final List<RegistryObject<Item>> TAB_ITEMS = new ArrayList<>();

    /**
     * The four cells only, in the order {@code Prototype1201Client} pairs them with their rung models and in
     * the order they join the status-light tint. Empty without MEGA Cells, which is what lets both client
     * passes skip the family instead of testing a gate of their own.
     */
    public static final List<RegistryObject<Item>> CELLS = new ArrayList<>();

    /**
     * Registers the family into the line's item register. Called once, from the mod constructor, behind
     * the MEGA Cells gate - {@link DeferredRegister} is still open at that point, exactly as it is for
     * {@code AppMekIntegration.register} and {@code FeStorageIntegration.register}.
     *
     * <p>Every id is written as a literal in its own {@code items.register("…")} call, even though the four
     * cells differ only in two arguments. The data guard reads this folder to learn which ids are gated by
     * which optional mod ({@code integration_registers} greps {@code .register("literal"}), and an id handed
     * to a helper as a variable is invisible to it - the family would then be missing from the recipe
     * condition check and from the tag rule that knows these ids only exist with MEGA Cells loaded.</p>
     */
    public static void register(DeferredRegister<Item> items) {
        RegistryObject<Item> item = items.register("simplify_small_bulk_storage_cell",
                () -> new SimplifySmallBulkCellItem(new Item.Properties().stacksTo(1), AEKeyType.items(),
                        () -> Registration.ecoCellType("mega_item"),
                        SimplifySmallBulkCellItem.MARKED_TYPES));
        CELLS.add(item);
        TAB_ITEMS.add(item);
        RegistryObject<Item> itemExpanded = items.register("simplify_small_bulk_storage_cell_expanded",
                () -> new SimplifySmallBulkCellItem(new Item.Properties().stacksTo(1), AEKeyType.items(),
                        () -> Registration.ecoCellType("mega_item"),
                        SimplifySmallBulkCellItem.EXPANDED_MARKED_TYPES));
        CELLS.add(itemExpanded);
        TAB_ITEMS.add(itemExpanded);
        RegistryObject<Item> fluid = items.register("simplify_small_bulk_fluid_storage_cell",
                () -> new SimplifySmallBulkCellItem(new Item.Properties().stacksTo(1), AEKeyType.fluids(),
                        () -> Registration.ecoCellType("mega_fluid"),
                        SimplifySmallBulkCellItem.MARKED_TYPES));
        CELLS.add(fluid);
        TAB_ITEMS.add(fluid);
        RegistryObject<Item> fluidExpanded = items.register("simplify_small_bulk_fluid_storage_cell_expanded",
                () -> new SimplifySmallBulkCellItem(new Item.Properties().stacksTo(1), AEKeyType.fluids(),
                        () -> Registration.ecoCellType("mega_fluid"),
                        SimplifySmallBulkCellItem.EXPANDED_MARKED_TYPES));
        CELLS.add(fluidExpanded);
        TAB_ITEMS.add(fluidExpanded);

        TAB_ITEMS.add(items.register("simplify_small_bulk_expansion_card",
                () -> new Item(new Item.Properties())));
        TAB_ITEMS.add(items.register("simplify_small_bulk_storage_matrix_housing",
                () -> new Item(new Item.Properties())));
        TAB_ITEMS.add(items.register("simplify_small_bulk_fluid_storage_matrix_housing",
                () -> new Item(new Item.Properties())));
    }

    private SmallBulkIntegration() {
    }
}
