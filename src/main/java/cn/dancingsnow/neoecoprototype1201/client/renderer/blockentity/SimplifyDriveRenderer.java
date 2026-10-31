package cn.dancingsnow.neoecoprototype1201.client.renderer.blockentity;

import appeng.api.storage.cells.CellState;
import appeng.client.render.tesr.CellLedRenderer;
import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.client.rendering.IFixedBlockEntityRenderer;
import cn.dancingsnow.neoecoae.blocks.entity.storage.ECODriveBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;

/**
 * Draws the mounted cell into the drive cavity and lights its status LED.
 *
 * <p>Without this the cavity shows bare interior faces, which read as black panels - eco hides them
 * the same way for its own drives. Shape copied from eco 1.20.1's {@code ECODriveRenderer}; the
 * infinite-member branch is left out because that cell family does not exist on this line.
 */
public class SimplifyDriveRenderer implements BlockEntityRenderer<ECODriveBlockEntity>,
        IFixedBlockEntityRenderer<ECODriveBlockEntity> {

    public SimplifyDriveRenderer() {
    }

    public SimplifyDriveRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ECODriveBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderFixed(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        if (!blockEntity.isMounted() || !blockEntity.isOnline()) {
            return;
        }
        CellState cellState = blockEntity.getCellState();
        if (cellState != null && cellState != CellState.ABSENT) {
            renderLed(blockEntity, poseStack, bufferSource, cellState.getStateColor());
        }
    }

    @Override
    public void renderFixed(ECODriveBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack cellStack = blockEntity.getCellStack();
        if (cellStack == null || cellStack.isEmpty()) {
            return;
        }
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(yRotForFacing(facing)));
        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.translate(2 / 16f, 2 / 16f, 0 / 16f);
        ResourceLocation modelLocation = ECOCellModels.getModelLocation(cellStack.getItem());
        tessellateModel(poseStack, bufferSource, modelLocation, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderLed(ECODriveBlockEntity blockEntity, PoseStack poseStack,
                                  MultiBufferSource bufferSource, int stateColor) {
        int red = stateColor >> 16 & 255;
        int green = stateColor >> 8 & 255;
        int blue = stateColor & 255;

        BlockState blockState = blockEntity.getBlockState();
        Direction face = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(face.getRotation());
        poseStack.translate(0, -0.501, 0);

        float pixel = 1f / 16;
        float xStart = -5 * pixel;
        float zStart = -5 * pixel;
        float xEnd = xStart + pixel;
        float zEnd = zStart + pixel * 2;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(CellLedRenderer.RENDER_LAYER);
        consumer.vertex(matrix, xStart, 0, zStart).color(red, green, blue, 255).endVertex();
        consumer.vertex(matrix, xEnd, 0, zStart).color(red, green, blue, 255).endVertex();
        consumer.vertex(matrix, xEnd, 0, zEnd).color(red, green, blue, 255).endVertex();
        consumer.vertex(matrix, xStart, 0, zEnd).color(red, green, blue, 255).endVertex();
        poseStack.popPose();
    }

    private static float yRotForFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> 0f;
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default -> 0f;
        };
    }
}
