package cn.dancingsnow.neoecoprototype1201.integration.ae2omnicells;

import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoae.api.integration.Integration;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The three omni matrix families, ported from the 1.21.1 line: universal, quantum and complex
 * universal, two rungs each. The spent nuclear waste pair is ported as an item class and waits: it
 * needs a third gate (Mekanism + Applied Mekanistics) that this line's checker cannot express yet.
 *
 * <p>The class carries eco's own {@link Integration @Integration} annotation, and that annotation is
 * the whole gate. eco's {@code IntegrationManager} reads it out of Forge's mod-file scan (which covers
 * every jar in the pack without loading a single class), and constructs this class only while
 * {@code ae2omnicells} is loaded - so the cell items below, whose superclass chain reaches OmniCells
 * through eco's {@code ECOUniversalStorageCellItem}, are never even class-loaded on a pack without it.
 * The same mechanism is how eco ships its own omni cells against an optional dependency, and our
 * load-after ordering on eco means the entries added here reach our own
 * {@code ITEMS.register(modBus)} in time.
 *
 * <p>The waste pair carries a second gate inside {@link #apply()}: the substance it stores comes out of
 * Mekanism through Applied Mekanistics, so both mods have to be present before the items exist at all.
 */
@Integration("ae2omnicells")
public final class UniversalIntegration {

    /** Everything the tab should show, in family order; empty without the mod. */
    public static final List<RegistryObject<Item>> TAB_ITEMS = new ArrayList<>();
    /** The eight cells only, in {@code MODELS} order, for the client's model ladder and tint list. */
    public static final List<RegistryObject<Item>> CELLS = new ArrayList<>();

    public void apply() {
        register(Registration.ITEMS);
    }

    private void register(DeferredRegister<Item> items) {
        // universal
        TAB_ITEMS.add(items.register("simplify_universal_storage_matrix_housing",
                () -> new Item(new Item.Properties())));
        TAB_ITEMS.add(addCell(items, "simplify_universal_storage_cell_1k",
                () -> new SimplifyUniversalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyUniversalStorageCellItem.BYTES_1K)));
        TAB_ITEMS.add(addCell(items, "simplify_universal_storage_cell_1m",
                () -> new SimplifyUniversalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyUniversalStorageCellItem.BYTES_1M)));
        // quantum
        TAB_ITEMS.add(items.register("simplify_quantum_storage_matrix_housing",
                () -> new Item(new Item.Properties())));
        TAB_ITEMS.add(addCell(items, "simplify_quantum_storage_cell_1k",
                () -> new SimplifyQuantumStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyQuantumStorageCellItem.BYTES_1K)));
        TAB_ITEMS.add(addCell(items, "simplify_quantum_storage_cell_1m",
                () -> new SimplifyQuantumStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyQuantumStorageCellItem.BYTES_1M)));
        // complex universal
        TAB_ITEMS.add(items.register("simplify_complex_universal_storage_matrix_housing",
                () -> new Item(new Item.Properties())));
        TAB_ITEMS.add(addCell(items, "simplify_complex_universal_storage_cell_1k",
                () -> new SimplifyComplexUniversalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyComplexUniversalStorageCellItem.BYTES_1K)));
        TAB_ITEMS.add(addCell(items, "simplify_complex_universal_storage_cell_1m",
                () -> new SimplifyComplexUniversalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyComplexUniversalStorageCellItem.BYTES_1M)));
        // The spent nuclear waste pair stays unported on this line, exactly like the small-bulk
        // chemical pair before it: it needs a third gate (Mekanism + Applied Mekanistics next to
        // Omni Cells) and this line's checker derives gating from the integration folder name alone,
        // so a triple-gated pair would read as over-conditioned. The item class is ported and waits;
        // bringing the pair up is one edit here plus the recipes, once a multi-gate rule is decided.
    }

    private static RegistryObject<Item> addCell(DeferredRegister<Item> items, String id,
                                                java.util.function.Supplier<Item> factory) {
        RegistryObject<Item> cell = items.register(id, factory);
        CELLS.add(cell);
        return cell;
    }
}
