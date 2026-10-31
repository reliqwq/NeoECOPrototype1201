package cn.dancingsnow.neoecoprototype1201.blockentity.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype1201.multiblock.definition.SimplifyComputationDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * C1 computation host. The one thing this class exists for is {@link #getBuildDefinition()}: eco answers a
 * host's shape with a {@code tableswitch} over its own tier indexes (1..3 = L4/L6/L9) and an addon tier at
 * index 0 falls through to {@code aconst_null}, and every caller of that value null-checks it - which is why
 * the panel's 搭建 button does nothing on a subclass-less C1.
 *
 * <p>Subclassing is affordable here and that is measured, not assumed: on eco 20.5.3 nothing looks this
 * machine up by AE2's exact class key. All ten consumers go through {@code grid.NEMachineLookup}, which
 * enumerates {@code IGrid.getMachineClasses()} and matches with {@code isAssignableFrom}, so a subclass stays
 * visible; the single direct {@code IGrid.getMachines(Class)} call in the whole jar asks for
 * {@code ECOMachineInterfaceBlockEntity}. (The 1.21.1 line keeps eco's class for this host for its own
 * measured reason - see the comment on that line's block class - so do not read this as a correction of it.)
 */
public class SimplifyComputationSystemBlockEntity extends ECOComputationSystemBlockEntity {
    public SimplifyComputationSystemBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                                IECOTier tier) {
        super(type, pos, state, tier);
    }

    /** L1 is a shell of addon blocks, so its geometry is our own definition, not eco's L4 layout. */
    @Override
    public MultiBlockDefinition getBuildDefinition() {
        return SimplifyComputationDefinition.L1;
    }
}
