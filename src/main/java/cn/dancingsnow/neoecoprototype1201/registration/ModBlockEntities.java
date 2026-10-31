package cn.dancingsnow.neoecoprototype1201.registration;

import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineCasingBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineInterfaceBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationCoolingControllerBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationThreadingCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationTransmitterBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.storage.ECODriveBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.storage.ECOEnergyCellBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.storage.ECOStorageSystemBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.storage.ECOStorageVentBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEStorageCluster;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOFluidInputHatchBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOFluidOutputHatchBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NEComputationClusterCalculator;
import cn.dancingsnow.neoecoae.multiblock.calculator.NECraftingClusterCalculator;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyCraftingTier;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype1201.blockentity.computation.SimplifyComputationDriveBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.computation.SimplifyComputationSystemBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyCraftingParallelCoreBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyCraftingVentBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyCraftingWorkerBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyPoweredMEInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifySuperconductiveInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyStorageHostBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Block entity types of the L1 storage matrix, kept apart from {@link ModRegistration} because the
 * two need each other: a block reads its type from {@code Properties#blockEntity}, and a type lists
 * the blocks it may sit in. Split across classes, both directions can stay lazy without a Java
 * forward reference; this is also how AE2 and eco split their own registries.
 */
public final class ModBlockEntities {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("neoeco1201-representatives");

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Prototype1201.MOD_ID);

    @FunctionalInterface
    private interface BEFactory<T extends BlockEntity> {
        T create(BlockEntityType<?> type, BlockPos pos, BlockState state);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerType(
            String name, Class<T> beClass, BEFactory<T> factory,
            BlockEntityTicker<T> serverTicker, Supplier<? extends Block>... validBlocks) {
        return buildType(name, beClass, factory, serverTicker, validBlocks);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> buildType(
            String name, Class<T> beClass, BEFactory<T> factory,
            BlockEntityTicker<T> serverTicker, Supplier<? extends Block>... validBlocks) {
        return BLOCK_ENTITIES.register(name, () -> {
            @SuppressWarnings("unchecked")
            BlockEntityType<T>[] holder = new BlockEntityType[1];
            Block[] blocks = Arrays.stream(validBlocks).map(Supplier::get).toArray(Block[]::new);
            @SuppressWarnings("unchecked")
            BlockEntityType<T> built = (BlockEntityType<T>) BlockEntityType.Builder
                    .of((pos, state) -> factory.create(holder[0], pos, state), blocks)
                    .build(null);
            holder[0] = built;
            // AE2 fills the block's type after construction (1.20.1 Properties has no blockEntity()),
            // so hand the finished type to every block it may sit in, like eco's onRegister does.
            wire(blocks, beClass, built, serverTicker);
            return built;
        });
    }

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerType(
            String name, Class<T> beClass, BEFactory<T> factory, Supplier<? extends Block>... validBlocks) {
        return buildType(name, beClass, factory, null, validBlocks);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean wire(Block[] blocks, Class beClass, BlockEntityType type, BlockEntityTicker ticker) {
        for (Block block : blocks) {
            if (block instanceof AEBaseEntityBlock entityBlock) {
                entityBlock.setBlockEntity(beClass, type, null, ticker);
            }
        }
        // AE2 names a machine in its own network tool by walking back to REPRESENTATIVE_ITEMS:
        // AEBaseBlockEntity.getItemFromBlockEntity() is getOrDefault(type, Items.AIR) and the row title is
        // that item's description. AE2 fills the table only for AE2's types and eco only for eco's (its
        // NEBlockEntityEntry#onCommonSetup), so a type we build ourselves would list every machine it owns
        // as 空气. Queued here and applied at commonSetup, not applied now: at registry-event time a block's
        // item may not be bound yet, and a guard that skipped quietly would turn the fix into a no-op.
        if (blocks.length > 0) {
            REPRESENTATIVE_ITEMS.add(new RepresentativeItem(type, blocks[0]));
        }
        return true;
    }

    private record RepresentativeItem(BlockEntityType<?> type, Block block) {
    }

    private static final java.util.List<RepresentativeItem> REPRESENTATIVE_ITEMS = new java.util.ArrayList<>();

    /**
     * Fills AE2's representative-item table for every type this class built. Call from common setup, where
     * every block's item is bound; each line is logged because the failure mode is a machine listing itself
     * as air, which a player reports and a log line names.
     */
    public static void registerRepresentativeItems() {
        for (RepresentativeItem entry : REPRESENTATIVE_ITEMS) {
            net.minecraft.world.item.Item item = entry.block().asItem();
            if (item.equals(net.minecraft.world.item.Items.AIR)) {
                LOGGER.warn("no representative item for {} (block {} has no item) - AE2's network tool will "
                        + "name that machine 空气",
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entry.type()),
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(entry.block()));
                continue;
            }
            appeng.blockentity.AEBaseBlockEntity.registerBlockEntityItem(entry.type(), item);
            LOGGER.info("representative item for {} = {}",
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(entry.block()),
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item));
        }
    }

    /**
     * The storage family is eco's own machine with our tier and our calculator handed in: the host carries
     * the panel, the priority and the build controls, the drive carries the cell, and the cluster
     * ({@code NEStorageCluster}) matches members by {@code instanceof} eco's classes - which is why none of
     * these may be a hand-rolled {@code NEBlockEntity} of our own.
     */
    public static final RegistryObject<BlockEntityType<ECOStorageSystemBlockEntity>> STORAGE_HOST =
            registerType("storage_host", ECOStorageSystemBlockEntity.class,
                    (type, pos, state) -> new SimplifyStorageHostBlockEntity(type, pos, state, SimplifyTier.L1),
                    (level, pos, state, be) -> be.tick(level, pos, state),
                    () -> ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOMachineCasingBlockEntity<NEStorageCluster>>> STORAGE_CASING =
            registerType("storage_casing",
                    (Class<ECOMachineCasingBlockEntity<NEStorageCluster>>) (Class<?>) ECOMachineCasingBlockEntity.class,
                    (type, pos, state) -> new ECOMachineCasingBlockEntity<>(
                            type, pos, state, SimplifyStorageClusterCalculator::new),
                    () -> ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECODriveBlockEntity>> DRIVE =
            registerType("drive", ECODriveBlockEntity.class, SimplifyDriveBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_DRIVE_BLOCK.get());
    /**
     * Two types for the two interface blocks, same class: AE2's network tool names a machine through the
     * representative item registered per type, so sharing one type would make both blocks answer with
     * whichever item happened to be first - and the communication interface is the one a player opens.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final RegistryObject<BlockEntityType<ECOMachineInterfaceBlockEntity<NEStorageCluster>>> STORAGE_INTERFACE =
            registerType("storage_interface",
                    (Class<ECOMachineInterfaceBlockEntity<NEStorageCluster>>) (Class<?>) ECOMachineInterfaceBlockEntity.class,
                    (type, pos, state) -> new ECOMachineInterfaceBlockEntity<>(
                            type, pos, state, SimplifyStorageClusterCalculator::new),
                    () -> ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get());
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final RegistryObject<BlockEntityType<ECOMachineInterfaceBlockEntity<NEStorageCluster>>> STORAGE_NETWORK_INTERFACE =
            registerType("storage_network_interface",
                    (Class<ECOMachineInterfaceBlockEntity<NEStorageCluster>>) (Class<?>) ECOMachineInterfaceBlockEntity.class,
                    (type, pos, state) -> new ECOMachineInterfaceBlockEntity<>(
                            type, pos, state, SimplifyStorageClusterCalculator::new),
                    () -> ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOStorageVentBlockEntity>> STORAGE_VENT =
            registerType("storage_vent", ECOStorageVentBlockEntity.class, ECOStorageVentBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_STORAGE_VENT_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOEnergyCellBlockEntity>> ENERGY_CELL =
            registerType("energy_cell", ECOEnergyCellBlockEntity.class,
                    (type, pos, state) -> new ECOEnergyCellBlockEntity(type, pos, state, SimplifyTier.L1),
                    () -> ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get());

    // ============================ standalone AE2-side devices ============================

    /**
     * AE2's own interface block entity, widened: both carry a passive generation service, and the
     * superconductive one also swaps the logic's two inventories for ones that let a single marker stock
     * 8192. Neither needs a ticker - AE2 drives an interface from the grid, not from block ticks.
     */
    public static final RegistryObject<BlockEntityType<SimplifyPoweredMEInterfaceBlockEntity>>
            POWERED_ME_INTERFACE = registerType("simplify_powered_me_interface",
            SimplifyPoweredMEInterfaceBlockEntity.class, SimplifyPoweredMEInterfaceBlockEntity::new,
            () -> ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifySuperconductiveInterfaceBlockEntity>>
            SUPERCONDUCTIVE_INTERFACE = registerType("superconductive_interface",
            SimplifySuperconductiveInterfaceBlockEntity.class, SimplifySuperconductiveInterfaceBlockEntity::new,
            () -> ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());

    // ============================ L1 computation ============================

    /**
     * Every computation machine except the transmitter and the host is one of eco's own block entities
     * with our tier handed in; only the drive needs a class of its own, because the panel-row decision
     * is made by comparing against eco's transmitter block and our machine is built from our blocks.
     */
    public static final RegistryObject<BlockEntityType<ECOComputationSystemBlockEntity>> COMPUTATION_SYSTEM =
            registerType("simplify_computation_system", ECOComputationSystemBlockEntity.class,
                    (type, pos, state) -> new SimplifyComputationSystemBlockEntity(type, pos, state, SimplifyTier.L1),
                    (level, pos, state, be) -> be.tick(level, pos, state),
                    () -> ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifyComputationDriveBlockEntity>> COMPUTATION_DRIVE =
            registerType("simplify_computation_drive", SimplifyComputationDriveBlockEntity.class,
                    SimplifyComputationDriveBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOComputationThreadingCoreBlockEntity>> COMPUTATION_THREADING_CORE =
            registerType("simplify_computation_threading_core", ECOComputationThreadingCoreBlockEntity.class,
                    (type, pos, state) -> new ECOComputationThreadingCoreBlockEntity(type, pos, state, SimplifyTier.L1),
                    () -> ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOComputationThreadingCoreBlockEntity>> ENERGIZED_COMPUTATION_THREADING_CORE =
            registerType("energized_computation_threading_core", ECOComputationThreadingCoreBlockEntity.class,
                    (type, pos, state) -> new ECOComputationThreadingCoreBlockEntity(
                            type, pos, state, SimplifyTier.L1_ENERGIZED_THREADING),
                    () -> ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOComputationParallelCoreBlockEntity>> COMPUTATION_PARALLEL_CORE =
            registerType("simplify_computation_parallel_core", ECOComputationParallelCoreBlockEntity.class,
                    (type, pos, state) -> new ECOComputationParallelCoreBlockEntity(type, pos, state, SimplifyTier.L1),
                    () -> ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOComputationParallelCoreBlockEntity>> ENERGIZED_COMPUTATION_CORE =
            registerType("energized_computation_core", ECOComputationParallelCoreBlockEntity.class,
                    (type, pos, state) -> new ECOComputationParallelCoreBlockEntity(
                            type, pos, state, SimplifyTier.L1_PARALLEL_SWITCH),
                    () -> ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOComputationCoolingControllerBlockEntity>> COMPUTATION_COOLING_CONTROLLER =
            registerType("simplify_computation_cooling_controller", ECOComputationCoolingControllerBlockEntity.class,
                    (type, pos, state) -> new ECOComputationCoolingControllerBlockEntity(type, pos, state, SimplifyTier.L1),
                    () -> ModRegistration.SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOComputationTransmitterBlockEntity>> COMPUTATION_TRANSMITTER =
            registerType("simplify_computation_transmitter", ECOComputationTransmitterBlockEntity.class,
                    ECOComputationTransmitterBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOMachineInterfaceBlockEntity<NEComputationCluster>>> COMPUTATION_INTERFACE =
            registerType("simplify_computation_interface",
                    (Class<ECOMachineInterfaceBlockEntity<NEComputationCluster>>) (Class<?>) ECOMachineInterfaceBlockEntity.class,
                    (type, pos, state) -> new ECOMachineInterfaceBlockEntity<>(
                            type, pos, state, NEComputationClusterCalculator::new),
                    () -> ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get());
    /** Its own type, same class, so AE2's network tool names the 通讯接口 by its own item. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final RegistryObject<BlockEntityType<ECOMachineInterfaceBlockEntity<NEComputationCluster>>>
            COMPUTATION_NETWORK_INTERFACE =
            registerType("simplify_computation_network_interface",
                    (Class<ECOMachineInterfaceBlockEntity<NEComputationCluster>>) (Class<?>) ECOMachineInterfaceBlockEntity.class,
                    (type, pos, state) -> new ECOMachineInterfaceBlockEntity<>(
                            type, pos, state, NEComputationClusterCalculator::new),
                    () -> ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOMachineCasingBlockEntity<NEComputationCluster>>> COMPUTATION_CASING =
            registerType("simplify_computation_casing",
                    (Class<ECOMachineCasingBlockEntity<NEComputationCluster>>) (Class<?>) ECOMachineCasingBlockEntity.class,
                    (type, pos, state) -> new ECOMachineCasingBlockEntity<>(
                            type, pos, state, NEComputationClusterCalculator::new),
                    () -> ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK.get());

    // ============================ L1 crafting ============================

    /**
     * The host is the only crafting machine whose block entity class is ours, and only because it has to
     * answer two questions eco cannot: which build definition an addon tier gets, and which of the two
     * interface blocks the formed machine holds. Everything else takes eco's class with our tier handed in.
     */
    public static final RegistryObject<BlockEntityType<ECOCraftingSystemBlockEntity>> CRAFTING_SYSTEM =
            registerType("simplify_crafting_system", ECOCraftingSystemBlockEntity.class,
                    (type, pos, state) -> new SimplifyCraftingSystemBlockEntity(
                            type, pos, state, SimplifyCraftingTier.L1),
                    (level, pos, state, be) -> be.tick(level, pos, state),
                    () -> ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOCraftingPatternBusBlockEntity>> CRAFTING_PATTERN_BUS =
            registerType("simplify_crafting_pattern_bus", ECOCraftingPatternBusBlockEntity.class,
                    ECOCraftingPatternBusBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifyCraftingWorkerBlockEntity>> CRAFTING_WORKER =
            registerType("simplify_crafting_worker", SimplifyCraftingWorkerBlockEntity.class,
                    SimplifyCraftingWorkerBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_CRAFTING_WORKER_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifyCraftingParallelCoreBlockEntity>> CRAFTING_PARALLEL_CORE =
            registerType("simplify_crafting_parallel_core", SimplifyCraftingParallelCoreBlockEntity.class,
                    (type, pos, state) -> new SimplifyCraftingParallelCoreBlockEntity(
                            type, pos, state, SimplifyCraftingTier.L1),
                    () -> ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifyCraftingVentBlockEntity>> CRAFTING_VENT =
            registerType("simplify_crafting_vent", SimplifyCraftingVentBlockEntity.class,
                    SimplifyCraftingVentBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_CRAFTING_VENT_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOMachineInterfaceBlockEntity<NECraftingCluster>>> CRAFTING_INTERFACE =
            registerType("simplify_crafting_interface",
                    (Class<ECOMachineInterfaceBlockEntity<NECraftingCluster>>) (Class<?>) ECOMachineInterfaceBlockEntity.class,
                    (type, pos, state) -> new ECOMachineInterfaceBlockEntity<>(
                            type, pos, state, NECraftingClusterCalculator::new),
                    () -> ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get());
    /**
     * Its own type, same class: AE2 names a machine in its network tool through the representative item
     * registered per type, so sharing one type with the plain interface would make the 通讯接口 answer with
     * the plain block's item.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final RegistryObject<BlockEntityType<ECOMachineInterfaceBlockEntity<NECraftingCluster>>>
            CRAFTING_NETWORK_INTERFACE =
            registerType("simplify_crafting_network_interface",
                    (Class<ECOMachineInterfaceBlockEntity<NECraftingCluster>>) (Class<?>) ECOMachineInterfaceBlockEntity.class,
                    (type, pos, state) -> new ECOMachineInterfaceBlockEntity<>(
                            type, pos, state, NECraftingClusterCalculator::new),
                    () -> ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOMachineCasingBlockEntity<NECraftingCluster>>> CRAFTING_CASING =
            registerType("simplify_crafting_casing",
                    (Class<ECOMachineCasingBlockEntity<NECraftingCluster>>) (Class<?>) ECOMachineCasingBlockEntity.class,
                    (type, pos, state) -> new ECOMachineCasingBlockEntity<>(
                            type, pos, state, NECraftingClusterCalculator::new),
                    () -> ModRegistration.SIMPLIFY_CRAFTING_CASING_BLOCK.get());
    /** The two hatches are the only members with a {@code tick(Level, BlockPos, BlockState)}, so they need one. */
    public static final RegistryObject<BlockEntityType<ECOFluidInputHatchBlockEntity>> FLUID_INPUT_HATCH =
            registerType("simplify_fluid_input_hatch", ECOFluidInputHatchBlockEntity.class,
                    ECOFluidInputHatchBlockEntity::new,
                    (level, pos, state, be) -> be.tick(level, pos, state),
                    () -> ModRegistration.SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get());
    public static final RegistryObject<BlockEntityType<ECOFluidOutputHatchBlockEntity>> FLUID_OUTPUT_HATCH =
            registerType("simplify_fluid_output_hatch", ECOFluidOutputHatchBlockEntity.class,
                    ECOFluidOutputHatchBlockEntity::new,
                    (level, pos, state, be) -> be.tick(level, pos, state),
                    () -> ModRegistration.SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get());

    private ModBlockEntities() {
    }
}
