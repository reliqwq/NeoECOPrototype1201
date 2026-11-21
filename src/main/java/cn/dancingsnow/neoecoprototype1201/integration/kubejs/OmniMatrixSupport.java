package cn.dancingsnow.neoecoprototype1201.integration.kubejs;

import cn.dancingsnow.neoecoprototype1201.integration.ae2omnicells.SimplifyQuantumStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.integration.ae2omnicells.SimplifyUniversalStorageCellItem;
import net.minecraft.world.item.Item;

/**
 * The Omni Cells half of the same arrangement as {@link ChemicalMatrixSupport}: the universal cell
 * family here extends eco's {@code ECOUniversalStorageCellItem}, whose own parent is an Omni Cells
 * class, so naming it in the builder is enough to break a pack that never installed the mod - the
 * reference is resolved when the builder type is registered, not when a script uses it. This line's
 * omni items already live behind the same gate ({@code UniversalIntegration}), and the two
 * constructors here are simpler than their 1.21.1 cousins: capacity is the only argument.
 */
final class OmniMatrixSupport {
    private OmniMatrixSupport() {
    }

    /**
     * @param quantum true for the quantum family, which carries no type limit at all
     */
    static Item create(Item.Properties properties, boolean quantum, int types, long bytes) {
        // The type count is carried by the two item classes themselves (512 universal, none quantum);
        // a script that wrote totalTypes(...) gets the family's own answer, which the definition below
        // records through typesForDefinition.
        return quantum
                ? new SimplifyQuantumStorageCellItem(properties, bytes)
                : new SimplifyUniversalStorageCellItem(properties, bytes);
    }

    /** The quantum matrix reports no ceiling, so the definition has to be written with one. */
    static int typesForDefinition(boolean quantum, int types) {
        return quantum ? Integer.MAX_VALUE : SimplifyUniversalStorageCellItem.TOTAL_TYPES;
    }
}
