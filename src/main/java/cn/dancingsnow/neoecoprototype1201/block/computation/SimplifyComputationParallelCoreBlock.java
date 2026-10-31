package cn.dancingsnow.neoecoprototype1201.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationParallelCore;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The parallel core sits in the computation panel's upper and lower rows, and eco asks this block -
 * not the block entity - which texture to cut those cells from.
 */
public class SimplifyComputationParallelCoreBlock extends ECOComputationParallelCore
        implements IHostPanelTextureProvider {
    private static final ResourceLocation UPPER = ResourceLocation.fromNamespaceAndPath(
            Prototype1201.MOD_ID, "textures/gui/panel/computation_upper.png");
    private static final ResourceLocation LOWER = ResourceLocation.fromNamespaceAndPath(
            Prototype1201.MOD_ID, "textures/gui/panel/computation_lower.png");

    public SimplifyComputationParallelCoreBlock(Properties properties) {
        this(properties, SimplifyTier.L1);
    }

    public SimplifyComputationParallelCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }

    @Override
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part) {
        return switch (part) {
            case COMPUTATION_UPPER -> UPPER;
            case COMPUTATION_LOWER -> LOWER;
            default -> null;
        };
    }
}
