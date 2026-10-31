package cn.dancingsnow.neoecoprototype1201.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;

/** The energized core takes the one cell directly behind the computation host. */
public class SimplifyEnergizedComputationCoreBlock extends SimplifyComputationParallelCoreBlock {
    public SimplifyEnergizedComputationCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }
}
