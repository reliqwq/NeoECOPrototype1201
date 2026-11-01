package cn.dancingsnow.neoecoprototype1201;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype1201.items.SimplifyComputationCellItem;
import cn.dancingsnow.neoecoprototype1201.items.SimplifyConcreteStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The L1 cell families: the five capacity steps the 1.21.1 line uses, then the mediums that came after.
 *
 * <p>eco builds the storage host's type rows by enumerating {@code neoecoae:cell_type}, and every cell here
 * rides one of eco's own rows ({@code items} / {@code fluids} / {@code mekanism} / {@code flux}) rather than
 * registering a row of our own - except the infinite matrix, whose type is deliberately unregistered. What
 * a row decides is the word the panel prints for the medium, and on this line its {@code typeCount} is also
 * what {@code getTotalTypes()} would answer, which is why the capacity ceiling stays with
 * {@link PrototypeCellItem} instead.
 */
public final class Registration {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Prototype1201.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Prototype1201.MOD_ID);
    /**
     * The L1 processor assembler's own recipe channel. Both registers follow AE2's and eco's shape on this
     * line: a {@code RecipeType} is a DeferredRegister entry whose supplier returns an anonymous type that
     * answers {@code toString()} with its own id (see {@code appeng.recipes.AERecipeTypes#register}).
     */
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Prototype1201.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Prototype1201.MOD_ID);

    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeType<
            cn.dancingsnow.neoecoprototype1201.recipe.ProcessorAssemblerRecipe>> PROCESSOR_ASSEMBLER_TYPE =
            RECIPE_TYPES.register("processor_assembler",
                    () -> new net.minecraft.world.item.crafting.RecipeType<>() {
                        @Override
                        public String toString() {
                            return Prototype1201.MOD_ID + ":processor_assembler";
                        }
                    });
    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeSerializer<?>>
            PROCESSOR_ASSEMBLER_SERIALIZER = RECIPE_SERIALIZERS.register("processor_assembler",
                    cn.dancingsnow.neoecoprototype1201.recipe.ProcessorAssemblerRecipe.Serializer::new);

    /**
     * The assembler's menu type. AE2's {@code MenuTypeBuilder} can only name entries {@code ae2:} on this
     * line - it has no {@code buildUnregistered} - so the type goes through Forge's own factory instead, and
     * the two wire values AE2's builder writes are read by {@link
     * cn.dancingsnow.neoecoprototype1201.menu.ProcessorAssemblerMenu#fromNetwork}.
     */
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS =
            DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.Keys.MENU_TYPES,
                    Prototype1201.MOD_ID);
    public static final RegistryObject<net.minecraft.world.inventory.MenuType<
            cn.dancingsnow.neoecoprototype1201.menu.ProcessorAssemblerMenu>> PROCESSOR_ASSEMBLER_MENU =
            MENUS.register("processor_assembler", () -> net.minecraftforge.common.extensions.IForgeMenuType
                    .create(cn.dancingsnow.neoecoprototype1201.menu.ProcessorAssemblerMenu::fromNetwork));

    /**
     * Same ladder as the 1.21.1 L1 cells. Public because the chemical family builds the same five steps
     * from it, in a class that only runs when Applied Mekanistics is loaded.
     */
    public static final String[] CELL_SIZES = {"1k", "16k", "64k", "1m", "4m"};
    public static final long[] CELL_BYTES = {1L << 10, 1L << 14, 1L << 16, 1L << 20, 1L << 22};

    /**
     * No row of our own: every item-medium cell here rides eco's {@code neoecoae:items}, which is what the
     * 1.21.1 line does and what makes the host panel read "物品" instead of a word only this addon knows.
     * The capacity ceiling is not borrowed with the row - see {@link PrototypeCellItem#TOTAL_TYPES}.
     */
    public static final List<RegistryObject<Item>> CELLS = new ArrayList<>();

    static {
        for (int i = 0; i < CELL_SIZES.length; i++) {
            final long bytes = CELL_BYTES[i];
            CELLS.add(ITEMS.register("prototype_item_cell_" + CELL_SIZES[i],
                    () -> new PrototypeCellItem(new Item.Properties().stacksTo(1),
                            () -> ecoCellType("items"), bytes)));
        }
    }

    /**
     * Looks up one of eco's own host-panel rows. The fluid family does not add a row of its: eco ships
     * {@code neoecoae:fluids} and its drive groups cells by whichever row they name, so a second row would
     * split one medium across two lines in the panel. Callers hold this in a {@link java.util.function.Supplier}
     * because the registry itself is only created at {@code NewRegistryEvent}.
     */
    public static ECOCellType ecoCellType(String path) {
        ECOCellType type = NERegistries.CELL_TYPE
                .get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoecoae", path));
        if (type == null) {
            throw new IllegalStateException("Missing eco cell type: " + path);
        }
        return type;
    }

    /**
     * The item family's own housing, so its five rungs assemble the same way the fluid, chemical and FE
     * families do - and so taking a matrix apart hands back the housing plus the component it was built
     * with. The 1M and 4M rungs use our own components because AE2 stops at 256k.
     */
    public static final RegistryObject<Item> SIMPLIFY_ITEM_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_item_storage_matrix_housing", () -> new Item(new Item.Properties()));

    /** The fluid family: one housing plus the same five capacity steps as the item cells. */
    public static final RegistryObject<Item> SIMPLIFY_FLUID_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_fluid_storage_matrix_housing", () -> new Item(new Item.Properties()));

    /**
     * The two capacity steps AE2 has no component for - it stops at 256k - so both cell families' top tiers
     * are assembled from these. Registered as plain items: nothing reads them beyond the recipes.
     */
    public static final RegistryObject<Item> SIMPLIFY_STORAGE_COMPONENT_1M =
            ITEMS.register("simplify_storage_component_1m", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SIMPLIFY_STORAGE_COMPONENT_4M =
            ITEMS.register("simplify_storage_component_4m", () -> new Item(new Item.Properties()));

    public static final List<RegistryObject<Item>> FLUID_CELLS = new ArrayList<>();

    static {
        for (int i = 0; i < CELL_SIZES.length; i++) {
            final long bytes = CELL_BYTES[i];
            FLUID_CELLS.add(ITEMS.register("simplify_fluid_storage_cell_" + CELL_SIZES[i],
                    () -> new PrototypeCellItem(new Item.Properties().stacksTo(1),
                            AEKeyType.fluids(), () -> ecoCellType("fluids"), bytes)));
        }
    }

    /** The pigmee easter-egg housing; see its own class for what 1.20.1 cannot carry across. */
    public static final RegistryObject<Item> PIGMEE_STORAGE_MATRIX_HOUSING =
            ITEMS.register("pigmee_storage_matrix_housing",
                    () -> new cn.dancingsnow.neoecoprototype1201.items.PigmeeStorageMatrixHousingItem(
                            new Item.Properties()));

    /** A 2,560-byte item cell on the same panel row as the rest of the item family. */
    public static final RegistryObject<Item> PIGMEE_STORAGE_CELL =
            ITEMS.register("pigmee_storage_cell",
                    () -> new PrototypeCellItem(new Item.Properties().stacksTo(1),
                            () -> ecoCellType("items"), 2_560L));

    /**
     * The infinite concrete matrix. Its cell names a cell type of its own rather than {@link #ROW}, and
     * that type is deliberately not registered - see {@code SimplifyConcreteStorageCellItem.CELL_TYPE} for
     * what makes an unregistered row safe on eco 20.5.3.
     */
    public static final RegistryObject<Item> SIMPLIFY_CONCRETE_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_concrete_storage_matrix_housing", () -> new Item(new Item.Properties()));

    public static final RegistryObject<SimplifyConcreteStorageCellItem> SIMPLIFY_CONCRETE_STORAGE_CELL =
            ITEMS.register("simplify_concrete_storage_cell",
                    () -> new SimplifyConcreteStorageCellItem(new Item.Properties().stacksTo(1)));

    /** Computation cells live in the drive, so they carry a tier instead of a storage type row. */
    public static final RegistryObject<SimplifyComputationCellItem> SIMPLIFY_COMPUTATION_CELL_1M =
            ITEMS.register("simplify_computation_cell_1m",
                    () -> new SimplifyComputationCellItem(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<SimplifyComputationCellItem> ENERGIZED_COMPUTATION_CELL_4M =
            ITEMS.register("energized_computation_cell_4m",
                    () -> new SimplifyComputationCellItem(
                            new Item.Properties().stacksTo(8), SimplifyTier.L1_REINFORCED));

    /**
     * The L1 core material the assembler and the green casing are built from; its name carries the family's
     * green, as on the 1.21.1 line.
     */
    public static final RegistryObject<Item> GREEN_CRYSTAL_MATRIX =
            ITEMS.register("simplify_green_crystal_matrix",
                    () -> new cn.dancingsnow.neoecoprototype1201.items.GreenCrystalMatrixItem(
                            new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Prototype1201.MOD_ID + ".main"))
                    .icon(() -> CELLS.get(0).get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        ModRegistration.TAB_BLOCK_ITEMS
                                .forEach(item -> output.accept(item.get()));
                        CELLS.forEach(cell -> output.accept(cell.get()));
                        output.accept(SIMPLIFY_ITEM_STORAGE_MATRIX_HOUSING.get());
                        output.accept(SIMPLIFY_FLUID_STORAGE_MATRIX_HOUSING.get());
                        output.accept(SIMPLIFY_STORAGE_COMPONENT_1M.get());
                        output.accept(SIMPLIFY_STORAGE_COMPONENT_4M.get());
                        FLUID_CELLS.forEach(cell -> output.accept(cell.get()));
                        cn.dancingsnow.neoecoprototype1201.integration.appmek.AppMekIntegration.TAB_ITEMS
                                .forEach(item -> output.accept(item.get()));
                        // Empty unless the appflux branch of the mod constructor filled it.
                        cn.dancingsnow.neoecoprototype1201.integration.appflux.FeStorageIntegration.TAB_ITEMS
                                .forEach(item -> output.accept(item.get()));
                        output.accept(PIGMEE_STORAGE_MATRIX_HOUSING.get());
                        output.accept(PIGMEE_STORAGE_CELL.get());
                        output.accept(SIMPLIFY_CONCRETE_STORAGE_MATRIX_HOUSING.get());
                        output.accept(SIMPLIFY_CONCRETE_STORAGE_CELL.get());
                        output.accept(SIMPLIFY_COMPUTATION_CELL_1M.get());
                        output.accept(ENERGIZED_COMPUTATION_CELL_4M.get());
                        output.accept(GREEN_CRYSTAL_MATRIX.get());
                    })
                    .build());

    private Registration() {
    }
}
