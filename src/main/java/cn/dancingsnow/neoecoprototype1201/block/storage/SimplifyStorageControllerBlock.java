package cn.dancingsnow.neoecoprototype1201.block.storage;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyStorageHostBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/** L1 storage subsystem host (controller) block. */
public class SimplifyStorageControllerBlock extends NEBlock<SimplifyStorageHostBlockEntity> {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty MIRRORED = BooleanProperty.create("mirrored");

    public SimplifyStorageControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(FORMED, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(MIRRORED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MIRRORED);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                    InteractionHand hand, BlockHitResult hitResult) {
        if (!isPlayerCloseEnough(level, pos, player)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isShiftKeyDown()) {
                if (level.getBlockEntity(pos) instanceof SimplifyStorageHostBlockEntity host) {
                    if (host.getCluster() != null) {
                        host.breakCluster();
                    }
                    host.rebuildMultiblock();
                }
                return InteractionResult.CONSUME;
            }
            // M1 has no panel yet, so the block reports its formation state in chat instead: that is
            // also the signal used when verifying the port by hand.
            if (level.getBlockEntity(pos) instanceof SimplifyStorageHostBlockEntity host) {
                serverPlayer.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        "block.neoecoprototype1201.storage_matrix_formed."
                                + (host.isFormed() ? "yes" : "no")), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean isPlayerCloseEnough(Level level, BlockPos pos, Player player) {
        return player.level() == level
                && level.getBlockState(pos).getBlock() instanceof SimplifyStorageControllerBlock
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}
