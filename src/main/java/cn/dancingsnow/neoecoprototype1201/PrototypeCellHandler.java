package cn.dancingsnow.neoecoprototype1201;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.ECOKeyTypeCellHandler;

import java.util.function.Supplier;

/**
 * The drive only accepts a stack once some handler claims it, so a panel row and an item are not
 * enough on their own - this is the piece every eco resource family registers.
 */
public final class PrototypeCellHandler extends ECOKeyTypeCellHandler {
    public static final PrototypeCellHandler INSTANCE =
            new PrototypeCellHandler(Registration.ROW::get, AEKeyType::items);

    private PrototypeCellHandler(Supplier<ECOCellType> cellType, Supplier<AEKeyType> keyType) {
        super(cellType, keyType);
    }
}
