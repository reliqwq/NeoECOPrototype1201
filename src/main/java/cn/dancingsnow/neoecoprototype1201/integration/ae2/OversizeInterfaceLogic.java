package cn.dancingsnow.neoecoprototype1201.integration.ae2;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoprototype1201.mixin.InterfaceLogicAccess;

/**
 * Replaces an {@link InterfaceLogic}'s two inventories with ones that widen how much one marker may stock.
 * This is the AE2 15.4.10 shape of the same idea the 1.21.1 line ships; two things differ and both are
 * measured, not stylistic:
 *
 * <ul>
 *   <li>AE2 15.4.10 has no per-slot filter ({@code AEKeySlotFilter} does not exist, and the interface's own
 *       inventories are built with a {@code null} {@code AEKeyFilter}), so there is nothing to carry over.</li>
 *   <li>The base value is read from {@code super.getMaxAmount} rather than from {@code getCapacity}. On this
 *       line both interface inventories are created with {@code allowOverstacking = false}, so AE2 never
 *       consults the capacity map at all - it defaults to {@code Long.MAX_VALUE} and is only filled by
 *       {@code useRegisteredCapacities()}, which the 1.21.1 port calls and which would here <em>lower</em> a
 *       fluid marker instead of widening it. Widening from the value AE2 actually uses keeps every key type
 *       at least as generous as upstream.</li>
 * </ul>
 */
public final class OversizeInterfaceLogic {
    public static final long MAX_AMOUNT = 8192L;
    /** AE2 caps an item marker at one stack, so this is how much wider every non-item marker gets. */
    public static final long WIDENING = MAX_AMOUNT / 64L;

    private OversizeInterfaceLogic() {
    }

    public static void install(InterfaceLogic logic) {
        var access = (InterfaceLogicAccess) (Object) logic;
        var config = new OversizeConfigInventory(null, GenericStackInv.Mode.CONFIG_STACKS,
                logic.getConfig().size(), access::neoecoprototype$onConfigRowChanged, false);
        var storage = new OversizeConfigInventory(null, GenericStackInv.Mode.STORAGE,
                logic.getStorage().size(), access::neoecoprototype$onStorageChanged, false);
        access.neoecoprototype$setConfig(config);
        access.neoecoprototype$setStorage(storage);
    }

    private static final class OversizeConfigInventory extends ConfigInventory {
        private OversizeConfigInventory(appeng.api.storage.AEKeyFilter filter, GenericStackInv.Mode mode,
                                        int size, Runnable listener, boolean allowOverstacking) {
            super(filter, mode, size, listener, allowOverstacking);
        }

        @Override
        public long getMaxAmount(AEKey key) {
            var base = super.getMaxAmount(key);
            if (key instanceof AEItemKey) {
                return Math.max(MAX_AMOUNT, base);
            }
            return base > Long.MAX_VALUE / WIDENING ? Long.MAX_VALUE : base * WIDENING;
        }
    }
}
