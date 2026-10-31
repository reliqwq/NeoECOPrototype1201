package cn.dancingsnow.neoecoprototype1201.items;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.items.ECOComputationCellItem;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;

/** The L1 computation cell. eco's own item already answers every reader, so only the tier is ours. */
public class SimplifyComputationCellItem extends ECOComputationCellItem {
    public SimplifyComputationCellItem(Properties properties) {
        this(properties, SimplifyTier.L1);
    }

    public SimplifyComputationCellItem(Properties properties, IECOTier tier) {
        super(properties, tier);
    }
}
