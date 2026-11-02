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
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.api.MachineMood;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype1201.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype1201.multiblock.definition.SimplifyCraftingDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

    /**
     * The host rolls for the face once, the first time it forms, and keeps the answer: geometry checks
     * re-run on neighbour changes, chunk loads and rebuilds, so a per-check roll would flip the look
     * back and forth instead of being rare. One in sixteen.
     */
    private static final int MIND_CHANCE_ONE_IN = 16;
    private static final String HAS_MIND_TAG = "neoecoprototype1201:has_mind";
    private static final String MIND_ROLLED_TAG = "neoecoprototype1201:mind_rolled";

    /** How often a minded host re-reads its own situation. One second is plenty for a mood. */
    private static final int MOOD_SAMPLE_TICKS = 20;

    /** The face is a one-in-sixteen roll per host, so noticing it is worth an advancement. 8 blocks. */
    private static final double MIND_AWARD_RANGE_SQR = 64.0D;
    private static final net.minecraft.resources.ResourceLocation MIND_ADVANCEMENT =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "mind");

    /**
     * How much of the idle draw a contented mind may take off, as a fraction of the normal draw. The 1.21.1
     * line reads this from its server config; this line ships no config system, so it is the same number
     * the other line ships as its default. It only ever saves - see {@link MachineMood}.
     */
    private static final double MIND_IDLE_POWER_SAVING = 0.10D;

    private boolean communicationInterface;
    private boolean appearancePending;
    private boolean hasMind;
    private boolean mindRolled;
    private boolean mindAwarded;
    private int moodCountdown = MOOD_SAMPLE_TICKS;
    /** The idle draw the node currently carries, so a sample that changed nothing pushes nothing. */
    private double idlePowerPushed = -1.0D;
    /** A band the host has crossed but not yet written, or -1 when there is nothing to publish. */
    private int mindTierToPublish = -1;

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
        // Deliberately after eco's own number: eco writes 64.0 AE/t for this member and this line ships
        // 4.0 (SimplifyPowerProfile.L1 = 32.0 base at the 1/8 multiplier), which is what a face then
        // trims to 3.6 when the mind is content.
        applyIdlePower(SimplifyPowerProfile.L1.craftingControllerIdlePower());
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
            publishAppearance(level, pos);
        }
        if (!level.isClientSide() && --moodCountdown <= 0) {
            moodCountdown = MOOD_SAMPLE_TICKS;
            applyMindMood();
            if (level instanceof ServerLevel serverLevel) {
                awardMindAdvancement(serverLevel);
            }
        }
    }

    @Override
    public void updateState(boolean updateExposed) {
        super.updateState(updateExposed);
        appearancePending = true;
    }

    private void publishAppearance(Level level, BlockPos pos) {
        if (isRemoved() || level == null || level.isClientSide()) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE)) {
            return;
        }
        if (isFormed() && !mindRolled) {
            mindRolled = true;
            hasMind = level.random.nextInt(MIND_CHANCE_ONE_IN) == 0;
            setChanged();
        }
        int publishedTier = mindTierToPublish >= 0 ? mindTierToPublish
                : state.getValue(SimplifyCraftingSystemBlock.MIND_TIER);
        mindTierToPublish = -1;
        BlockState next = state.setValue(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE,
                        isFormed() && communicationInterface)
                .setValue(SimplifyCraftingSystemBlock.HAS_MIND, isFormed() && hasMind)
                .setValue(SimplifyCraftingSystemBlock.MIND_TIER,
                        isFormed() && hasMind ? publishedTier : 0);
        if (next != state) {
            level.setBlock(worldPosition, next, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Tells the closest player within reach of the panel that this host grew a face. It is not persisted:
     * {@code hasMind} is, so a world that reloaded before anyone came close asks again on the next sample,
     * and an advancement can be awarded repeatedly without harm.
     */
    private void awardMindAdvancement(ServerLevel level) {
        if (mindAwarded || !hasMind || !isFormed()) {
            return;
        }
        net.minecraft.server.level.ServerPlayer nearest = null;
        double nearestDistance = MIND_AWARD_RANGE_SQR;
        for (net.minecraft.server.level.ServerPlayer player : level.getPlayers(p -> true)) {
            double distance = player.distanceToSqr(worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D);
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }
        if (nearest == null) {
            return;
        }
        mindAwarded = true;
        // 1.20.1 hands out the Advancement itself and answers null for an unknown id; the 1.21.1 line
        // passes a Holder, so this is the one shape that differs in this method.
        net.minecraft.advancements.Advancement mind = level.getServer().getAdvancements()
                .getAdvancement(MIND_ADVANCEMENT);
        if (mind != null) {
            nearest.getAdvancements().award(mind, "easter_egg");
        }
    }

    /**
     * A host with a face draws slightly less power while idle, by how content its situation is: coolant in
     * the bath and work to do. Nothing here is stored - the next sample recomputes it - so the value cannot
     * be farmed, cannot drift across saves, and needs no migration when the numbers change.
     *
     * <p>The same mood also picks which face the art shows, but that one <em>is</em> written to the block
     * state, because models cannot read a live number. It is therefore the only part allowed to trigger a
     * block change, and it triggers on a band crossing rather than on every sample - see
     * {@link MachineMood#tierOf}. The write itself rides {@code appearancePending}, the same deferral the
     * face flag uses, so it never lands inside AE2's cluster recalculation.</p>
     */
    private void applyMindMood() {
        Level reader = this.level;
        if (reader == null || reader.isClientSide()) {
            return;
        }
        double baseline = SimplifyPowerProfile.L1.craftingControllerIdlePower();
        if (!hasMind) {
            applyIdlePower(baseline);
            return;
        }
        int capacity = getDisplayedCoolantCapacity();
        double coolant = capacity > 0 ? (double) getDisplayedCoolantAmount() / capacity : 0.0D;
        boolean busy = getRunningThreadCount() > 0;
        boolean formed = isFormed();
        applyIdlePower(baseline * MachineMood.idlePowerFactor(true, coolant, busy, formed,
                MIND_IDLE_POWER_SAVING));
        if (!formed) {
            return;
        }
        int shown = reader.getBlockState(worldPosition).getValue(SimplifyCraftingSystemBlock.MIND_TIER);
        int next = MachineMood.tierOf(MachineMood.mood(coolant, busy), shown);
        if (next != shown) {
            mindTierToPublish = next;
            appearancePending = true;
        }
    }

    /** Pushes a new idle draw only when it differs, because this runs on a timer and the node is shared. */
    private void applyIdlePower(double value) {
        if (Double.compare(value, idlePowerPushed) == 0) {
            return;
        }
        idlePowerPushed = value;
        getMainNode().setIdlePowerUsage(value);
    }

    /** Whether this host grew a face. */
    public boolean hasMind() {
        return hasMind;
    }

    /** Whether the once-per-host roll has already happened. */
    public boolean mindRolled() {
        return mindRolled;
    }

    /**
     * What the grid actually charges this host while idle. The live node answers it - {@code IManagedGridNode}
     * only has a setter - and before the node exists the answer is what we last meant to push.
     */
    public double idlePowerDraw() {
        var node = getMainNode().getNode();
        return node == null ? Math.max(0.0D, idlePowerPushed) : node.getIdlePowerUsage();
    }

    // public, not protected: AE2 15.4.10 widens this hook beyond vanilla 1.20.1's modifier, and an
    // override may not narrow what it inherits.
    @Override
    public void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(MIND_ROLLED_TAG, mindRolled);
        tag.putBoolean(HAS_MIND_TAG, hasMind);
    }

    @Override
    public void loadTag(net.minecraft.nbt.CompoundTag tag) {
        // AE2 declares load(final) and routes the extra fields here, so loadTag is the hook for these two.
        super.loadTag(tag);
        mindRolled = tag.getBoolean(MIND_ROLLED_TAG);
        hasMind = tag.getBoolean(HAS_MIND_TAG);
    }

    /** What the interface cell of the formed machine holds. Read by the game tests and the blockstate write. */
    public boolean communicationInterface() {
        return communicationInterface;
    }
}
