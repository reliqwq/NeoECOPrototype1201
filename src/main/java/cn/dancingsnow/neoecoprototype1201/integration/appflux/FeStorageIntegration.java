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
 * <p>What the card allowance below is: appflux mixes the induction card's behaviour into AE2's own
 * {@code InterfaceLogic} (and into its {@code PatternProviderLogic}), and both of our interfaces run
 * that class rather than a subclass of it, so the only thing missing was the allowance. The read of
 * {@code AFItemAndBlock.INDUCTION_CARD} lives in this class - which the mod constructor only enters
 * behind {@code isLoaded("appflux")} - because a field read is a must-resolve reference, unlike the
 * {@code FluxKeyType} reads above that sit inside method bodies. This line has no pattern provider and
 * no cable parts, so the provider half of the 1.21.1 call has nothing to hand the card to.
 */
public final class FeStorageIntegration {
    /** One card per machine, which is what appflux asks for on AE2's own interface. */
    private static final int INDUCTION_CARDS_PER_MACHINE = 1;

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

    /**
     * Lets appflux's induction card into our two interfaces. {@code Prototype1201} calls this from common
     * setup behind the same {@code isLoaded("appflux")} gate - after registries freeze, because the card
     * is a field appflux's own registry pass fills in - and next to the two AE2 cards it already hands
     * those items. AE2's interface group, for the tooltip reason spelled out there.
     */
    public static void registerUpgradeCards() {
        var card = com.glodblock.github.appflux.common.AFItemAndBlock.INDUCTION_CARD;
        var interfaceGroup = appeng.core.localization.GuiText.Interface.getTranslationKey();
        for (var item : java.util.List.<net.minecraft.world.level.ItemLike>of(
                cn.dancingsnow.neoecoprototype1201.registration.ModRegistration
                        .SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get().asItem(),
                cn.dancingsnow.neoecoprototype1201.registration.ModRegistration
                        .SUPERCONDUCTIVE_INTERFACE_BLOCK.get().asItem())) {
            appeng.api.upgrades.Upgrades.add(card, item, INDUCTION_CARDS_PER_MACHINE, interfaceGroup);
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
