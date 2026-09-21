package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * First vertical slice on 1.20.1: one panel row of our own plus one item cell, so the registration
 * path is proven before any multiblock is ported.
 *
 * <p>eco builds the storage host's type rows by enumerating {@code neoecoae:cell_type}, so a row is
 * just another registry entry. Unlike the 1.21.1 line, {@link ECOCellType} there carries its own id
 * and has no visibility flag - every row registered here shows up in the host panel.
 */
public final class Registration {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Prototype1201.MOD_ID);
    public static final DeferredRegister<ECOCellType> CELL_TYPES =
            DeferredRegister.create(NERegistries.Keys.CELL_TYPE, Prototype1201.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Prototype1201.MOD_ID);

    /** Our own host panel row. */
    public static final RegistryObject<ECOCellType> PROBE = CELL_TYPES.register("probe",
            () -> ECOCellType.builder()
                    .id(ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "probe"))
                    .desc(Component.translatable("cell_type." + Prototype1201.MOD_ID + ".probe"))
                    .typeCount(PrototypeCellItem.TOTAL_TYPES)
                    .build());

    public static final RegistryObject<Item> PROBE_CELL = ITEMS.register("probe_cell",
            () -> new PrototypeCellItem(new Item.Properties().stacksTo(1), PROBE::get));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Prototype1201.MOD_ID + ".main"))
                    .icon(() -> PROBE_CELL.get().getDefaultInstance())
                    .displayItems((params, output) -> output.accept(PROBE_CELL.get()))
                    .build());

    private Registration() {
    }
}
