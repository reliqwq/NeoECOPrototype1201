package cn.dancingsnow.neoecoprototype1201.items;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.api.storage.IECOCellHandler;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 全物品创造元件的接入：与 {@link InfiniteConcreteCellHandler} 同一套"无限仓库"行为，清单是
 * 本模组与 eco 两个命名空间下的全部物品。格子无状态、无需持久化，也不收存入——否则它一挂上去，
 * 网络上任何物品都会被它"吸收"，别的存储矩阵永远存不进东西。
 *
 * <p>只在第一次被查询时扫注册表（物品在那之前已经注册完），之后复用同一份不可变清单。
 * 1.21.1 的 {@code ExactAmountSource}/{@code neoecoae$visitExactAmounts}/
 * {@code neoecoae$hasSaturatingListing} 三个口子在这个 eco（20.5.3）上不存在，所以这里的
 * "无限"只到列出的量为止：eco 按普通大数取货并按量计费。</p>
 */
public final class AllItemsCreativeCellHandler implements IECOCellHandler {

    private static final Set<String> NAMESPACES =
            Set.of(Prototype1201.MOD_ID, "neoecoae");

    private static final AllItemsCreativeCellHandler INSTANCE = new AllItemsCreativeCellHandler();

    @Nullable
    private static volatile List<AEItemKey> cachedKeys;

    public static void register() {
        ECOStorageCells.register(INSTANCE);
    }

    /** Every item our two mods put in the registry, in registry order. */
    static List<AEItemKey> keys() {
        List<AEItemKey> keys = cachedKeys;
        if (keys != null) {
            return keys;
        }
        List<AEItemKey> built = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            if (!NAMESPACES.contains(id.getNamespace())) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == Items.AIR) {
                continue;
            }
            AEItemKey key = AEItemKey.of(item);
            if (key != null) {
                built.add(key);
            }
        }
        keys = List.copyOf(built);
        cachedKeys = keys;
        return keys;
    }

    @Override
    public boolean isCell(ItemStack stack) {
        return stack.getItem() instanceof AllItemsCreativeCellItem;
    }

    @Override
    @Nullable
    public IECOStorageCell getCellInventory(ItemStack stack, @Nullable ISaveProvider saveProvider) {
        return isCell(stack) ? new Cell() : null;
    }

    private static boolean isListed(AEKey what) {
        return what instanceof AEItemKey && NAMESPACES.contains(what.getId().getNamespace());
    }

    /** A stateless endless source listing both mods' items. */
    private static final class Cell implements IECOStorageCell {
        private static final Component DESCRIPTION = Component.translatable(
                "item.neoecoprototype1201.all_items_creative_cell");

        @Override
        public CellState getStatus() {
            return CellState.NOT_EMPTY;
        }

        @Override
        public double getIdleDrain() {
            return AllItemsCreativeCellItem.IDLE_DRAIN;
        }

        @Override
        public boolean canFitInsideCell() {
            // An endless source must not disappear into another storage cell.
            return false;
        }

        @Override
        public void persist() {
            // stateless
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            // Deliberately false: absorbing every listed item would starve every other cell on the network.
            return false;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            // A source, never a sink: deposits are left for real storage.
            return 0;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            return isListed(what) ? amount : 0;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (AEItemKey key : keys()) {
                out.add(key, AllItemsCreativeCellItem.REPORTED_AMOUNT_PER_TYPE);
            }
        }

        @Override
        public Component getDescription() {
            return DESCRIPTION;
        }

        @Override
        public cn.dancingsnow.neoecoae.api.IECOTier getTier() {
            return SimplifyTier.L1;
        }

        @Override
        public ECOCellType getCellType() {
            return AllItemsCreativeCellItem.CELL_TYPE;
        }

        @Override
        public long getStoredItemTypes() {
            return keys().size();
        }

        @Override
        public long getTotalItemTypes() {
            return keys().size();
        }

        @Override
        public long getUsedBytes() {
            return 0;
        }

        @Override
        public long getTotalBytes() {
            return AllItemsCreativeCellItem.REPORTED_BYTES;
        }

        @Override
        public boolean hasInfiniteTypeCapacity() {
            return true;
        }

        @Override
        public boolean isInfiniteStorageEligible() {
            return false;
        }
    }
}
