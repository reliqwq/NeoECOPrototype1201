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
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageInterfaceBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

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

    private ModRegistration() {
    }
}
