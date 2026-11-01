package cn.dancingsnow.neoecoprototype1201.integration.appflux;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional AppliedFlux integration. This class is loaded only when appflux is present, and only its
 * caller's {@code ModList.isLoaded("appflux")} branch touches it: {@link SimplifyFeStorageCellItem}'s
 * superclass hands {@code FluxKeyType.TYPE} to eco inside its own constructor, which is a method-body
 * reference and therefore never resolved on a machine without the mod.
 *
 * <p>Our own FE housing, like every other medium in this family has one; the 1M and 4M cells are that
 * housing plus our own component, and the 1K one plus AE2's own 1K component, because we never drew a
 * component below 1M.
 *
 * <p>What is not here: the 1.21.1 line also lets appflux's induction card into our interfaces and its
 * pattern provider, by reading {@code AFSingletons.INDUCTION_CARD}. That needs appflux on the compile
 * classpath (a field read is a must-resolve reference, unlike the case above), and this line has no
 * pattern-provider or cable-part items to hand it to, so the allowance waits until one of those is true.
 */
public final class FeStorageIntegration {
    public static final List<RegistryObject<Item>> TAB_ITEMS = new ArrayList<>();
    /** The three cells only, in {@link #FE_SIZES} order, for the client's model ladder. */
    public static final List<RegistryObject<Item>> CELLS = new ArrayList<>();

    public static void register(DeferredRegister<Item> items) {
        TAB_ITEMS.add(items.register("simplify_fe_cell_housing", () -> new Item(new Item.Properties())));
        for (int i = 0; i < FE_RUNGS.length; i++) {
            final long bytes = FE_RUNGS[i];
            RegistryObject<Item> cell = items.register("simplify_fe_storage_cell_" + FE_SIZES[i],
                    () -> new SimplifyFeStorageCellItem(new Item.Properties().stacksTo(1),
                            SimplifyFeStorageCellItem::getFluxCellType, bytes));
            TAB_ITEMS.add(cell);
            CELLS.add(cell);
        }
    }

    /** The three rungs the 1.21.1 line ships: AE2's 1K step, then appflux's own 1M and 4M. */
    private static final String[] FE_SIZES = {"1k", "1m", "4m"};
    private static final long[] FE_RUNGS = {
            SimplifyFeStorageCellItem.BYTES_1K,
            SimplifyFeStorageCellItem.BYTES_1M,
            SimplifyFeStorageCellItem.BYTES_4M};

    private FeStorageIntegration() {
    }
}
