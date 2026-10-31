package cn.dancingsnow.neoecoprototype1201.multiblock.calculator;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.RelativeSide;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationCoolingControllerBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationThreadingCoreBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NEComputationClusterCalculator;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyMultiblockConfig;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationSystemBlock;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * L1 computation geometry using the addon block set and eco's CPU cluster.
 *
 * <p>Same rule set as the 1.21.1 line. The parent's own {@code verifyStructure} is not called: it
 * validates eco's block set (including the network switch column an L1 machine has no member for) and
 * is not shaped to be told otherwise, so the geometry is re-checked here against our own blocks. The
 * parent's helpers ({@code findUniqueController}, {@code matchingStateFacing}, {@code validateBlockLine},
 * {@code ensureSameSurface}) are used as-is, which is also why the reading of "which hand is which"
 * stays identical to eco's.
 */
public class SimplifyComputationClusterCalculator extends NEComputationClusterCalculator {
    private static final int MAX_LENGTH = SimplifyMultiblockConfig.L1_MAX_LENGTH;

    /** Side-effect-free result of the computation geometry check. */
    public record StructureValidation(boolean valid, boolean mirrored,
                                      BlockPos controllerPos, BlockPos coolerPos,
                                      boolean communicationInterface, boolean energizedParallelCore) {
        public static StructureValidation invalid(BlockPos controllerPos) {
            return new StructureValidation(false, false, controllerPos, null, false, false);
        }
    }

    /** What {@link #verifyStructure} reports when one hand of the machine checks out. */
    private record Formed(BlockPos coolerPos, boolean communicationInterface,
                          boolean energizedParallelCore) { }

    public SimplifyComputationClusterCalculator(NEBlockEntity<NEComputationCluster, ?> blockEntity) {
        super(blockEntity);
    }

    @Override
    protected int maxLength() {
        return MAX_LENGTH;
    }

    @Override
    protected Supplier<? extends Block> casing() {
        return ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK;
    }

