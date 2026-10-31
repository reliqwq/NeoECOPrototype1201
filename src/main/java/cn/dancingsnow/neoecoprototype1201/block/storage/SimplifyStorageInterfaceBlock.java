package cn.dancingsnow.neoecoprototype1201.block.storage;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEStorageCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The structural interface of the L1 storage matrix: it feeds the cluster and nothing else.
 *
 * <p>eco 20.5.3 opens its interface UI from {@code NEEntityBlock.onActivated}, which dispatches to the
 * protected {@code useWithoutItem} - live on this line, measured at offset 64 of that method. Answering PASS
 * here leaves the click to whatever the player is holding, so the block reads as the plain machine it is.
 * The GUI belongs to {@code simplify_storage_network_interface}, the communication interface, and only to it
 * - the same split the 1.21.1 line draws, where the plain block has no {@code GuiTitleProvider}.</p>
 */
public class SimplifyStorageInterfaceBlock extends ECOMachineInterface<NEStorageCluster> {
    public SimplifyStorageInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }
}
