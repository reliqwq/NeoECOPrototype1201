package cn.dancingsnow.neoecoprototype1201.block.computation;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The plain endpoint that joins the cluster but opens nothing; the GUI lives on the network interface. */
public class SimplifyComputationInterfaceBlock extends ECOMachineInterface<NEComputationCluster> {
    public SimplifyComputationInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}
