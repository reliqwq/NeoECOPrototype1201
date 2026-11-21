package cn.dancingsnow.neoecoprototype1201.api;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype1201.items.SimplifyStorageCellItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Factory entry point for new finite matrices; legacy registrations intentionally do not use it.
 *
 * <p>It lives in {@code api/} and names {@link SimplifyStorageCellItem} on purpose: this mirrors the
 * 1.21.1 line's one documented layer exception, because the method is advertised to pack authors in
 * {@code docs/kubejs.md} and a factory has to name the concrete item it builds.
 *
 * <p>Ported from the 1.21.1 line with this line's registry shapes: a Forge {@link DeferredRegister}
 * in, a {@link RegistryObject} out. The {@code infinite} half stays unported - this line's infinite
 * matrices are created by the KubeJS builders or shipped as fixed items, and no Java caller has asked
 * for a registration door yet; the argument check below still refuses an infinite definition so a
 * caller cannot build one by mistake without noticing.
 */
public final class StorageMatrixRegistration {
    private StorageMatrixRegistration() {
    }

    public static RegistryObject<Item> registerFinite(
            DeferredRegister<Item> items,
            String registryName,
            StorageMatrixDefinition definition,
            AEKeyType keyType,
            Supplier<ECOCellType> cellType) {
        if (definition.infinite()) {
            throw new IllegalArgumentException("An infinite definition does not belong in registerFinite;"
                    + " build it through the KubeJS infinite_storage_matrix builder instead");
        }
        if (!definition.id().getPath().equals(registryName)) {
            throw new IllegalArgumentException("Matrix definition path does not match item registration: "
                    + definition.id() + " vs " + registryName);
        }
        // The 1.21.1 original also checked definition.id().getNamespace() against the register's own
        // namespace; Forge 1.20.1's DeferredRegister keeps its modid private, so the namespace half of
        // the check is unreachable here and the path half below carries the guard alone.
        return items.register(registryName, () -> {
            var item = new SimplifyStorageCellItem(new Item.Properties().stacksTo(1), keyType, cellType,
                    definition.bytes(), definition.bytesPerType(), definition.totalTypes());
            StorageMatrixRegistry.register(definition, item);
            return item;
        });
    }
}
