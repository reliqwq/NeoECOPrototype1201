package cn.dancingsnow.neoecoprototype1201.integration.kubejs;

import appeng.api.stacks.AEKey;
import cn.dancingsnow.neoecoprototype1201.PrototypeCellItem;
import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoprototype1201.integration.appmek.AppMekIntegration;
import cn.dancingsnow.neoecoprototype1201.integration.appmek.ChemicalKeyType;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Every Mekanism and Applied Mekanistics name this addon can reach for through a script-made matrix
 * lives here, behind calls the builder makes only after checking both mods are installed.
 *
 * <p>Not a style preference. Registering the KubeJS builder type resolves the builder class itself,
 * so a single reference left in {@code StorageMatrixBuilder} takes down every pack that has KubeJS
 * but not Mekanism - the 1.21.1 line measured it as hundreds of {@code NoClassDefFoundError} during
 * the registry event, with the server never starting and no test running.
 *
 * <p>Ported from the 1.21.1 line with this line's own shapes. Mekanism 10.4 (this line's version)
 * has no single chemical registry: {@code MekanismAPI} names four sub-registries (gas, infusion,
 * pigment, slurry) and the {@code ChemicalStack} is abstract, so a bound id is probed across the
 * four and turned into a key with {@code chemical.getStack(1)} - {@code MekanismKey.of} is appmek
 * 1.20.1's own factory. And the chemical cells here are plain {@code PrototypeCellItem}s keyed by
 * {@code ChemicalKeyType} (this line never grew a dedicated chemical cell class), so the per-type
 * fee is the family's own {@code bytes >> 8}, not the item/fluid {@code typeFee}; the bound-chemical
 * blacklist the 1.21.1 item carried has no counterpart here.
 */
final class ChemicalMatrixSupport {
    private ChemicalMatrixSupport() {
    }

    /**
     * The bound chemical's key. Nothing here runs until a matrix actually asks for it, so a pack that
     * never writes {@code chemicalType(...)} never loads this class at all.
     */
    static AEKey resolve(ResourceLocation id, ResourceLocation matrix) {
        Chemical chemical = findChemical(id);
        if (chemical == null) {
            throw new IllegalStateException("Unknown Mekanism chemical '" + id + "' for matrix " + matrix);
        }
        return MekanismKey.of(chemical.getStack(1));
    }

    /**
     * Probe the four chemical sub-registries in {@code MekanismAPI}'s declared order. A missing id
     * answers null rather than throwing: the sentence that names the id lives at the call site, which
     * knows both the matrix and the script.
     */
    private static Chemical findChemical(ResourceLocation id) {
        List<ResourceKey<? extends Registry<? extends Chemical>>> names = List.of(
                MekanismAPI.GAS_REGISTRY_NAME,
                MekanismAPI.INFUSE_TYPE_REGISTRY_NAME,
                MekanismAPI.PIGMENT_REGISTRY_NAME,
                MekanismAPI.SLURRY_REGISTRY_NAME);
        for (ResourceKey<? extends Registry<? extends Chemical>> name : names) {
            var registry = BuiltInRegistries.REGISTRY.get((ResourceKey) name);
            if (!(registry instanceof Registry<?> typed)) {
                continue;
            }
            var found = typed.get(id);
            if (found instanceof Chemical chemical && !chemical.isEmptyType()) {
                return chemical;
            }
        }
        return null;
    }

    /** The chemical family's type count. Read only past the guard, so loading it is safe there. */
    static int totalTypes() {
        return AppMekIntegration.TOTAL_TYPES;
    }

    /** What the shipped chemical family charges per type; the definition records the same figure. */
    static int bytesPerTypeFor(long bytes) {
        return (int) (bytes >> 8);
    }

    /** Called from behind the guard in {@code createObject}, which is what makes loading it safe. */
    static Item create(Item.Properties properties, long bytes) {
        return new PrototypeCellItem(properties, ChemicalKeyType.get(),
                () -> Registration.ecoCellType("mekanism"), bytes, totalTypes());
    }
}
