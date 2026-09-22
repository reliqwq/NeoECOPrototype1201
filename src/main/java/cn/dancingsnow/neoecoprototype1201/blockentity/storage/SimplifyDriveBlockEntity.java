package cn.dancingsnow.neoecoprototype1201.blockentity.storage;

import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype1201.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyDriveBlock;
import cn.dancingsnow.neoecoprototype1201.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCellItem;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.multiblock.cluster.SimplifyStorageCluster;
import appeng.api.networking.IGridNodeListener;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import cn.dancingsnow.neoecoprototype1201.util.SimplifyServerLifecycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.ModList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * L1 drive: hosts exactly one ECO-compatible storage cell (any cell accepted by
 * eco's {@link ECOStorageCells}, which includes this addon's L1 cells).
 * <p>
 * The drive follows eco's current storage contract: it is an
 * {@link IStorageProvider} and mounts its own cell through its multiblock grid
 * node once the cluster is formed.
 */
public class SimplifyDriveBlockEntity extends NEBlockEntity<SimplifyStorageCluster, SimplifyDriveBlockEntity>
        implements ISaveProvider, IStorageProvider {

    private ItemStack cellStack = ItemStack.EMPTY;
    @Nullable
    private ItemStack cachedCellStack;
    @Nullable
    private IECOStorageCell cachedCellInventory;

    private boolean online;
    private boolean mounted;
    private boolean providerServiceRegistered;
    private CellState cellState = CellState.ABSENT;

    public SimplifyDriveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }

    @Override
    protected void writeUiSyncTag(CompoundTag tag) {
        super.writeUiSyncTag(tag);
        if (!cellStack.isEmpty()) {
            tag.put("cell", cellStack.save(new CompoundTag()));
        }
        tag.putBoolean("online", online);
        tag.putBoolean("mounted", mounted);
        tag.putString("cellState", cellState.name());
    }

    @Override
    protected void readUiSyncTag(CompoundTag tag) {
        super.readUiSyncTag(tag);
        cellStack = tag.contains("cell") ? ItemStack.of(tag.getCompound("cell")) : ItemStack.EMPTY;
        online = tag.getBoolean("online");
        mounted = tag.getBoolean("mounted");
        CellState read;
        try {
            read = CellState.valueOf(tag.getString("cellState"));
        } catch (IllegalArgumentException ignored) {
            read = CellState.ABSENT;
        }
        cellState = read;
        invalidateCellInventoryCache();
    }

    /**
     * AE2's version only pokes the neighbours (it passes {@code UPDATE_NEIGHBORS}) because it expects
     * client-visible state to live in the block state. Our cell rides in the block entity tag, so
     * without the packet a removed cell keeps rendering until the chunk reloads.
     */
    @Override
    public void markForUpdate() {
        super.markForUpdate();
        if (level != null && !level.isClientSide && !isRemoved()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Mark whether the cluster's storage interface currently mounts our cell. */
    public void setMounted(boolean mounted) {
        if (this.mounted != mounted) {
            this.mounted = mounted;
            setChanged();
            markForUpdate();
        }
    }

    public boolean isMounted() {
        return mounted;
    }

    public boolean isOnline() {
        return online;
    }

    public CellState getCellState() {
        return cellState;
    }

    public boolean isItemValid(ItemStack stack) {
        return ECOStorageCells.isCellHandled(stack);
    }

    @Nullable
    public ItemStack getCellStack() {
        return cellStack.isEmpty() ? null : cellStack;
    }

    public boolean hasCell() {
        return !cellStack.isEmpty();
    }

    /** @return true when the cell was inserted. */
    public boolean insertCell(ItemStack stack) {
        if (!cellStack.isEmpty() || !isItemValid(stack)) {
            return false;
        }
        invalidateCellInventoryCache();
        this.cellStack = stack.copyWithCount(1);
        updateCellState();
        syncCellBlockState();
        updateIdlePower();
        notifyClusterStorageChanged();
        setChanged();
        markForUpdate();
        return true;
    }

    /** @return the removed cell, or {@link ItemStack#EMPTY} when empty / locked. */
    public ItemStack removeCell() {
        if (cellStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ECOStorageCells.releaseCellInventory(cellStack, this);
        ItemStack removed = cellStack;
        this.cellStack = ItemStack.EMPTY;
        invalidateCellInventoryCache();
        updateCellState();
        syncCellBlockState();
        updateIdlePower();
        notifyClusterStorageChanged();
        setChanged();
        markForUpdate();
        return removed;
    }

    /** Ask this drive's provider node to remount its cell. */
    private void notifyClusterStorageChanged() {
        if (level != null && !level.isClientSide && !SimplifyServerLifecycle.isStopping(level)) {
            requestProviderUpdate();
        }
    }

    private void requestProviderUpdate() {
        // The interface member registers no storage service on this line, so only our own node is
        // refreshed - and only once the service exists and the node is actually in a grid.
        if (providerServiceRegistered && getMainNode().isOnline()) {
            SimplifyGridFacade.requestStorageUpdate(getMainNode());
        }
    }

    @Nullable
    public IECOStorageCell getCellInventory() {
        if (cellStack.isEmpty()) {
            invalidateCellInventoryCache();
            return null;
        }
        // Reference-identity check on purpose: cellStack is only ever replaced
        // wholesale (insertCell/removeCell/loadTag), never mutated in place, so an
        // unchanged reference means unchanged cell contents. This is also the
        // safety net if a future path forgets to call invalidateCellInventoryCache();
        // any code that starts mutating cellStack in place must invalidate here too.
        if (cachedCellStack != cellStack) {
            cachedCellStack = cellStack;
            cachedCellInventory = ECOStorageCells.getCellInventory(cellStack, this);
        }
        return cachedCellInventory;
    }

    private void invalidateCellInventoryCache() {
        cachedCellStack = null;
        cachedCellInventory = null;
    }

    @Override
    public void onChunkUnloaded() {
        ECOStorageCells.releaseCellInventory(cellStack, this);
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        ECOStorageCells.releaseCellInventory(cellStack, this);
        super.setRemoved();
    }

    private void syncCellBlockState() {
        if (level == null || level.isClientSide || SimplifyServerLifecycle.isStopping(level)) {
            return;
        }
        BlockState state = getBlockState();
        SimplifyDriveBlock.CellKind kind = getCellKind();
        BlockState newState = state.setValue(SimplifyDriveBlock.HAS_CELL, hasCell())
                .setValue(SimplifyDriveBlock.CELL_KIND, kind);
        if (newState != state) {
            level.setBlock(worldPosition, newState, Block.UPDATE_CLIENTS);
        }
    }

    private SimplifyDriveBlock.CellKind getCellKind() {
        if (cellStack.isEmpty() || !(cellStack.getItem() instanceof IECOStorageCellItem cellItem)) {
            return SimplifyDriveBlock.CellKind.ITEM;
        }
        if (cellItem.getKeyTypes().contains(appeng.api.stacks.AEKeyType.fluids())) {
            return SimplifyDriveBlock.CellKind.FLUID;
        }
        if (cellItem.getKeyTypes().stream().anyMatch(type -> "chemical".equals(type.getId().getPath()))) {
            return SimplifyDriveBlock.CellKind.CHEMICAL;
        }
        return SimplifyDriveBlock.CellKind.ITEM;
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        SimplifyStorageCluster cluster = getCluster();
        SimplifyStorageHostBlockEntity controller = cluster == null ? null : cluster.getController();
        IECOStorageCell cell = getCellInventory();
        if (controller != null && cell != null && isAcceptedByL1Host()) {
            storageMounts.mount(cell, controller.getStoragePriority());
            setMounted(true);
            updateCellState();
            return;
        }
        setMounted(false);
        updateCellState();
    }

    /** Mounts when our L1 controller outranks the cell; foreign universal cells opt in by namespace. */
    private boolean isAcceptedByL1Host() {
        IECOStorageCell cell = getCellInventory();
        SimplifyStorageCluster cluster = getCluster();
        SimplifyStorageHostBlockEntity controller = cluster == null ? null : cluster.getController();
        if (cell == null || controller == null) {
            return false;
        }
        if (controller.getTier().compareTo(cell.getTier()) >= 0) {
            return true;
        }
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(cellStack.getItem()).toString();
        return isOmniCellsItem(itemId);
    }

    /** OmniCells is optional; use only its stable item namespace and never load its classes here. */
    private static boolean isOmniCellsItem(String itemId) {
        return ModList.get().isLoaded("ae2omnicells") && itemId.startsWith("ae2omnicells:");
    }

    @Override
    public void onReady() {
        getMainNode().addService(IStorageProvider.class, this);
        providerServiceRegistered = true;
        super.onReady();
        online = getMainNode().isOnline();
        updateCellState();
        updateIdlePower();
        requestProviderUpdate();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (SimplifyServerLifecycle.isStopping(level)) {
            return;
        }
        super.onMainNodeStateChanged(reason);
        boolean nextOnline = getMainNode().isOnline();
        if (online != nextOnline) {
            online = nextOnline;
            setChanged();
            markForUpdate();
        }
        updateCellState();
        updateIdlePower();
        requestProviderUpdate();
    }

    private void updateIdlePower() {
        SimplifyPowerProfile profile = SimplifyPowerProfile.L1;
        double power = profile.storageDriveIdlePower();
        if (isFormed() && getCluster() != null && getCluster().getController() != null) {
            IECOStorageCell cell = getCellInventory();
            if (cell != null
                    && getCluster().getController().getTier().compareTo(cell.getTier()) >= 0) {
                power += profile.scale(cell.getIdleDrain());
            }
        }
        getMainNode().setIdlePowerUsage(power);
    }

    /**
     * Spill the inserted cell when the drive is broken / removed, exactly like
     * eco's own drive ({@code ECODriveBlockEntity#addAdditionalDrops}). AE2's
     * base block invokes this hook on removal and pops the returned stacks, so
     * the cell (whose data lives on the ItemStack itself) survives intact.
     * <p>NOTE: do NOT touch the level / block state in here - the block is
     * mid-removal; placing a block state (e.g. clearing HAS_CELL) would
     * resurrect the drive at this position and break its own loot drop.
     */
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        if (!cellStack.isEmpty()) {
            ECOStorageCells.releaseCellInventory(cellStack, this);
            drops.add(cellStack);
            // Clear so a second invocation of this hook cannot drop it twice.
            cellStack = ItemStack.EMPTY;
            invalidateCellInventoryCache();
        }
    }

    private void updateCellState() {
        if (level == null || level.isClientSide) {
            return;
        }
        IECOStorageCell cellInventory = getCellInventory();
        CellState nextState = cellInventory == null ? CellState.ABSENT : cellInventory.getStatus();
        if (cellState != nextState) {
            cellState = nextState;
            markForUpdate();
        }
    }

    @Override
    public void saveChanges() {
        if (cachedCellInventory != null) {
            cachedCellInventory.persist();
        }
        notifyPersistence();
    }

    public void notifyPersistence() {
        if (level instanceof ServerLevel) {
            updateCellState();
            setChanged();
            markForUpdate();
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.put("cell", cellStack.save(new CompoundTag()));
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        invalidateCellInventoryCache();
        if (data.contains("cell")) {
            CompoundTag cellTag = data.getCompound("cell");
            cellStack = cellTag.isEmpty()
                    ? ItemStack.EMPTY
                    : ItemStack.of(cellTag);
        }
        updateCellState();
    }
}
