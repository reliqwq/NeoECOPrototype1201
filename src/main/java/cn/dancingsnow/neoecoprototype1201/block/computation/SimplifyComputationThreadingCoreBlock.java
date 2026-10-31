package cn.dancingsnow.neoecoprototype1201.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationThreadingCore;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** The threading core is the middle row of the computation panel. */
public class SimplifyComputationThreadingCoreBlock extends ECOComputationThreadingCore
        implements IHostPanelTextureProvider {
    private static final ResourceLocation CORE = ResourceLocation.fromNamespaceAndPath(
            Prototype1201.MOD_ID, "textures/gui/panel/computation_core.png");
    private static final ResourceLocation CORE_ENERGIZED = ResourceLocation.fromNamespaceAndPath(
            Prototype1201.MOD_ID, "textures/gui/panel/computation_core_energized.png");

    public SimplifyComputationThreadingCoreBlock(Properties properties) {
        this(properties, SimplifyTier.L1);
    }

    public SimplifyComputationThreadingCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }

    @Override
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part) {
        if (part != IHostPanelTextureProvider.Part.COMPUTATION_CORE) {
            return null;
        }
        // All four of our tiers report the same tier index, so the artwork keys off block identity.
        return getTier() == SimplifyTier.L1_ENERGIZED_THREADING ? CORE_ENERGIZED : CORE;
    }
}
