package cn.dancingsnow.neoecoprototype1201.multiblock.definition;

import cn.dancingsnow.neoecoae.blocks.ECOMachineCasing;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationCoolingController;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationParallelCore;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationThreadingCore;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyMultiblockConfig;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** Placement definition for the ECO - L1 computation subsystem. */
public final class SimplifyComputationDefinition {
    public static final MultiBlockDefinition L1 = create();

    private SimplifyComputationDefinition() {
    }

    private static MultiBlockDefinition create() {
        Supplier<? extends Block> owner = ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK;
        BlockState casing = ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK.get().defaultBlockState();
        BlockState parallelCore = ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get()
                .defaultBlockState()
                .setValue(ECOComputationParallelCore.FACING, Direction.SOUTH);
        BlockState threadingCore = ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get()
                .defaultBlockState()
                .setValue(ECOComputationThreadingCore.FACING, Direction.SOUTH);
        BlockState cooler = ModRegistration.SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK.get()
                .defaultBlockState()
                .setValue(ECOComputationCoolingController.FACING, Direction.WEST);

        return MultiBlockDefinition.builder(owner)
                .setBlock(pos(1, 1, 0), ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get().defaultBlockState())
                .setBlock(pos(1, 0, 0), casing)
                .setBlock(pos(2, 0, 0), casing)
                .setBlock(pos(2, 1, 0), casing)
                .setBlock(pos(1, 2, 0), casing)
                .setBlock(pos(2, 2, 0), casing)
                .setBlock(pos(1, 0, 1), casing)
                .setBlock(pos(2, 0, 1), casing)
                .setBlock(pos(2, 1, 1), ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get().defaultBlockState())
                .setBlock(pos(1, 1, 1), casing)
                .setBlock(pos(1, 2, 1), casing)
                .setBlock(pos(2, 2, 1), casing)
                .setBlock(pos(0, 0, 0), casing)
                .setBlock(pos(0, 1, 0), casing)
                .setBlock(pos(0, 2, 0), casing)
                .setBlock(pos(0, 0, 1), casing)
                .setBlock(pos(0, 1, 1), casing)
                .setBlock(pos(0, 2, 1), casing)
                .setBlockRepeatable(pos(-1, 1, 0), Direction.WEST,
                        ModRegistration.SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK.get().defaultBlockState())
                .setBlockRepeatable(pos(-1, 2, 0), Direction.WEST,
                        ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get().defaultBlockState())
                .setBlockRepeatable(pos(-1, 0, 0), Direction.WEST,
                        ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get().defaultBlockState())
                .setBlockRepeatable(pos(-1, 0, 1), Direction.WEST, parallelCore)
                .setBlockRepeatable(pos(-1, 1, 1), Direction.WEST, threadingCore)
                .setBlockRepeatable(pos(-1, 2, 1), Direction.WEST, parallelCore)
                .setBlockWithRepeatShifted(pos(-1, 1, 0), Direction.WEST, 0, cooler)
                .setBlockWithRepeatShifted(pos(-1, 2, 0), Direction.WEST, 0, casing)
                .setBlockWithRepeatShifted(pos(-1, 0, 0), Direction.WEST, 0, casing)
                .setBlockWithRepeatShifted(pos(-1, 0, 1), Direction.WEST, 0, casing)
                .setBlockWithRepeatShifted(pos(-1, 1, 1), Direction.WEST, 0, casing)
                .setBlockWithRepeatShifted(pos(-1, 2, 1), Direction.WEST, 0, casing)
                .expandMin(1)
                // Our own constant, not eco's config field: eco keeps its max length in a plain static int it
                // only fills when its server config loads, so reading it here bakes in 0 whenever this class
                // initialises first, and the build plan comes out as expandMin 1 / expandMax -4.
                .expandMax(SimplifyMultiblockConfig.L1_PLACEMENT_EXPAND_MAX)
                .onFormed((blockPos, level) -> {
                    BlockState state = level.getBlockState(blockPos);
                    BlockState formedState = state;
                    if (state.hasProperty(NEBlock.FORMED)) {
                        formedState = formedState.setValue(NEBlock.FORMED, true);
                    }
                    if (formedState.hasProperty(ECOMachineCasing.INVISIBLE)) {
                        formedState = formedState.setValue(ECOMachineCasing.INVISIBLE, true);
                    }
                    if (formedState != state) {
                        level.setBlockAndUpdate(blockPos, formedState);
                    }
                })
                // The Consumer overload is the one that does NOT append to NEMultiBlocks.DEFINITIONS, and that
                // is deliberate: touching that field runs eco's own <clinit>, which reads its max-length
                // statics before the server config has loaded.
                .create(definition -> {
                });
    }

    private static BlockPos pos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }
}
