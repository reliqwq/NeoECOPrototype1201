package cn.dancingsnow.neoecoprototype1201.block.computation;

import appeng.api.networking.crafting.ICraftingService;
import cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * C1 computation host. {@code communication_interface} carries whether the interface cell holds the
 * communication interface, so the blockstate file can hand that case its own formed model;
 * {@code energized_parallel_core} swaps the formed face when the energized core sits behind the host.
 *
 * <p>The block entity is our own subclass: on eco 20.5.3 every consumer that finds this machine goes
 * through {@code grid.NEMachineLookup}, which matches with {@code isAssignableFrom} and so still sees a
 * subclass, and the host's build definition - the thing eco's 搭建 button asks for - can only be answered
 * from the block entity. See {@code SimplifyComputationSystemBlockEntity} for the measurement.
 */
public class SimplifyComputationSystemBlock extends ECOComputationSystem
        implements IHostPanelTextureProvider {
    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(Prototype1201.MOD_ID + "-computation-diagnose");

    /** True when the interface cell holds {@code simplify_computation_network_interface}. */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");

    /** True when the cell directly behind the host holds the energized parallel core. */
    public static final BooleanProperty ENERGIZED_PARALLEL_CORE =
            BooleanProperty.create("energized_parallel_core");

    /** The one shape read the host has to publish: which interface, and whether the energized core is in. */
    public record Shape(boolean communicationInterface, boolean energizedParallelCore) { }

    /** What the calculator measured, parked per dimension and position until a tick may write it. */
    private static final Map<ResourceKey<Level>, Map<Long, Shape>> PENDING_SHAPE = new ConcurrentHashMap<>();

    private static Map<Long, Shape> pendingFor(Level level) {
        return PENDING_SHAPE.computeIfAbsent(level.dimension(), d -> new ConcurrentHashMap<>());
    }

    public SimplifyComputationSystemBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(COMMUNICATION_INTERFACE, false)
                .setValue(ENERGIZED_PARALLEL_CORE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMMUNICATION_INTERFACE, ENERGIZED_PARALLEL_CORE);
    }

    /**
     * Records what the machine turned out to hold and asks for a tick that can write it. The calculator
     * cannot write on its own stack: it runs inside AE2's cluster recalculation, which holds a global
     * "modification in progress" latch.
     */
    public void publishShape(ServerLevel level, BlockPos pos, Shape shape) {
        pendingFor(level).put(pos.asLong(), shape);
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        // The only reader of a parked shape is the tick scheduled for this position, and Minecraft skips
        // a block tick whose block no longer matches, so once the host is gone nothing consumes the entry.
        if (!level.isClientSide) {
            pendingFor(level).remove(pos.asLong());
        }
    }

    /** Drops every parked shape when a server stops; the table is static and keyed by dimension id. */
    public static void clearPendingShapes() {
        PENDING_SHAPE.clear();
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        Shape shape = pendingFor(level).remove(pos.asLong());
        BlockState next = state;
        if (shape != null && state.hasProperty(COMMUNICATION_INTERFACE)) {
            // Both bits describe the formed look only: an unformed host must not wear a formed face.
            next = next.setValue(COMMUNICATION_INTERFACE, state.getValue(NEBlock.FORMED)
                            && shape.communicationInterface())
                    .setValue(ENERGIZED_PARALLEL_CORE, state.getValue(NEBlock.FORMED)
                            && shape.energizedParallelCore());
        }
        // eco lights these two from a shell cell beside the host and reads them back to decide whether
        // the cluster registers its CPUs through a switch frequency. An L1 machine has no switch block
        // and so no frequency; leaving them set sends our CPUs into a logical network that does not
        // exist. Our calculator replaces the eco method that would clear them, so pin them here.
        next = pinned(next, ECOComputationSystem.NETWORK_SWITCH);
        next = pinned(next, ECOComputationSystem.HIGH_ENERGY_NETWORK_SWITCH);
        if (next != state) {
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
        }
    }

    private static BlockState pinned(BlockState state, Property<Boolean> bit) {
        return state.hasProperty(bit) && state.getValue(bit) ? state.setValue(bit, false) : state;
    }

    /**
     * Sneak + right-click prints what the crafting-submission filters read. AE2's
     * {@code CraftingCpuLogic} rejects a CPU in three steps - busy, then {@code isActive()}, then
     * {@code getAvailableStorage()} below the plan's bytes - and the client only ever shows the single
     * sentence "被排除：N 个" without saying which step fired. The CPU bytes are not the host tier's:
     * {@code NEComputationCluster.getTotalStorage()} sums {@code getCPUTotalBytes()} over the cell stack
     * of every drive in both rows, so this prints each drive's stack and each CPU's own three answers.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown() && level instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(pos) instanceof ECOComputationSystemBlockEntity host) {
            var cluster = host.getCluster();
            var sb = new StringBuilder();
            var drives = new StringBuilder();
            if (cluster != null) {
                for (var drive : cluster.getUpperDrives()) {
                    describeDrive(drives, "upper", drive);
                }
                for (var drive : cluster.getLowerDrives()) {
                    describeDrive(drives, "lower", drive);
                }
                var cpus = new StringBuilder();
                for (var cpu : cluster.getActiveCPUs()) {
                    cpus.append(' ').append(cpu.getName().getString())
                            .append(" active=").append(cpu.isActive())
                            .append(" busy=").append(cpu.isBusy())
                            .append(" avail=").append(cpu.getAvailableStorage())
                            .append(" status=").append(cpu.getJobStatus());
                }
                // The list the submit filter actually walks is AE2's grid service, not the cluster's job
                // map: getActiveCPUs() only holds CPUs that already carry a plan, so an idle machine reads
                // empty no matter how many threads it has.
                var gridCpus = new StringBuilder();
                var grid = host.getMainNode().getGrid();
                var service = grid == null ? null : grid.getService(ICraftingService.class);
                if (service != null) {
                    for (var cpu : service.getCpus()) {
                        gridCpus.append(' ').append(cpu.getName().getString())
                                .append(" busy=").append(cpu.isBusy())
                                .append(" avail=").append(cpu.getAvailableStorage())
                                .append(" co=").append(cpu.getCoProcessors())
                                .append(" status=").append(cpu.getJobStatus());
                    }
                    gridCpus.append(" count=").append(service.getCpus().size());
                } else {
                    gridCpus.append("no-grid-or-service");
                }
                sb.append(" total=").append(cluster.getTotalStorage())
                        .append(" avail=").append(cluster.getAvailableStorage())
                        .append(" threads=").append(cluster.getMaxThreads())
                        .append(" parallel=").append(cluster.getPooledParallelism())
                        .append(" cpus=").append(cluster.getActiveCPUs().size())
                        .append(" blockers=").append(cluster.getEndgameBlockers())
                        .append(" cpuList=").append(cpus)
                        .append(" gridCpus=").append(gridCpus);
            }
            LOGGER.info("L1 computation diagnose at {} formed={} tier={} cluster={} drives={}{}",
                    pos, host.isFormed(), host.getTier(), cluster, drives, sb);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("L1 计算矩阵诊断 formed=" + host.isFormed()
                        + (cluster == null ? " 无集群" : " 合成存储=" + cluster.getTotalStorage()
                        + " 可用=" + cluster.getAvailableStorage() + " CPU数=" + cluster.getActiveCPUs().size())), false);
                serverPlayer.displayClientMessage(Component.literal("  驱动与闪存:" + drives), false);
                serverPlayer.displayClientMessage(Component.literal("  集群:" + sb), false);
            }
            return InteractionResult.CONSUME;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    private static void describeDrive(StringBuilder into, String row,
                                      cn.dancingsnow.neoecoae.blocks.entity.computation
                                              .ECOComputationDriveBlockEntity drive) {
        var stack = drive.getCellStack();
        into.append(' ').append(row).append('=').append(drive.getBlockPos())
                .append(" cell=").append(stack == null ? "null" : stack.getCount() + "x" + stack.getItem());
        if (stack != null && stack.getItem() instanceof cn.dancingsnow.neoecoae.items.ECOComputationCellItem cell) {
            into.append(" cpuBytes=").append(cell.getTier().getCPUTotalBytes());
        }
    }

    private static final ResourceLocation PANEL_TIER = panel("computation_tier.png");
    private static final ResourceLocation PANEL_MARKER = panel("computation_marker.png");
    private static final ResourceLocation SWITCH_ON = panel("computation_switch_on.png");
    private static final ResourceLocation SWITCH_ON_HOVER = panel("computation_switch_on_hover.png");
    private static final ResourceLocation STATUS_0_ENERGIZED = panel("computation_status_0_energized.png");

    @Nullable
    private static ResourceLocation panel(String file) {
        return ResourceLocation.fromNamespaceAndPath(
                Prototype1201.MOD_ID, "textures/gui/panel/" + file);
    }

    @Override
    @Nullable
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part) {
        return switch (part) {
            case COMPUTATION_TIER -> PANEL_TIER;
            case COMPUTATION_MARKER -> PANEL_MARKER;
            default -> null;
        };
    }

    /**
     * The switches' tiles and the first status lamp. Lamp 0 ignores {@code lit} on purpose: eco lights
     * it from its own endgame predicate, which an L1 host can never satisfy, and the fact this cell has
     * to carry is the energized parallel core behind the host.
     */
    @Override
    @Nullable
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part,
                                            int index, boolean on, boolean hover) {
        if (part == IHostPanelTextureProvider.Part.COMPUTATION_STATUS) {
            return index == 0 && state.hasProperty(ENERGIZED_PARALLEL_CORE)
                    && state.getValue(ENERGIZED_PARALLEL_CORE)
                    ? STATUS_0_ENERGIZED : null;
        }
        if (part != IHostPanelTextureProvider.Part.COMPUTATION_SWITCH || !on) {
            return null;
        }
        return hover ? SWITCH_ON_HOVER : SWITCH_ON;
    }
}
