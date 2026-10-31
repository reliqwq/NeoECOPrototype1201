package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype1201.items.SimplifyComputationCellItem;
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
 * The prototype item-cell family: one host panel row plus the five capacity steps the 1.21.1 line
 * uses, so the whole registration path is exercised before any multiblock is ported.
 *
 * <p>eco builds the storage host's type rows by enumerating {@code neoecoae:cell_type}. Every row
 * registered in this project shows up in the host panel; the visibility flag this record carries in
 * eco 20.5.3 stays at its default, so no explicit choice is made here.
 */
public final class Registration {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Prototype1201.MOD_ID);
    public static final DeferredRegister<ECOCellType> CELL_TYPES =
            DeferredRegister.create(NERegistries.Keys.CELL_TYPE, Prototype1201.MOD_ID);
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

    /** Same ladder as the 1.21.1 L1 cells. */
    private static final String[] SUFFIXES = {"1k", "16k", "64k", "1m", "4m"};
    private static final long[] BYTES = {1L << 10, 1L << 14, 1L << 16, 1L << 20, 1L << 22};

    /** Our own host panel row, shared by the whole family. */
    public static final RegistryObject<ECOCellType> ROW = CELL_TYPES.register("prototype",
            () -> ECOCellType.builder()
                    .desc(Component.translatable("cell_type." + Prototype1201.MOD_ID + ".prototype"))
                    .typeCount(PrototypeCellItem.TOTAL_TYPES)
                    .build());

    public static final List<RegistryObject<Item>> CELLS = new ArrayList<>();

    static {
        for (int i = 0; i < SUFFIXES.length; i++) {
            final long bytes = BYTES[i];
            CELLS.add(ITEMS.register("prototype_item_cell_" + SUFFIXES[i],
                    () -> new PrototypeCellItem(new Item.Properties().stacksTo(1), ROW::get, bytes)));
        }
    }

    /** Computation cells live in the drive, so they carry a tier instead of a storage type row. */
    public static final RegistryObject<SimplifyComputationCellItem> SIMPLIFY_COMPUTATION_CELL_1M =
            ITEMS.register("simplify_computation_cell_1m",
                    () -> new SimplifyComputationCellItem(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<SimplifyComputationCellItem> ENERGIZED_COMPUTATION_CELL_4M =
            ITEMS.register("energized_computation_cell_4m",
                    () -> new SimplifyComputationCellItem(
                            new Item.Properties().stacksTo(8), SimplifyTier.L1_REINFORCED));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Prototype1201.MOD_ID + ".main"))
                    .icon(() -> CELLS.get(0).get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        ModRegistration.TAB_BLOCK_ITEMS
                                .forEach(item -> output.accept(item.get()));
                        CELLS.forEach(cell -> output.accept(cell.get()));
                        output.accept(SIMPLIFY_COMPUTATION_CELL_1M.get());
                        output.accept(ENERGIZED_COMPUTATION_CELL_4M.get());
                    })
                    .build());

    private Registration() {
    }
}
