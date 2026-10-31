package cn.dancingsnow.neoecoprototype1201.block.computation;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.gui.GuiTitleProvider;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/** The block the player opens: same cluster behaviour as the plain interface plus eco's interface UI. */
public class SimplifyComputationNetworkInterfaceBlock extends ECOMachineInterface<NEComputationCluster>
        implements GuiTitleProvider {
    public SimplifyComputationNetworkInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Component getGuiTitle(BlockState state) {
        return getName();
    }
}
