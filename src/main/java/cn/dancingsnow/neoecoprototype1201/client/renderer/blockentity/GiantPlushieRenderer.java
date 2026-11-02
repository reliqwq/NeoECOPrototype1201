package cn.dancingsnow.neoecoprototype1201.client.renderer.blockentity;

import cn.dancingsnow.neoecoprototype1201.block.decoration.NamedDollBlock;
import cn.dancingsnow.neoecoprototype1201.blockentity.decoration.NamedDollBlockEntity;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.client.model.data.ModelData;

/**
 * The giant plushie: one block, three times her size, standing with her feet on the block she was set down
 * on. The block itself carries no geometry - it is {@code INVISIBLE} - and this draws the plain doll's own
 * baked model over it, so there is exactly one copy of her artwork in the jar and a redraw of the plushie
 * shows up here the same frame it shows up on the shelf.
 *
 * <p>She is drawn through the plain doll's block state rather than her own, and turned the same way she was
 * set down: the doll's blockstate names the four {@code facing} turns, so asking for that state's model
 * already gives the model rotated, and the giant's own state is {@code INVISIBLE} - which
 * {@code BlockRenderDispatcher#renderSingleBlock} refuses to draw at all.</p>
 */
public class GiantPlushieRenderer implements BlockEntityRenderer<NamedDollBlockEntity> {
    /** How many times the plain doll's size she is drawn at. */
    public static final float SCALE = 3.0F;

    public GiantPlushieRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NamedDollBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.getBlockState().getBlock()
                != ModRegistration.FUMO_DOLL_YANG120_GIANT_BLOCK.get()) {
            // The other dolls hang on this same block entity type and draw themselves the ordinary way.
            return;
        }
        var state = blockEntity.getBlockState();
        // Her own state cannot be what we ask to draw: BlockRenderDispatcher#renderSingleBlock reads
        // getRenderShape() first and returns without drawing a single quad for INVISIBLE - which is exactly
        // how she is set up, so the block she hangs on must not be the block we ask for. The plain doll's
        // state carries the same four facing turns, so her orientation comes through unchanged.
        var drawn = ModRegistration.FUMO_DOLL_YANG120_BLOCK.get().defaultBlockState()
                .setValue(NamedDollBlock.FACING, state.getValue(NamedDollBlock.FACING));
        poseStack.pushPose();
        // Grow out of the block's bottom centre, so her feet stay on the floor of the cell that was placed.
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(drawn, poseStack, bufferSource,
                packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();
    }

    /**
     * The drawing leaves the block it hangs on by three times its size, so she has to stay drawn while any
     * part of her is on screen - vanilla's default test is "is the one cube she sits on visible".
     */
    @Override
    public boolean shouldRenderOffScreen(NamedDollBlockEntity blockEntity) {
        return true;
    }
}
