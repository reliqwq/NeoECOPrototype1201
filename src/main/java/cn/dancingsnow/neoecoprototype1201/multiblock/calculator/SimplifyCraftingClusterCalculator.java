package cn.dancingsnow.neoecoprototype1201.multiblock.calculator;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NECraftingClusterCalculator;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyMultiblockConfig;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * L1 crafting geometry using the addon block set and eco's crafting cluster.
 *
 * <p>Same rule set as the 1.21.1 line. The parent's own {@code verifyStructure} is not called: it validates
 * eco's block set -- including the network-switch column an L1 machine has no member for -- and is not shaped
 * to be told otherwise, so the geometry is re-checked here against our own blocks. The parent's helpers
 * ({@code findUniqueController}, {@code matchingStateFacing}, {@code validateBlockLine},
 * {@code ensureSameSurface}) are used as-is, which is also why "which hand is which" reads the same as eco's.
 */
public class SimplifyCraftingClusterCalculator extends NECraftingClusterCalculator {
    private static final int MAX_LENGTH = SimplifyMultiblockConfig.L1_MAX_LENGTH;

    /** Side-effect-free result of the crafting geometry check. */
    public record StructureValidation(boolean valid, boolean mirrored, BlockPos controllerPos,
                                      boolean communicationInterface) {
        public static StructureValidation invalid(BlockPos controllerPos) {
            return new StructureValidation(false, false, controllerPos, false);
        }
    }

    public SimplifyCraftingClusterCalculator(NEBlockEntity<NECraftingCluster, ?> target) {
        super(target);
    }

    @Override
    protected int maxLength() {
        return MAX_LENGTH;
    }

    @Override
    protected Supplier<? extends Block> casing() {
        return ModRegistration.SIMPLIFY_CRAFTING_CASING_BLOCK;
    }

