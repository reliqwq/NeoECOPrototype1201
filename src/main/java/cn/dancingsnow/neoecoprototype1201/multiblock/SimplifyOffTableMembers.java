package cn.dancingsnow.neoecoprototype1201.multiblock;

import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/**
 * The blocks a family's cluster calculator accepts in a cell that family's definition table names a
 * different block for.
 *
 * <p>eco's shape definition is a program run against a virtual level, not a table of candidates: every
 * instruction ends in {@code MultiBlockContext.setBlock(pos, one state)}, {@code getLevel()} hands back
 * the dummy world rather than the standing one, and the resulting plan is read back as one wanted block
 * per position. So "this cell may wear either of two blocks" cannot be written into a definition at all.
 * Anything that then measures a machine off its plan - the expansion card counting how long a host
 * already is - sees a foreign block where the cluster sees a member, and calls a running machine
 * unformed.</p>
 *
 * <p>These are the whole gap, each pair named after the acceptance site that motivated it. Every block
 * here belongs to one family and to no plan, so identity alone answers both questions and nothing has to
 * know which machine it is looking at. eco's own L4/L6/L9 machines are in here too: their calculators
 * carry the same casing-or-switch clause, which is what first proved that the tables cannot express it
 * (I had written "eco's tables list exactly what their calculators take" without measuring, and a real
 * machine proved the opposite within one click).</p>
 *
 * <p>Known hole, deliberately not guessed at: {@code NECraftingClusterCalculator} also names
 * {@code NEBlocks.FX_MONITOR_CORE}, which no eco table lists. Which cell it wears is unmeasured, so a
 * formed L4 crafting host carrying one still stops the card - with a coordinate, which is how this one
 * gets found.</p>
 *
 * <p>The formation gate is not relaxed by this. Callers have to hear {@code isFormed()} say yes first,
 * so these sets cannot let a machine that really came apart pass as built - which is also what makes
 * them safe to read without a position: anything standing in a formed machine's own cell was accepted
 * there by the calculator.</p>
 */
public final class SimplifyOffTableMembers {
    /**
     * eco's block by registry name. The other line reaches these through {@code NEBlocks.X.get()}, but those
     * fields are Registrate {@code BlockEntry}s and eco ships Regstrate nested inside its own jar, so the type
     * is not on this line's compile classpath - the registry lookup answers the identical Block instance, and
     * these calls only run while a machine forms, long after blocks are registered.
     */
    private static Block ecoBlock(String name) {
        return net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                new net.minecraft.resources.ResourceLocation("neoecoae", name));
    }

    /** The kind this family's calculator also accepts where its plan wants {@code wanted}. */
    public static Set<Block> alternatesFor(Block wanted) {
        // matchedComputationInterface takes either interface kind in the one interface cell, and the
        // storage and crafting calculators do the same for their own family.
        if (wanted == ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get()) {
            return Set.of(ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get());
        }
        if (wanted == ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get()) {
            return Set.of(ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get());
        }
        if (wanted == ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get()) {
            return Set.of(ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get());
        }
        // isShell lets the energized parallel core wear one casing cell of the outer column.
        if (wanted == ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK.get()) {
            return Set.of(ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get());
        }
        // matchingThreadingCore lets the energized kind replace the first threading core.
        if (wanted == ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get()) {
            return Set.of(ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get());
        }
        // eco's own clause, twice: validateCasingOrNetworkSwitch in both its computation and crafting
        // calculators takes either network switch in a cell its table can only write as casing.
        if (wanted == ecoBlock("computation_casing")) {
            return Set.of(ecoBlock("computation_network_switch"),
                    ecoBlock("computation_high_energy_network_switch"));
        }
        if (wanted == ecoBlock("crafting_casing")) {
            return Set.of(ecoBlock("crafting_network_switch"),
                    ecoBlock("crafting_high_energy_network_switch"));
        }
        return Set.of();
    }

    /**
     * Whether {@code standing} is one of those alternates - a block only a calculator can name, so a plan
     * that wants a different block there is not evidence it does not belong to the machine.
     *
     * <p>Kept by hand next to {@link #alternatesFor}: a new pair there without a line here means the
     * card stops on the swap path while still growing fine, which is the half that carries contents.</p>
     */
    public static boolean isOffTableMember(Block standing) {
        return standing == ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get()
                || standing == ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get()
                || standing == ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get()
                || standing == ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get()
                || standing == ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get()
                || standing == ecoBlock("computation_network_switch")
                || standing == ecoBlock("computation_high_energy_network_switch")
                || standing == ecoBlock("crafting_network_switch")
                || standing == ecoBlock("crafting_high_energy_network_switch");
    }

    private SimplifyOffTableMembers() {
    }
}
