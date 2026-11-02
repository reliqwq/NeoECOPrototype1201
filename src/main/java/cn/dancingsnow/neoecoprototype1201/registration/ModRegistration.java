package cn.dancingsnow.neoecoprototype1201.registration;

import cn.dancingsnow.neoecoae.blocks.ECOMachineCasing;
import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.blocks.storage.ECODriveBlock;
import cn.dancingsnow.neoecoae.blocks.storage.ECOEnergyCellBlock;
import cn.dancingsnow.neoecoae.blocks.storage.ECOStorageVentBlock;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEStorageCluster;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingParallelCore;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingPatternBus;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingVent;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingWorker;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOFluidInputHatchBlock;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOFluidOutputHatchBlock;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyCraftingTier;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationCoolingControllerBlock;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationDriveBlock;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationInterfaceBlock;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationNetworkInterfaceBlock;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationParallelCoreBlock;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationSystemBlock;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationThreadingCoreBlock;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationTransmitterBlock;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyEnergizedComputationCoreBlock;
import cn.dancingsnow.neoecoprototype1201.block.crafting.SimplifyCraftingInterfaceBlock;
import cn.dancingsnow.neoecoprototype1201.block.crafting.SimplifyCraftingNetworkInterfaceBlock;
import cn.dancingsnow.neoecoprototype1201.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype1201.block.crafting.SimplifyPatternProviderBlock;
import cn.dancingsnow.neoecoprototype1201.block.decoration.GiantDollBlock;
import cn.dancingsnow.neoecoprototype1201.block.decoration.NamedDollBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageInterfaceBlock;
import cn.dancingsnow.neoecoprototype1201.items.FumoItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Blocks and block items of the L1 storage matrix.
 *
 * <p>Named {@code ModRegistration} on purpose: the 1.21.1 line keeps its registrations in a class of
 * the same name, so a structure file copied between the two lines resolves its imports unchanged.
 * Block entity types live in {@link ModBlockEntities}: AE2's block base reads the type out of the
 * block properties, and a block registered without one keeps a null type and crashes as soon as a
 * placed block is used.
 */
