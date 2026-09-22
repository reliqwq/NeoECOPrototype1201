package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
 * <p>eco builds the storage host's type rows by enumerating {@code neoecoae:cell_type}. Unlike the
 * 1.21.1 line, {@link ECOCellType} here carries its own id and has no visibility flag, so every row
 * registered in this project shows up in the host panel.
 */
public final class Registration {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Prototype1201.MOD_ID);
    public static final DeferredRegister<ECOCellType> CELL_TYPES =
            DeferredRegister.create(NERegistries.Keys.CELL_TYPE, Prototype1201.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Prototype1201.MOD_ID);

    /** Same ladder as the 1.21.1 L1 cells. */
    private static final String[] SUFFIXES = {"1k", "16k", "64k", "1m", "4m"};
    private static final long[] BYTES = {1L << 10, 1L << 14, 1L << 16, 1L << 20, 1L << 22};

    /** Our own host panel row, shared by the whole family. */
    public static final RegistryObject<ECOCellType> ROW = CELL_TYPES.register("prototype",
            () -> ECOCellType.builder()
                    .id(ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "prototype"))
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

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Prototype1201.MOD_ID + ".main"))
                    .icon(() -> CELLS.get(0).get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        ModRegistration.TAB_BLOCK_ITEMS
                                .forEach(item -> output.accept(item.get()));
                        CELLS.forEach(cell -> output.accept(cell.get()));
                    })
                    .build());

    private Registration() {
    }
}
