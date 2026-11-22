package cn.dancingsnow.neoecoprototype1201;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype1201.items.SimplifySingularityCellItem;
import cn.dancingsnow.neoecoprototype1201.items.BackupDriveItem;
import cn.dancingsnow.neoecoprototype1201.items.Ae2ToL1UpgradeCardItem;
import cn.dancingsnow.neoecoprototype1201.registration.ModBlockEntities;
import cn.dancingsnow.neoecoprototype1201.items.SimplifyComputationCellItem;
import cn.dancingsnow.neoecoprototype1201.items.SimplifyConcreteStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.items.SimplifyMigrationDriveItem;
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
     * The L1 pattern provider's menu type, same route as the assembler's for the same reason: AE2's
     * {@code MenuTypeBuilder} on this line names entries inside its own namespace only.
     */
    public static final RegistryObject<net.minecraft.world.inventory.MenuType<
            cn.dancingsnow.neoecoprototype1201.menu.L1PatternProviderMenu>> L1_PATTERN_PROVIDER_MENU =
            MENUS.register("l1_pattern_provider", () -> net.minecraftforge.common.extensions.IForgeMenuType
                    .create(cn.dancingsnow.neoecoprototype1201.menu.L1PatternProviderMenu::fromNetwork));

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

    /**
     * Creative-only source listing every item this addon and eco register. No recipe and no survival
     * route on purpose: it exists to test drives, terminals and crafting setups without hoarding items.
     * The 1.20.1 shape of the cell has no config of its own to gate it - it ships unconditionally,
     * exactly like the 1.21.1 one does.
     */
    public static final RegistryObject<cn.dancingsnow.neoecoprototype1201.items.AllItemsCreativeCellItem>
            ALL_ITEMS_CREATIVE_CELL =
            ITEMS.register("all_items_creative_cell",
                    () -> new cn.dancingsnow.neoecoprototype1201.items.AllItemsCreativeCellItem(
                            new Item.Properties().stacksTo(1)));

    /**
     * Creative-only: right-click hands out a random batch of encoded patterns for this addon's and eco's
     * recipes, skipping the ones already in the inventory. See {@code PatternGeneratorItem}.
     */
    public static final RegistryObject<cn.dancingsnow.neoecoprototype1201.items.PatternGeneratorItem>
            PATTERN_GENERATOR =
            ITEMS.register("pattern_generator",
                    () -> new cn.dancingsnow.neoecoprototype1201.items.PatternGeneratorItem(
                            new Item.Properties().stacksTo(1)));

    /** Computation cells live in the drive, so they carry a tier instead of a storage type row. */
    public static final RegistryObject<SimplifyComputationCellItem> SIMPLIFY_COMPUTATION_CELL_1M =
            ITEMS.register("simplify_computation_cell_1m",
                    () -> new SimplifyComputationCellItem(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<SimplifyComputationCellItem> ENERGIZED_COMPUTATION_CELL_4M =
            ITEMS.register("energized_computation_cell_4m",
                    () -> new SimplifyComputationCellItem(
                            new Item.Properties().stacksTo(8), SimplifyTier.L1_REINFORCED));
    /**
     * The provider on one face of a cable bus. There is no part-type registry to call on this line
     * ({@code IPartHelper} is absent), so an {@code appeng.items.parts.PartItem} in our own item register is
     * the whole registration: an {@code IPartItem}'s network id is literally that item's id in the item
     * registry. The model names are a separate duty - see {@code PartModels.registerModels} in the client.
     */
    public static final RegistryObject<appeng.items.parts.PartItem<
            cn.dancingsnow.neoecoprototype1201.part.SimplifyPatternProviderPart>> CABLE_PATTERN_PROVIDER =
            ITEMS.register("cable_pattern_provider",
                    () -> new appeng.items.parts.PartItem<>(new Item.Properties(),
                            cn.dancingsnow.neoecoprototype1201.part.SimplifyPatternProviderPart.class,
                            cn.dancingsnow.neoecoprototype1201.part.SimplifyPatternProviderPart::new));

    /** The two cable-face interfaces. Their generation lives in the part (the same
     *  IPassiveEnergyGenerator service the block entities carry), and the upgrade cards' cable
     *  branch swaps AE2's own interface part for these in place. */
    public static final RegistryObject<appeng.items.parts.PartItem<
            cn.dancingsnow.neoecoprototype1201.part.PoweredInterfacePart>> CABLE_POWERED_ME_INTERFACE_PART =
            ITEMS.register("cable_powered_me_interface",
                    () -> new appeng.items.parts.PartItem<>(new Item.Properties(),
                            cn.dancingsnow.neoecoprototype1201.part.PoweredInterfacePart.class,
                            cn.dancingsnow.neoecoprototype1201.part.PoweredInterfacePart::new));
    public static final RegistryObject<appeng.items.parts.PartItem<
            cn.dancingsnow.neoecoprototype1201.part.SuperconductiveInterfacePart>> CABLE_SUPERCONDUCTIVE_INTERFACE_PART =
            ITEMS.register("cable_superconductive_interface",
                    () -> new appeng.items.parts.PartItem<>(new Item.Properties(),
                            cn.dancingsnow.neoecoprototype1201.part.SuperconductiveInterfacePart.class,
                            cn.dancingsnow.neoecoprototype1201.part.SuperconductiveInterfacePart::new));

    public static final RegistryObject<Item> GRETTE_CRYSTAL =
            ITEMS.register("grette_crystal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GRETTE_CRYSTAL_DUST =
            ITEMS.register("grette_crystal_dust", () -> new Item(new Item.Properties()));

    /**
     * The L1 core material the assembler and the green casing are built from; its name carries the family's
     * green, as on the 1.21.1 line.
     */
    public static final RegistryObject<Item> GREEN_CRYSTAL_MATRIX =
            ITEMS.register("simplify_green_crystal_matrix",
                    () -> new cn.dancingsnow.neoecoprototype1201.items.GreenCrystalMatrixItem(
                            new Item.Properties()));

    /**
     * The four rungs of the migration drive plus the shell it returns as. A drive takes one finished, full
     * length host up a rung in place and hands every block it replaced back to the player, so the disk's own
     * recipe would be the whole price - and this line ships no recipe for any of them, deliberately: the
     * 1.21.1 line has not priced them either, and a paid-for disk whose price is a guess is worse in a
     * public pack than one that is only reachable from the creative tab or /give.
     *
     * <p>The names wear the tier colour of the rung they start from (green L1, blue L4, orange L6, purple for
     * the disk that jumps straight to L9); the empty shell is left colourless so it cannot be read as a
     * usable drive.</p>
     */
    private static final int NAME_THEME_TIER_L1 = 0xFF61D389;
    private static final int NAME_THEME_TIER_L4 = 0xFF80AEFF;
    private static final int NAME_THEME_TIER_L6 = 0xFFE57E39;
    private static final int NAME_THEME_TIER_L9 = 0xFFB379E0;

    public static final RegistryObject<SimplifyMigrationDriveItem> MIGRATION_DRIVE_1_4 =
            ITEMS.register("migration_drive_1_4",
                    () -> new SimplifyMigrationDriveItem(new Item.Properties().stacksTo(16),
                            SimplifyMigrationDriveItem.Rung.ONE_TO_FOUR, NAME_THEME_TIER_L1));
    public static final RegistryObject<SimplifyMigrationDriveItem> MIGRATION_DRIVE_4_6 =
            ITEMS.register("migration_drive_4_6",
                    () -> new SimplifyMigrationDriveItem(new Item.Properties().stacksTo(16),
                            SimplifyMigrationDriveItem.Rung.FOUR_TO_SIX, NAME_THEME_TIER_L4));
    public static final RegistryObject<SimplifyMigrationDriveItem> MIGRATION_DRIVE_6_9 =
            ITEMS.register("migration_drive_6_9",
                    () -> new SimplifyMigrationDriveItem(new Item.Properties().stacksTo(16),
                            SimplifyMigrationDriveItem.Rung.SIX_TO_NINE, NAME_THEME_TIER_L6));
    /** What the machine returns once it has taken the structure across: the drive with nothing written on it. */
    public static final RegistryObject<Item> MIGRATION_DRIVE_EMPTY =
            ITEMS.register("migration_drive_empty", () -> new Item(new Item.Properties().stacksTo(16)));

    /**
     * The three AE2-to-L1 upgrade cards, ported from the 1.21.1 line with its ids: right-click the
     * matching AE2 block (or, for the pattern provider, cable part) and it is replaced in place with
     * the L1 counterpart, contents and settings carried over. On this line the two interface cards
     * are block-only - the powered and superconductive cable parts are not ported yet, so their
     * part halves pass null and the cable branch stays dormant.
     */
    public static final RegistryObject<Item> AE2_PATTERN_PROVIDER_TO_L1_CARD =
            ITEMS.register("ae2_pattern_provider_to_l1_card", () -> new Ae2ToL1UpgradeCardItem(
                    new Item.Properties().stacksTo(1),
                    appeng.blockentity.crafting.PatternProviderBlockEntity.class,
                    appeng.parts.crafting.PatternProviderPart.class,
                    () -> ModRegistration.SIMPLIFY_PATTERN_PROVIDER_BLOCK.get(),
                    () -> ModBlockEntities.PATTERN_PROVIDER.get(),
                    () -> CABLE_PATTERN_PROVIDER.get()));
    public static final RegistryObject<Item> AE2_INTERFACE_TO_L1_POWERED_CARD =
            ITEMS.register("ae2_interface_to_l1_powered_card", () -> new Ae2ToL1UpgradeCardItem(
                    new Item.Properties().stacksTo(1),
                    appeng.blockentity.misc.InterfaceBlockEntity.class, appeng.parts.misc.InterfacePart.class,
                    () -> ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get(),
                    () -> ModBlockEntities.POWERED_ME_INTERFACE.get(),
                    () -> CABLE_POWERED_ME_INTERFACE_PART.get()));
    /** The two self-growing cells ship unconditionally; the source member is arseng-gated and lives
     * in SourceIntegration. The registered handler behind them mounts in {@code Prototype1201}. */
    public static final RegistryObject<Item> SIMPLIFY_SINGULARITY_CELL =
            ITEMS.register("simplify_singularity_cell",
                    () -> new SimplifySingularityCellItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SIMPLIFY_SKY_STONE_DUST_CELL =
            ITEMS.register("simplify_sky_stone_dust_cell",
                    () -> new SimplifySingularityCellItem(new Item.Properties().stacksTo(1),
                            SimplifySingularityCellItem.SKY_STONE_DUST));

    /** The structure expansion card: grow a machine along its edge, or swap it up a tier in place. */
    public static final RegistryObject<Item> SIMPLIFY_STRUCTURE_EXPANSION_CARD =
            ITEMS.register("simplify_structure_expansion_card",
                    () -> new cn.dancingsnow.neoecoprototype1201.items.SimplifyStructureExpansionCardItem(
                            new Item.Properties().stacksTo(1)));

    /** The bag a click hands over instead of ninety loose blocks; right-click pours it out. */
    public static final RegistryObject<Item> BACKUP_DRIVE =
            ITEMS.register("backup_drive", () -> new BackupDriveItem(new Item.Properties().stacksTo(1)));

    /** One work order per machine the card can be pointed at; Trinity stays unported on this line. */
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_L1_STORAGE =
            ITEMS.register("simplify_work_order_grow_l1_storage",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_STORAGE_L4 =
            ITEMS.register("simplify_work_order_grow_storage_l4",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_STORAGE_L6 =
            ITEMS.register("simplify_work_order_grow_storage_l6",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_STORAGE_L9 =
            ITEMS.register("simplify_work_order_grow_storage_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L4 =
            ITEMS.register("simplify_work_order_grow_computation_l4",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L6 =
            ITEMS.register("simplify_work_order_grow_computation_l6",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L9 =
            ITEMS.register("simplify_work_order_grow_computation_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L4 =
            ITEMS.register("simplify_work_order_grow_crafting_l4",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L6 =
            ITEMS.register("simplify_work_order_grow_crafting_l6",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L9 =
            ITEMS.register("simplify_work_order_grow_crafting_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_GROW_F1_CRAFTING =
            ITEMS.register("simplify_work_order_grow_f1_crafting",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_UPGRADE_STORAGE_L9 =
            ITEMS.register("simplify_work_order_upgrade_storage_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_UPGRADE_COMPUTATION_L9 =
            ITEMS.register("simplify_work_order_upgrade_computation_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIMPLIFY_WORK_ORDER_UPGRADE_CRAFTING_L9 =
            ITEMS.register("simplify_work_order_upgrade_crafting_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> AE2_INTERFACE_TO_L1_SUPERCONDUCTIVE_CARD =
            ITEMS.register("ae2_interface_to_l1_superconductive_card", () -> new Ae2ToL1UpgradeCardItem(
                    new Item.Properties().stacksTo(1),
                    appeng.blockentity.misc.InterfaceBlockEntity.class, appeng.parts.misc.InterfacePart.class,
                    () -> ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get(),
                    () -> ModBlockEntities.SUPERCONDUCTIVE_INTERFACE.get(),
                    () -> CABLE_SUPERCONDUCTIVE_INTERFACE_PART.get()));
    public static final RegistryObject<SimplifyMigrationDriveItem> MIGRATION_DRIVE_CREATIVE =
            ITEMS.register("migration_drive_creative",
                    () -> new SimplifyMigrationDriveItem(new Item.Properties().stacksTo(16),
                            SimplifyMigrationDriveItem.Rung.STRAIGHT_TO_NINE, NAME_THEME_TIER_L9));

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
                        // Same for the megacells branch: the family's four cells, its card and its two
                        // housings, or nothing at all.
                        cn.dancingsnow.neoecoprototype1201.integration.megacells.SmallBulkIntegration.TAB_ITEMS
                                .forEach(item -> output.accept(item.get()));
                        // The four omni families: empty without OmniCells, because eco's
                        // IntegrationManager only constructs the integration behind its gate.
                        cn.dancingsnow.neoecoprototype1201.integration.ae2omnicells
                                .UniversalIntegration.TAB_ITEMS
                                .forEach(item -> output.accept(item.get()));
                        output.accept(SIMPLIFY_STRUCTURE_EXPANSION_CARD.get());
                        output.accept(BACKUP_DRIVE.get());
                        output.accept(PIGMEE_STORAGE_MATRIX_HOUSING.get());
                        output.accept(PIGMEE_STORAGE_CELL.get());
                        output.accept(SIMPLIFY_CONCRETE_STORAGE_MATRIX_HOUSING.get());
                        output.accept(SIMPLIFY_CONCRETE_STORAGE_CELL.get());
                        // Creative-only helpers: an endless everything-cell, and the pattern generator.
                        // Neither has a survival recipe; they exist so testers can build a crafting
                        // setup without hand-encoding hundreds of patterns.
                        output.accept(ALL_ITEMS_CREATIVE_CELL.get());
                        output.accept(PATTERN_GENERATOR.get());
                        output.accept(SIMPLIFY_COMPUTATION_CELL_1M.get());
                        output.accept(ENERGIZED_COMPUTATION_CELL_4M.get());
                        output.accept(CABLE_PATTERN_PROVIDER.get());
                        // The two cable-face interfaces, next to the cable pattern provider.
                        output.accept(CABLE_POWERED_ME_INTERFACE_PART.get());
                        output.accept(CABLE_SUPERCONDUCTIVE_INTERFACE_PART.get());
                        output.accept(SIMPLIFY_SINGULARITY_CELL.get());
                        output.accept(SIMPLIFY_SKY_STONE_DUST_CELL.get());
                        output.accept(AE2_PATTERN_PROVIDER_TO_L1_CARD.get());
                        output.accept(AE2_INTERFACE_TO_L1_POWERED_CARD.get());
                        output.accept(AE2_INTERFACE_TO_L1_SUPERCONDUCTIVE_CARD.get());
                        output.accept(GREEN_CRYSTAL_MATRIX.get());
                        output.accept(GRETTE_CRYSTAL.get());
                        output.accept(GRETTE_CRYSTAL_DUST.get());
                        // Match the source policy: only the dessert forms are visible; ores and the
                        // dissolution bath stay registered for commands/datapacks, outside this tab.
                        output.accept(cn.dancingsnow.neoecoprototype1201.registration.ColdContent.CRYOTHEUM_ICE_CREAM.get());
                        output.accept(cn.dancingsnow.neoecoprototype1201.registration.ColdContent.CRYOTHEUM_ICE_CREAM_BLOCK_ITEM.get());
                        output.accept(cn.dancingsnow.neoecoprototype1201.registration.ColdContent.CRYOTHEUM_ICE_CREAM_BUCKET.get());
                        output.accept(MIGRATION_DRIVE_1_4.get());
                        output.accept(MIGRATION_DRIVE_4_6.get());
                        output.accept(MIGRATION_DRIVE_6_9.get());
                        output.accept(MIGRATION_DRIVE_CREATIVE.get());
                        output.accept(MIGRATION_DRIVE_EMPTY.get());
                    })
                    .build());

    private Registration() {
    }
}
