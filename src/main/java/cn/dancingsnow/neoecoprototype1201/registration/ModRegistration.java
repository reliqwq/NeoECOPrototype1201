package cn.dancingsnow.neoecoprototype1201.registration;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyDriveBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyEnergyCellBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageCasingBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageInterfaceBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageNetworkInterfaceBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageVentBlock;
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
                    () -> new SimplifyStorageCasingBlock(
                            metal()));
    public static final RegistryObject<Block> SIMPLIFY_DRIVE_BLOCK =
            registerBlock("simplify_drive",
                    () -> new SimplifyDriveBlock(
                            metal().noOcclusion()));
    public static final RegistryObject<Block> SIMPLIFY_STORAGE_INTERFACE_BLOCK =
            registerBlock("simplify_storage_interface",
                    () -> new SimplifyStorageInterfaceBlock(
                            metal()));
    public static final RegistryObject<Block> SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK =
            registerBlock("simplify_storage_network_interface",
                    () -> new SimplifyStorageNetworkInterfaceBlock(
                            metal()));
    public static final RegistryObject<Block> SIMPLIFY_STORAGE_VENT_BLOCK =
            registerBlock("simplify_storage_vent",
                    () -> new SimplifyStorageVentBlock(
                            metal()));
    public static final RegistryObject<Block> SIMPLIFY_ENERGY_CELL_BLOCK =
            registerBlock("simplify_energy_cell",
                    () -> new SimplifyEnergyCellBlock(
                            metal().noOcclusion()));

    private ModRegistration() {
    }
}
