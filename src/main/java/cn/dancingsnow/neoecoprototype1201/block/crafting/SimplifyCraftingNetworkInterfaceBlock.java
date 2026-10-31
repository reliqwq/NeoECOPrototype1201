package cn.dancingsnow.neoecoprototype1201.block.crafting;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;

/**
 * F1 subsystem communication interface: the block the lang files call 通讯接口, and the only one of the pair
 * that opens eco's interface UI.
 *
 * <p>It extends eco's block directly rather than {@link SimplifyCraftingInterfaceBlock}: that plain class
 * answers PASS to an empty-handed click, and a subclass cannot reach past its parent to eco's own
 * {@code useWithoutItem}. The 1.21.1 line draws the same pair the same way - the plain one suppresses, the
 * network one is a sibling that keeps the UI.</p>
 */
public class SimplifyCraftingNetworkInterfaceBlock extends ECOMachineInterface<NECraftingCluster> {
    public SimplifyCraftingNetworkInterfaceBlock(Properties properties) {
        super(properties);
    }
}