public final class ModRegistration {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, Prototype1201.MOD_ID);
    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(Registries.ITEM, Prototype1201.MOD_ID);

    /** DeferredRegister is not iterable, so the creative tab reads this instead. */
    public static final List<RegistryObject<Item>> TAB_BLOCK_ITEMS = new ArrayList<>();

    /**
     * Mirrors eco's own blocks, which copy iron's properties instead of naming a material. The block
     * entity type is not part of the properties here: 1.20.1 has no Properties#blockEntity, so
     * ModBlockEntities back-fills it through AEBaseEntityBlock#setBlockEntity.
     */
    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK);
    }

    private static RegistryObject<Block> registerBlock(String name, Supplier<Block> block) {
        RegistryObject<Block> registered = BLOCKS.register(name, block);
        RegistryObject<Item> item = BLOCK_ITEMS.register(name,
                () -> new BlockItem(registered.get(), new Item.Properties()));
        TAB_BLOCK_ITEMS.add(item);
        return registered;
    }

    /**
     * Same as {@link #registerBlock}, but the item is AE2's {@code EnergyCellBlockItem}: a battery that
     * carries its charge has to show it in the tooltip and let a charger read it, and both hang off
     * {@code IAEItemPowerStorage}, which a plain BlockItem does not implement.
     */
    private static RegistryObject<Block> registerCell(String name, Supplier<Block> block) {
        RegistryObject<Block> registered = BLOCKS.register(name, block);
        RegistryObject<Item> item = BLOCK_ITEMS.register(name,
                () -> new appeng.block.networking.EnergyCellBlockItem(registered.get(), new Item.Properties()));
        TAB_BLOCK_ITEMS.add(item);
        return registered;
    }

    public static final RegistryObject<Block> SIMPLIFY_STORAGE_CONTROLLER_BLOCK =
            registerBlock("simplify_storage_controller",
                    () -> new SimplifyStorageControllerBlock(
                            metal().noOcclusion()));
    public static final RegistryObject<Block> SIMPLIFY_STORAGE_CASING_BLOCK =
            registerBlock("simplify_storage_casing",
                    () -> new ECOMachineCasing<NEStorageCluster>(metal()));
    public static final RegistryObject<Block> SIMPLIFY_DRIVE_BLOCK =
            registerBlock("simplify_drive",
                    () -> new ECODriveBlock(metal().noOcclusion()));
    /**
     * The two interface blocks are deliberately different classes: the plain one opens nothing, the
     * communication one keeps eco's interface UI. They also get separate block entity types (see
     * ModBlockEntities) so AE2's network tool can name each by its own item.
     */
    public static final RegistryObject<Block> SIMPLIFY_STORAGE_INTERFACE_BLOCK =
            registerBlock("simplify_storage_interface",
                    () -> new SimplifyStorageInterfaceBlock(metal()));
    public static final RegistryObject<Block> SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK =
            registerBlock("simplify_storage_network_interface",
                    () -> new ECOMachineInterface<NEStorageCluster>(metal()));
    public static final RegistryObject<Block> SIMPLIFY_STORAGE_VENT_BLOCK =
            registerBlock("simplify_storage_vent",
                    () -> new ECOStorageVentBlock(
                            metal()));
    public static final RegistryObject<Block> SIMPLIFY_ENERGY_CELL_BLOCK =
            registerBlock("simplify_energy_cell",
                    () -> new ECOEnergyCellBlock(
                            metal().noOcclusion()));

    // ============================ standalone AE2-side devices ============================

    /**
     * The two interface machines are AE2's own {@code InterfaceBlock} with no subclass: nothing on the block
     * side is ours. AE2's no-arg constructor copies its own metal properties, so {@link #metal()} is not
     * applied here - passing our properties would need a constructor AE2 does not have.
     */
    public static final RegistryObject<Block> SIMPLIFY_POWERED_ME_INTERFACE_BLOCK =
            registerBlock("simplify_powered_me_interface", appeng.block.misc.InterfaceBlock::new);
    public static final RegistryObject<Block> SUPERCONDUCTIVE_INTERFACE_BLOCK =
            registerBlock("superconductive_interface", appeng.block.misc.InterfaceBlock::new);

    /** AE, not FE. The second id says 512g on both lines while its buffer is 256G - the guide page says so. */
    private static final double ENERGIZED_CELL_CAPACITY_16G = 16_000_000_000.0;
    private static final double ENERGIZED_CELL_CAPACITY_256G = 256_000_000_000.0;

    /**
     * The two standalone batteries. AE2's own {@code EnergyCellBlock} is the base because on this engine it
     * already carries the whole contract: it publishes an {@code IAEPowerStorage} node, persists its charge,
     * hands that charge to the dropped item, shows it in the tooltip, and takes the buffer size in its
     * constructor. Charge rate follows AE2's dense cell (one thousandth of the buffer per tick); the two
     * priorities are ours, set above AE2's dense 1600 so the bigger battery is filled first.
     * The price of reusing it is the one the two interfaces above already pay: that constructor chooses its
     * own properties ({@code glassProps()}), so {@link #metal()} cannot be applied from here.
     */
    public static final RegistryObject<Block> ENERGIZED_ENERGY_CELL_16G_BLOCK =
            registerCell("energized_energy_cell_16g",
                    () -> new appeng.block.networking.EnergyCellBlock(
                            ENERGIZED_CELL_CAPACITY_16G, ENERGIZED_CELL_CAPACITY_16G / 1000.0, 3200));
    public static final RegistryObject<Block> ENERGIZED_ENERGY_CELL_512G_BLOCK =
            registerCell("energized_energy_cell_512g",
                    () -> new appeng.block.networking.EnergyCellBlock(
                            ENERGIZED_CELL_CAPACITY_256G, ENERGIZED_CELL_CAPACITY_256G / 1000.0, 6400));
    /**
     * The assembler is our own subclass of AE2's, unlike the two interfaces above: it carries a block
     * identity of its own because the machine has to name itself to terminals (see
     * {@code SimplifyStonecuttingAssemblerBlockEntity#getCraftingMachineInfo}).
     */
    public static final RegistryObject<Block> SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK =
            registerBlock("simplify_stonecutting_assembler",
                    () -> new cn.dancingsnow.neoecoprototype1201.block.crafting
                            // noOcclusion, not plain metal(): this model is 13 elements with 32 cullfaces, so
                            // every face that touches a full block is skipped unless the block says it does
                            // not occlude. Same rule the computation and crafting helpers already follow.
                            .SimplifyStonecuttingAssemblerBlock(metal().noOcclusion()));

    /**
     * The L1 shell material, and the thing the assembler is built from. Plain block on purpose: eco's own
     * {@code ECOMachineCasing} is a multiblock member, and a crafting material that can complete a machine
     * would be a trap. Properties follow this line's other casings rather than the 1.21.1 numbers.
     */
    public static final RegistryObject<Block> SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK =
            registerBlock("simplify_green_aluminum_casing",
                    () -> new cn.dancingsnow.neoecoprototype1201.block.SimplifyCasingBlock(metal().noOcclusion()));

    // ============================ L1 computation blocks ============================

    /**
     * The whole computation family copies iron's properties and adds noOcclusion, same as the 1.21.1
     * line: the models are shells with open faces rather than full cubes.
     */
    private static BlockBehaviour.Properties computation() {
        return metal().noOcclusion();
    }

    public static final RegistryObject<Block> SIMPLIFY_COMPUTATION_SYSTEM_BLOCK =
            registerBlock("simplify_computation_system",
                    () -> new SimplifyComputationSystemBlock(computation()));
    public static final RegistryObject<Block> SIMPLIFY_COMPUTATION_DRIVE_BLOCK =
            registerBlock("simplify_computation_drive",
                    () -> new SimplifyComputationDriveBlock(computation()));
    public static final RegistryObject<Block> SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK =
            registerBlock("simplify_computation_threading_core",
                    () -> new SimplifyComputationThreadingCoreBlock(computation()));
    public static final RegistryObject<Block> ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK =
            registerBlock("energized_computation_threading_core",
                    () -> new SimplifyComputationThreadingCoreBlock(computation(),
                            SimplifyTier.L1_ENERGIZED_THREADING));
    public static final RegistryObject<Block> SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK =
            registerBlock("simplify_computation_parallel_core",
                    () -> new SimplifyComputationParallelCoreBlock(computation()));
    public static final RegistryObject<Block> ENERGIZED_COMPUTATION_CORE_BLOCK =
            registerBlock("energized_computation_core",
                    () -> new SimplifyEnergizedComputationCoreBlock(computation(),
                            SimplifyTier.L1_PARALLEL_SWITCH));
    public static final RegistryObject<Block> SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK =
            registerBlock("simplify_computation_cooling_controller",
                    () -> new SimplifyComputationCoolingControllerBlock(computation()));
    public static final RegistryObject<Block> SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK =
            registerBlock("simplify_computation_transmitter",
                    () -> new SimplifyComputationTransmitterBlock(computation()));
    public static final RegistryObject<Block> SIMPLIFY_COMPUTATION_INTERFACE_BLOCK =
            registerBlock("simplify_computation_interface",
                    () -> new SimplifyComputationInterfaceBlock(computation()));
    public static final RegistryObject<Block> SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK =
            registerBlock("simplify_computation_network_interface",
                    () -> new SimplifyComputationNetworkInterfaceBlock(computation()));
    public static final RegistryObject<Block> SIMPLIFY_COMPUTATION_CASING_BLOCK =
            registerBlock("simplify_computation_casing",
                    () -> new ECOMachineCasing<NEComputationCluster>(metal()));

    // ============================ L1 crafting blocks ============================

    /** The same shell rule as the computation family: these models are open frames, not full cubes. */
    private static BlockBehaviour.Properties crafting() {
        return metal().noOcclusion();
    }

    public static final RegistryObject<Block> SIMPLIFY_CRAFTING_SYSTEM_BLOCK =
            registerBlock("simplify_crafting_system",
                    () -> new SimplifyCraftingSystemBlock(crafting()));
    public static final RegistryObject<Block> SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK =
            registerBlock("simplify_crafting_pattern_bus",
                    () -> new ECOCraftingPatternBus(crafting()));
    /**
     * The L1 pattern provider. Not a cluster member and not eco's machine: AE2's own provider block, whose
     * only opening is that its properties are AE2's {@code metalProps()} - this generation's constructor
     * takes nothing at all, so there is no noOcclusion to ask for and the box is a full cube by design.
     */
    public static final RegistryObject<Block> SIMPLIFY_PATTERN_PROVIDER_BLOCK =
            registerBlock("simplify_pattern_provider", SimplifyPatternProviderBlock::new);
    public static final RegistryObject<Block> SIMPLIFY_CRAFTING_WORKER_BLOCK =
            registerBlock("simplify_crafting_worker",
                    () -> new ECOCraftingWorker(crafting()));
    /** eco's own block class, because the only thing a subclass could add here is the tier it already takes. */
    public static final RegistryObject<Block> SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK =
            registerBlock("simplify_crafting_parallel_core",
                    () -> new ECOCraftingParallelCore(crafting(), SimplifyCraftingTier.L1));
    public static final RegistryObject<Block> SIMPLIFY_CRAFTING_VENT_BLOCK =
            registerBlock("simplify_crafting_vent",
                    () -> new ECOCraftingVent(crafting()));
    public static final RegistryObject<Block> SIMPLIFY_CRAFTING_INTERFACE_BLOCK =
            registerBlock("simplify_crafting_interface",
                    () -> new SimplifyCraftingInterfaceBlock(crafting()));
    public static final RegistryObject<Block> SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK =
            registerBlock("simplify_crafting_network_interface",
                    () -> new SimplifyCraftingNetworkInterfaceBlock(crafting()));
    public static final RegistryObject<Block> SIMPLIFY_CRAFTING_CASING_BLOCK =
            registerBlock("simplify_crafting_casing",
                    () -> new ECOMachineCasing<NECraftingCluster>(metal()));
    public static final RegistryObject<Block> SIMPLIFY_FLUID_INPUT_HATCH_BLOCK =
            registerBlock("simplify_fluid_input_hatch",
                    () -> new ECOFluidInputHatchBlock(crafting()));
    public static final RegistryObject<Block> SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK =
            registerBlock("simplify_fluid_output_hatch",
                    () -> new ECOFluidOutputHatchBlock(crafting()));

    /** The dolls' family colour, taken from the same green her lamp strip uses. */
    private static final int NAME_THEME_GREEN = 0xFF94EDAB;

    /**
     * What the dolls are made of. Their face is in their texture, so nothing resolves a skin - only the
     * shared {@link cn.dancingsnow.neoecoprototype1201.blockentity.decoration.NamedDollBlockEntity}, which
     * exists to sweep for creepers, and one client renderer for the giant plushie.
     *
     * <p>The light level is a design choice carried over from the 1.21.1 line, not a fact this file can
     * justify: the plushie was given one on purpose and the other dolls inherited it.</p>
     */
    private static BlockBehaviour.Properties dollProperties() {
        return BlockBehaviour.Properties.of().strength(0.8F)
                .sound(net.minecraft.world.level.block.SoundType.WOOL).noOcclusion()
                .lightLevel(state -> 15);
    }

    /** A doll's worn stats: its own armour, then whatever effects it hands out on top of the night vision. */
    private static FumoItem.DollStats dollStats(float armor, float toughness,
                                                FumoItem.DollEffect... extra) {
        return new FumoItem.DollStats(armor, toughness, java.util.List.of(extra), 0.0D);
    }

    /** The same for the doll that also feeds what its wearer is carrying; the rate is its own constant. */
    private static FumoItem.DollStats chargingDollStats(float armor, float toughness,
                                                        FumoItem.DollEffect... extra) {
        return new FumoItem.DollStats(armor, toughness, java.util.List.of(extra), 100.0D);
    }

    /** One effect at one level - amplifier 0 is the first level, as everywhere else in vanilla. */
    private static FumoItem.DollEffect dollEffect(net.minecraft.world.effect.MobEffect effect, int amplifier) {
        return new FumoItem.DollEffect(effect, amplifier);
    }

    /**
     * A doll's wearable item, registered against the block named above it. The plain {@link #registerBlock}
     * would hand back a {@code BlockItem}, and these need the {@link FumoItem} that carries the armour, the
     * effects and the worn tooltip - which is also why the tab entry is added here rather than there.
     */
    private static RegistryObject<Item> registerDollItem(String name, RegistryObject<? extends Block> block,
                                                         FumoItem.DollStats stats,
                                                         @Nullable Integer nameColor, boolean shiny) {
        java.util.function.Supplier<Item> factory = () -> new FumoItem(block.get(),
                new Item.Properties().stacksTo(16), stats, nameColor, shiny);
        RegistryObject<Item> item = BLOCK_ITEMS.register(name, factory);
        TAB_BLOCK_ITEMS.add(item);
        return item;
    }

    public static final RegistryObject<NamedDollBlock> FUMO_DOLL_RELIQWQ_BLOCK =
            BLOCKS.register("fumo_doll_reliqwq", () -> new NamedDollBlock(dollProperties()));
    public static final RegistryObject<Item> FUMO_DOLL_RELIQWQ_ITEM =
            registerDollItem("fumo_doll_reliqwq", FUMO_DOLL_RELIQWQ_BLOCK,
                    dollStats(4.0F, 2.0F), NAME_THEME_GREEN, false);

    public static final RegistryObject<NamedDollBlock> FUMO_DOLL_YANG120_BLOCK =
            BLOCKS.register("fumo_doll_yang120", () -> new NamedDollBlock(dollProperties()));
    public static final RegistryObject<Item> FUMO_DOLL_YANG120_ITEM =
            registerDollItem("fumo_doll_yang120", FUMO_DOLL_YANG120_BLOCK,
                    dollStats(4.0F, 2.0F), NAME_THEME_GREEN, false);

    public static final RegistryObject<NamedDollBlock> FUMO_DOLL_KOOOKI_BLOCK =
            BLOCKS.register("fumo_doll_kouooki", () -> new NamedDollBlock(dollProperties()));
    public static final RegistryObject<Item> FUMO_DOLL_KOOOKI_ITEM =
            registerDollItem("fumo_doll_kouooki", FUMO_DOLL_KOOOKI_BLOCK,
                    dollStats(1.0F, 5.0F), NAME_THEME_GREEN, false);

    public static final RegistryObject<NamedDollBlock> FUMO_DOLL_TEDXENON_BLOCK =
            BLOCKS.register("fumo_doll_tedxenon", () -> new NamedDollBlock(dollProperties()));
    public static final RegistryObject<Item> FUMO_DOLL_TEDXENON_ITEM =
            registerDollItem("fumo_doll_tedxenon", FUMO_DOLL_TEDXENON_BLOCK,
                    chargingDollStats(6.0F, 1.0F,
                            dollEffect(net.minecraft.world.effect.MobEffects.REGENERATION, 1),
                            dollEffect(net.minecraft.world.effect.MobEffects.LUCK, 0)),
                    NAME_THEME_GREEN, false);

    /**
     * The plushie drawn three times her size, standing on the one block she was set down on. She is
     * decoration only - no CPU, no grid node - and she carries no model of her own: the block declares
     * itself {@link net.minecraft.world.level.block.RenderShape#INVISIBLE} and
     * {@link cn.dancingsnow.neoecoprototype1201.client.renderer.blockentity.GiantPlushieRenderer} draws the
     * plain doll's model scaled up, which is why the blockstate still names her model (breaking particles
     * and the fallback path read it).
     */
    public static final RegistryObject<GiantDollBlock> FUMO_DOLL_YANG120_GIANT_BLOCK =
            BLOCKS.register("fumo_doll_yang120_giant", () -> new GiantDollBlock(dollProperties()));
    public static final RegistryObject<Item> FUMO_DOLL_YANG120_GIANT_ITEM =
            registerDollItem("fumo_doll_yang120_giant", FUMO_DOLL_YANG120_GIANT_BLOCK,
                    dollStats(4.0F, 2.0F), null, true);

    private ModRegistration() {
    }
}
