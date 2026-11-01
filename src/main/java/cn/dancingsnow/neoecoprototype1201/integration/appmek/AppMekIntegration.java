package cn.dancingsnow.neoecoprototype1201.integration.appmek;

import cn.dancingsnow.neoecoprototype1201.PrototypeCellItem;
import cn.dancingsnow.neoecoprototype1201.Registration;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The chemical matrix family, registered only when Applied Mekanistics is loaded.
 *
 * <p>Like the fluid family this rides an existing eco panel row - appmek itself creates
 * {@code neoecoae:mekanism} through eco's registrate, which is what eco's own chemical cells file under -
 * so the host panel keeps one line for chemicals instead of splitting it by which mod added the disc.
 *
 * <p>The caller must check {@code ModList.isLoaded("appmek")} in its own method: loading this class does
 * not resolve {@link ChemicalKeyType}, and only that class names the third party. {@link #TAB_ITEMS} is
 * empty when the family was never registered, which is how the creative tab stays correct either way.
 */
public final class AppMekIntegration {
    public static final List<RegistryObject<Item>> TAB_ITEMS = new ArrayList<>();
    /** The five cells only, in {@code Registration.CELL_SIZES} order, for the client's model ladder. */
    public static final List<RegistryObject<Item>> CELLS = new ArrayList<>();

    public static void register(DeferredRegister<Item> items) {
        TAB_ITEMS.add(items.register("simplify_chemical_storage_matrix_housing",
                () -> new Item(new Item.Properties())));
        for (int i = 0; i < Registration.CELL_SIZES.length; i++) {
            final long bytes = Registration.CELL_BYTES[i];
            RegistryObject<Item> cell = items.register(
                    "simplify_chemical_storage_cell_" + Registration.CELL_SIZES[i],
                    () -> new PrototypeCellItem(new Item.Properties().stacksTo(1),
                            ChemicalKeyType.get(), () -> Registration.ecoCellType("mekanism"), bytes));
            TAB_ITEMS.add(cell);
            CELLS.add(cell);
        }
    }

    private AppMekIntegration() {
    }
}
