package cn.dancingsnow.neoecoprototype1201.blockentity.crafting;

import appeng.api.orientation.OrientationStrategies;
import appeng.api.orientation.RelativeSide;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingSystem;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingWorkerBlockEntity;
import cn.dancingsnow.neoecoae.gui.crafting.CraftingControllerPanelState;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype1201.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype1201.multiblock.definition.SimplifyCraftingDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * F1 crafting host.
 *
 * <p>Two jobs beyond eco's own machine. It hands out the L1 shape, because eco resolves a host's build
 * definition with a {@code tableswitch} over its own tier indexes and an addon tier at index 0 falls through
 * to null -- and this BE is the only place that answer can be given, since it is read through the machine,
 * not the block. It also publishes which interface the finished machine holds onto the block state, so the
 * blockstate file can pick a formed model per case.
 *
 * <p>eco's own {@code updateState} already writes {@code mirrored} with a block change, and both that and this
 * would run inside AE2's cluster recalculation, which holds a global "modification in progress" latch. A second
 * write on the same stack is what wedged the C1 host, so the value is recorded here and applied on the next
 * server tick instead.
 */
public class SimplifyCraftingSystemBlockEntity extends ECOCraftingSystemBlockEntity {
    /** The core column the crafting artwork numbers, so a slot with a core in it is not read as empty. */
    private static final int CORE_ART_COLUMN = 1;

    private boolean communicationInterface;
    private boolean appearancePending;

    public SimplifyCraftingSystemBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                             IECOTier tier) {
        super(type, pos, state, tier);
    }

    /** F1 is a shell of addon blocks, so its geometry is our own definition, not eco's L4 layout. */
    @Override
    public MultiBlockDefinition getBuildDefinition() {
        return SimplifyCraftingDefinition.L1;
    }

    /**
     * The brackets above and below each working core on the crafting panel's queue strip.
     *
     * <p>eco writes one 2-bit number per slot out of the core's own tier index, and the same number - 0 -
     * means "no core stands here". An addon tier whose index is 0 is therefore indistinguishable from an
     * empty slot, and the artwork returns for anything outside 1..3, so an F1 host draws no brackets at all.
     * Rebuilding the two lists from our own cluster is what separates those two zeros: a slot gets
     * {@link #CORE_ART_COLUMN} only where a core really sits above or below that worker, and stays 0 where
     * none does.
     *
     * <p>Everything else is handed back as super built it, and the slot order and the mirrored flip are eco's
     * own rule (workers sorted by distance to the controller, cut at {@code SLOT_COUNT}) - so a sync that
     * changes the coolant, switch, queue or energy halves needs no edit here.
     */
    @Override
    public CraftingControllerPanelState getCraftingControllerPanelState() {
        CraftingControllerPanelState base = super.getCraftingControllerPanelState();
        NECraftingCluster cluster = getCluster();
        if (!base.formed() || cluster == null) {
            return base;
        }
        Map<BlockPos, Integer> coresByPos = new HashMap<>();
        for (ECOCraftingParallelCoreBlockEntity core : cluster.getParallelCores()) {
            int index = core.getTier().getTier();
            coresByPos.put(core.getBlockPos(), index >= 1 ? index : CORE_ART_COLUMN);
        }
        var upSide = OrientationStrategies.horizontalFacing().getSide(getBlockState(), RelativeSide.TOP);
        List<ECOCraftingWorkerBlockEntity> workers = new ArrayList<>(cluster.getWorkers());
        workers.sort(Comparator.comparingDouble(worker -> worker.getBlockPos().distSqr(getBlockPos())));
        if (workers.size() > CraftingControllerPanelState.SLOT_COUNT) {
            workers = workers.subList(0, CraftingControllerPanelState.SLOT_COUNT);
        }
        List<Integer> upper = new ArrayList<>(workers.size());
        List<Integer> lower = new ArrayList<>(workers.size());
        for (ECOCraftingWorkerBlockEntity worker : workers) {
            upper.add(coresByPos.getOrDefault(worker.getBlockPos().relative(upSide), 0));
            lower.add(coresByPos.getOrDefault(worker.getBlockPos().relative(upSide.getOpposite()), 0));
        }
        return CraftingControllerPanelState.of(base.hostTier(), upper, lower, workers.size(),
                getBlockState().getValue(ECOCraftingSystem.MIRRORED), base.coolantOverlay(),
                base.switchOverlay(), base.queuePermille(), base.energyPermille());
    }

    @Override
    public void onReady() {
        super.onReady();
        // Deliberately after eco's own number, which is where this member's 64.0 AE/t is written.
        getMainNode().setIdlePowerUsage(
                SimplifyPowerProfile.L1.craftingControllerIdlePower());
    }

    /** Records what the interface cell turned out to hold, for the next {@link #tick}. */
    public void setCommunicationInterface(boolean communicationInterface) {
        if (this.communicationInterface == communicationInterface) {
            return;
        }
        this.communicationInterface = communicationInterface;
        this.appearancePending = true;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        super.tick(level, pos, state);
        if (appearancePending && !level.isClientSide()) {
            appearancePending = false;
            publishAppearance(level, state);
        }
    }

    @Override
    public void updateState(boolean updateExposed) {
        super.updateState(updateExposed);
        appearancePending = true;
    }

    private void publishAppearance(Level level, BlockState state) {
        if (isRemoved() || level == null || level.isClientSide()) {
            return;
        }
        if (!state.hasProperty(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE)) {
            return;
        }
        BlockState next = state.setValue(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE,
                isFormed() && communicationInterface);
        if (next != state) {
            level.setBlock(worldPosition, next, Block.UPDATE_CLIENTS);
        }
    }

    /** What the interface cell of the formed machine holds. Read by the game tests and the blockstate write. */
    public boolean communicationInterface() {
        return communicationInterface;
    }
}
