package cn.dancingsnow.neoecoprototype1201.block.crafting;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * F1 subsystem interface: the plain endpoint that joins the cluster but opens nothing. The GUI lives on
 * {@code simplify_crafting_network_interface}, the block the lang files call 通讯接口 - the same rule C1 and
 * L1 draw, and the reason this class exists at all.
 *
 * <p>eco 20.5.3 has no hook for "this machine has no UI": its {@code NEEntityBlock.onActivated} dispatches an
 * empty-handed click straight to {@code useWithoutItem}, which is where {@code ECOMachineInterface} opens the
 * interface UI. Answering PASS here leaves the click to AE2's own handling - orientation and wrench gestures
 * still go through, since those come from the item-held branch of the same method.</p>
 */
public class SimplifyCraftingInterfaceBlock extends ECOMachineInterface<NECraftingCluster> {
    public SimplifyCraftingInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}