    @Override
    public NEComputationCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new NEComputationCluster(min, max);
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        StructureValidation result = validateStructure(level, min, max);
        applyValidationState(level, result);
        return result.valid();
    }

    /** Reads the world and returns the geometry result without mutating block entities. */
    public StructureValidation validateStructure(ServerLevel level, BlockPos min, BlockPos max) {
        Optional<ControllerContext<ECOComputationSystemBlockEntity>> contextResult = findUniqueController(
                level, min, max, ECOComputationSystemBlockEntity.class);
        if (contextResult.isEmpty()) {
            return StructureValidation.invalid(null);
        }

        ControllerContext<ECOComputationSystemBlockEntity> context = contextResult.orElseThrow();
        ECOComputationSystemBlockEntity controller = context.controller();
        BlockPos controllerPos = context.position();
        IECOTier tier = controller.getTier();
        Direction front = context.front();
        Direction back = context.back();
        Direction top = context.top();
        Direction down = context.down();
        Direction left = context.left();
        Direction right = context.right();
        // The one cell an energized core may stand in for a casing: directly behind the host.
        BlockState controllerState = level.getBlockState(controllerPos);
        BlockPos energizedCoreCell = energizedCoreCell(controllerPos, controllerState);

        Optional<Formed> formed = verifyStructure(
                level, controllerPos, tier, front, back, top, down, right, left, energizedCoreCell, min, max);
        if (formed.isPresent()) {
            Formed shape = formed.get();
            return new StructureValidation(true, false, controllerPos, shape.coolerPos(),
                    shape.communicationInterface(), shape.energizedParallelCore());
        }
        formed = verifyStructure(level, controllerPos, tier, front, back, top, down, left, right,
                energizedCoreCell, min, max);
        if (formed.isPresent()) {
            Formed shape = formed.get();
            return new StructureValidation(true, true, controllerPos, shape.coolerPos(),
                    shape.communicationInterface(), shape.energizedParallelCore());
        }
        return StructureValidation.invalid(controllerPos);
    }

    private void applyValidationState(ServerLevel level, StructureValidation result) {
        if (result.controllerPos() != null
                && level.getBlockEntity(result.controllerPos()) instanceof ECOComputationSystemBlockEntity controller) {
            controller.setMirrored(result.mirrored());
            // The shape goes to the block rather than being written here: this stack is inside AE2's
            // cluster recalculation, where a block change would wedge every multiblock.
            if (level.getBlockState(result.controllerPos()).getBlock() instanceof SimplifyComputationSystemBlock host) {
                host.publishShape(level, result.controllerPos(),
                        new SimplifyComputationSystemBlock.Shape(
                                result.communicationInterface(), result.energizedParallelCore()));
            }
        }
        if (result.coolerPos() != null
                && level.getBlockEntity(result.coolerPos()) instanceof ECOComputationCoolingControllerBlockEntity cooler) {
            cooler.setMirrored(result.mirrored());
        }
    }

    private Optional<Formed> verifyStructure(ServerLevel level, BlockPos controllerPos, IECOTier tier,
                                             Direction front, Direction back, Direction top, Direction down,
                                             Direction interfaceSide, Direction expandSide,
                                             BlockPos energizedCorePos, BlockPos min, BlockPos max) {
        Optional<Block> matchedInterface = validateInterfaceFace(
                level, controllerPos, back, top, down, interfaceSide, energizedCorePos);
        if (matchedInterface.isEmpty()
                || !validateShell(level, controllerPos, top, down, expandSide, energizedCorePos)
                || !validateShell(level, controllerPos, top, down, back, energizedCorePos)
                || !validateShell(level, controllerPos.relative(back).relative(expandSide), top, down,
                        energizedCorePos)) {
            return Optional.empty();
        }
        boolean communicationInterface = matchedInterface.get()
                == ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get();

        BlockPos connectorStart = controllerPos.relative(expandSide).relative(expandSide);
        Optional<BlockPos> connectorEndResult = validateBlockLine(level, expandSide, connectorStart,
                matchingStateFacing(ModRegistration.SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK, front));
        if (connectorEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos connectorEnd = connectorEndResult.orElseThrow();

        BlockPos threadingCoreStart = connectorStart.relative(back);
        Optional<BlockPos> threadingCoreEndResult = validateBlockLine(level, expandSide, threadingCoreStart,
                ownThreadingCore(level, tier, back, threadingCoreStart));
        if (threadingCoreEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos threadingCoreEnd = threadingCoreEndResult.orElseThrow();

        BlockPos upperParallelCoreStart = threadingCoreStart.relative(top);
        Optional<BlockPos> upperParallelCoreEndResult = validateBlockLine(level, expandSide, upperParallelCoreStart,
                ownParallelCore(level, tier, back));
        if (upperParallelCoreEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos upperParallelCoreEnd = upperParallelCoreEndResult.orElseThrow();

        BlockPos lowerParallelCoreStart = threadingCoreStart.relative(down);
        Optional<BlockPos> lowerParallelCoreEndResult = validateBlockLine(level, expandSide, lowerParallelCoreStart,
                ownParallelCore(level, tier, back));
        if (lowerParallelCoreEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos lowerParallelCoreEnd = lowerParallelCoreEndResult.orElseThrow();

        BlockPos upperDriveStart = connectorStart.relative(top);
        Optional<BlockPos> upperDriveEndResult = validateBlockLine(level, expandSide, upperDriveStart,
                matchingStateFacing(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BLOCK, front));
        if (upperDriveEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos upperDriveEnd = upperDriveEndResult.orElseThrow();

        BlockPos lowerDriveStart = connectorStart.relative(down);
        Optional<BlockPos> lowerDriveEndResult = validateBlockLine(level, expandSide, lowerDriveStart,
                matchingStateFacing(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BLOCK, front));
        if (lowerDriveEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos lowerDriveEnd = lowerDriveEndResult.orElseThrow();

        List<BlockPos> tails = List.of(connectorEnd, threadingCoreEnd, upperDriveEnd,
                lowerDriveEnd, upperParallelCoreEnd, lowerParallelCoreEnd);
        if (!ensureSameSurface(tails)) {
            return Optional.empty();
        }

        List<BlockPos> tailCasings = List.of(
                threadingCoreEnd.relative(expandSide),
                upperDriveEnd.relative(expandSide),
                lowerDriveEnd.relative(expandSide),
                upperParallelCoreEnd.relative(expandSide),
                lowerParallelCoreEnd.relative(expandSide));

        BlockPos coolerPos = connectorEnd.relative(expandSide);
        if (!validateBlock(level, coolerPos, ownCoolingController(level, tier, expandSide), coolerPos)
                || !tailCasings.stream().allMatch(tail -> level.getBlockState(tail).is(casing().get()))) {
            return Optional.empty();
        }
        // The host stands in a column no shell walk visits, so the two cells above and below it are the
        // only cells nothing above looks at; they sit inside the cluster bounds and would otherwise be
        // adopted without passing a geometry test.
        for (Direction lift : new Direction[]{top, down}) {
            if (!level.getBlockState(controllerPos.relative(lift)).is(casing().get())) {
                return Optional.empty();
            }
        }
        // One allotted cell is also how "exactly one" is enforced: geometry refuses the block everywhere
        // else. The energized threading core is not reported - it draws its own advanced face.
        boolean energizedParallelCore = level.getBlockState(energizedCorePos)
                .is(ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get());
        return Optional.of(new Formed(coolerPos, communicationInterface, energizedParallelCore));
    }

    /**
     * The face on the interface side: the shell column beside the host and, behind it, the interface
     * column, whose middle cell holds the interface.
     */
    protected Optional<Block> validateInterfaceFace(ServerLevel level, BlockPos controllerPos, Direction back,
                                                    Direction top, Direction down, Direction interfaceSide,
                                                    BlockPos energizedCorePos) {
        if (!validateShell(level, controllerPos, top, down, interfaceSide, energizedCorePos)) {
            return Optional.empty();
        }
        return matchedComputationInterface(level, controllerPos.relative(back).relative(interfaceSide), top, down);
    }

    private boolean validateShell(ServerLevel level, BlockPos pos, Direction top, Direction down,
                                  BlockPos energizedCorePos) {
        return isShell(level.getBlockState(pos), pos, energizedCorePos)
                && isShell(level.getBlockState(pos.relative(top)), pos.relative(top), energizedCorePos)
                && isShell(level.getBlockState(pos.relative(down)), pos.relative(down), energizedCorePos);
    }

    private boolean validateShell(ServerLevel level, BlockPos pos, Direction top, Direction down,
                                  Direction side, BlockPos energizedCorePos) {
        return validateShell(level, pos.relative(side), top, down, energizedCorePos);
    }

    /**
     * The one cell an energized core may occupy: directly behind the host, read through AE2's
     * orientation API so it is the same cell for a mirrored build.
     */
    public static BlockPos energizedCoreCell(BlockPos controllerPos, BlockState controllerState) {
        return controllerPos.relative(
                IOrientationStrategy.get(controllerState).getSide(controllerState, RelativeSide.BACK));
    }

    private boolean isShell(BlockState state, BlockPos pos, BlockPos energizedCorePos) {
        return state.is(casing().get())
                || (pos.equals(energizedCorePos)
                        && state.is(ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get()));
    }

    /** Which of the two interface kinds occupies the interface cell, if either does. */
    private Optional<Block> matchedComputationInterface(ServerLevel level, BlockPos interfacePos,
                                                        Direction top, Direction down) {
        Block communication = ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get();
        Block network = ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get();
        Supplier<? extends Block> casing = casing();
        if (validateInterface(level, interfacePos, top, down,
                ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK, casing)) {
            return Optional.of(communication);
        }
        if (validateInterface(level, interfacePos, top, down,
                ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK, casing)) {
            return Optional.of(network);
        }
        return Optional.empty();
    }

    /**
     * The threading core line. The energized core is only accepted at {@code firstPos} -- the cell
     * nearest the controller -- which caps a structure at exactly one of them without counting state.
     */
    private static BiPredicate<BlockState, BlockPos> ownThreadingCore(
            ServerLevel level, IECOTier tier, Direction facing, BlockPos firstPos) {
        return (state, pos) -> (
                        state.is(ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get())
                        || (pos.equals(firstPos)
                                && state.is(ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get())))
                && level.getBlockEntity(pos) instanceof ECOComputationThreadingCoreBlockEntity core
                && tier.supportsComponentTier(core.getTier())
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
    }

    /**
     * The parallel core column takes the plain L1 core, facing {@code back}; the energized core is
     * deliberately not accepted here - it has exactly one allotted cell, in the shell.
     */
    private static BiPredicate<BlockState, BlockPos> ownParallelCore(
            ServerLevel level, IECOTier tier, Direction facing) {
        return (state, pos) -> state.is(ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get())
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing
                && level.getBlockEntity(pos) instanceof ECOComputationParallelCoreBlockEntity core
                && tier.supportsComponentTier(core.getTier());
    }

    private static BiPredicate<BlockState, BlockPos> ownCoolingController(
            ServerLevel level, IECOTier tier, Direction facing) {
        return (state, pos) -> state.is(ModRegistration.SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK.get())
                && level.getBlockEntity(pos) instanceof ECOComputationCoolingControllerBlockEntity cooler
                && tier.supportsComponentTier(cooler.getTier())
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
    }
}
