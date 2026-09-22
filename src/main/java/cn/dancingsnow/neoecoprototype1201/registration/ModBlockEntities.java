package cn.dancingsnow.neoecoprototype1201.registration;

import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyStorageCasingBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyStorageInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyStorageVentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Prototype1201.MOD_ID);

    @FunctionalInterface
    private interface BEFactory<T extends BlockEntity> {
        T create(BlockEntityType<?> type, BlockPos pos, BlockState state);
    }

    @SafeVarargs
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerType(
            String name, Class<T> beClass, BEFactory<T> factory, Supplier<? extends Block>... validBlocks) {
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
            wire(blocks, beClass, built);
            return built;
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean wire(Block[] blocks, Class beClass, BlockEntityType type) {
        for (Block block : blocks) {
            if (block instanceof AEBaseEntityBlock entityBlock) {
                entityBlock.setBlockEntity(beClass, type, null, null);
            }
        }
        return true;
    }

    public static final RegistryObject<BlockEntityType<SimplifyStorageHostBlockEntity>> STORAGE_HOST =
            registerType("storage_host", SimplifyStorageHostBlockEntity.class, SimplifyStorageHostBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifyStorageCasingBlockEntity>> STORAGE_CASING =
            registerType("storage_casing", SimplifyStorageCasingBlockEntity.class, SimplifyStorageCasingBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifyDriveBlockEntity>> DRIVE =
            registerType("drive", SimplifyDriveBlockEntity.class, SimplifyDriveBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_DRIVE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifyStorageInterfaceBlockEntity>> STORAGE_INTERFACE =
            registerType("storage_interface", SimplifyStorageInterfaceBlockEntity.class, SimplifyStorageInterfaceBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                    () -> ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifyStorageVentBlockEntity>> STORAGE_VENT =
            registerType("storage_vent", SimplifyStorageVentBlockEntity.class, SimplifyStorageVentBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_STORAGE_VENT_BLOCK.get());
    public static final RegistryObject<BlockEntityType<SimplifyEnergyCellBlockEntity>> ENERGY_CELL =
            registerType("energy_cell", SimplifyEnergyCellBlockEntity.class, SimplifyEnergyCellBlockEntity::new,
                    () -> ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get());

    private ModBlockEntities() {
    }
}
