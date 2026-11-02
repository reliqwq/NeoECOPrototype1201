package cn.dancingsnow.neoecoprototype1201.block.decoration;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The plushie drawn three times her size, standing on the one block she was set down on.
 *
 * <p>She draws nothing herself: the client renderer scales the plain doll's model up over this cell, which
 * keeps her artwork a single file - a redraw of the plushie lands here in the same frame it lands on the
 * shelf. Everything else about her is the doll's own behaviour, inherited: she turns to face whoever sets
 * her down and creepers keep their distance.</p>
 */
public class GiantDollBlock extends NamedDollBlock {
    public GiantDollBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