    @Override
    public NECraftingCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new NECraftingCluster(min, max);
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        StructureValidation result = validateStructure(level, min, max);
        applyValidationState(level, result);
        return result.valid();
    }

    /** Reads the world and returns the geometry result without mutating the controller. */
    public StructureValidation validateStructure(ServerLevel level, BlockPos min, BlockPos max) {
        Optional<ControllerContext<ECOCraftingSystemBlockEntity>> result = findUniqueController(
                level, min, max, ECOCraftingSystemBlockEntity.class);
        if (result.isEmpty()) {
            return StructureValidation.invalid(null);
        }
        ControllerContext<ECOCraftingSystemBlockEntity> context = result.orElseThrow();
        BlockPos controllerPos = context.position();
        IECOTier tier = context.controller().getTier();
        if (verifyStructure(level, controllerPos, tier, context.front(), context.back(), context.top(),
                context.down(), context.right(), context.left())) {
            return formed(level, controllerPos, context.back(), context.right(), false);
        }
        if (verifyStructure(level, controllerPos, tier, context.front(), context.back(), context.top(),
                context.down(), context.left(), context.right())) {
            return formed(level, controllerPos, context.back(), context.left(), true);
        }
        return StructureValidation.invalid(controllerPos);
    }

    /** A hand that checked out: report the shape, including which of the two interface blocks won. */
    private StructureValidation formed(ServerLevel level, BlockPos controllerPos, Direction back,
                                       Direction interfaceSide, boolean mirrored) {
        BlockState interfaceState = level.getBlockState(interfaceCell(controllerPos, back, interfaceSide));
        return new StructureValidation(true, mirrored, controllerPos, interfaceState.is(
                ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get()));
    }

    /** The one cell that carries the machine's interface, behind the controller and off to a hand. */
    private static BlockPos interfaceCell(BlockPos controllerPos, Direction back, Direction interfaceSide) {
        return controllerPos.relative(back).relative(interfaceSide);
    }

    private void applyValidationState(ServerLevel level, StructureValidation result) {
        if (result.controllerPos() == null) {
            return;
        }
        if (level.getBlockEntity(result.controllerPos()) instanceof ECOCraftingSystemBlockEntity controller) {
            controller.setMirrored(result.mirrored());
            if (controller instanceof SimplifyCraftingSystemBlockEntity host) {
                host.setCommunicationInterface(result.communicationInterface());
            }
        }
    }

    private boolean verifyStructure(ServerLevel level, BlockPos controllerPos, IECOTier tier,
                                    Direction front, Direction back, Direction top, Direction down,
                                    Direction interfaceSide, Direction expandSide) {
        if (!validateCasing(level, controllerPos, top, down, interfaceSide)
                || !validateCasing(level, controllerPos, top, down, expandSide)
                || !validateCasing(level, controllerPos, top, down, back)
                || !validateCasing(level, controllerPos.relative(back).relative(expandSide), top, down)
                // The column the controller itself stands in is the one no walk above visits, and the cluster
                // adopts anything inside its bounds, so a core parked above or below the host used to be free
                // throughput.
                || !level.getBlockState(controllerPos.relative(top)).is(casing().get())
                || !level.getBlockState(controllerPos.relative(down)).is(casing().get())) {
            return false;
        }

        BlockPos interfacePos = interfaceCell(controllerPos, back, interfaceSide);
        if (!validateBlock(level, interfacePos, state -> isCraftingInterface(state))
                || !validateBlock(level, interfacePos.relative(top), state ->
                state.is(ModRegistration.SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get()))
                || !validateBlock(level, interfacePos.relative(down), state ->
                state.is(ModRegistration.SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get()))) {
            return false;
        }

        BlockPos workerStart = controllerPos.relative(expandSide).relative(expandSide);
        Optional<BlockPos> workerEnd = validateBlockLine(level, expandSide, workerStart,
                matchingStateFacing(ModRegistration.SIMPLIFY_CRAFTING_WORKER_BLOCK, front));
        Optional<BlockPos> upperCoreEnd = validateBlockLine(level, expandSide, workerStart.relative(top),
                ownParallelCore(level, tier, front));
        Optional<BlockPos> lowerCoreEnd = validateBlockLine(level, expandSide, workerStart.relative(down),
                ownParallelCore(level, tier, front));
        BlockPos ventStart = workerStart.relative(back);
        Optional<BlockPos> ventEnd = validateBlockLine(level, expandSide, ventStart,
                matchingStateFacing(ModRegistration.SIMPLIFY_CRAFTING_VENT_BLOCK, back));
        Optional<BlockPos> upperBusEnd = validateBlockLine(level, expandSide, ventStart.relative(top),
                matchingStateFacing(ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK, back));
        Optional<BlockPos> lowerBusEnd = validateBlockLine(level, expandSide, ventStart.relative(down),
                matchingStateFacing(ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK, back));

        if (workerEnd.isEmpty() || upperCoreEnd.isEmpty() || lowerCoreEnd.isEmpty()
                || ventEnd.isEmpty() || upperBusEnd.isEmpty() || lowerBusEnd.isEmpty()) {
            return false;
        }

        List<BlockPos> ends = List.of(workerEnd.orElseThrow(), upperCoreEnd.orElseThrow(),
                lowerCoreEnd.orElseThrow(), ventEnd.orElseThrow(), upperBusEnd.orElseThrow(),
                lowerBusEnd.orElseThrow());
        if (!ensureSameSurface(ends)) {
            return false;
        }
        return ends.stream().map(pos -> pos.relative(expandSide))
                .allMatch(pos -> level.getBlockState(pos).is(casing().get()));
    }

    private boolean isCraftingInterface(BlockState state) {
        return state.is(ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get())
                || state.is(ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get());
    }

    /**
     * Not an override of eco's {@code matchingParallelCore}: that one matches eco's own core family, while this
     * predicate matches ours. A same-name same-signature static would hide the inherited instance method, which
     * javac rejects.
     */
    private static BiPredicate<BlockState, BlockPos> ownParallelCore(Level level, IECOTier tier,
                                                                     Direction facing) {
        return (state, pos) -> state.is(ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get())
                && level.getBlockEntity(pos) instanceof ECOCraftingParallelCoreBlockEntity core
                && tier.supportsComponentTier(core.getTier())
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
    }

    @Override
    public boolean isValidBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof NEBlockEntity<?, ?> ne
                && ne.getCalculator() instanceof NECraftingClusterCalculator;
    }
}
