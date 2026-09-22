package cn.dancingsnow.neoecoprototype1201.block.storage;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoprototype1201.blockentity.storage.SimplifyDriveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * L1 drive block - holds one ECO-compatible storage cell. Interaction matches
 * eco's drive: right click with a cell inserts it; sneak + right click (no item)
 * removes it.
 */
public class SimplifyDriveBlock extends NEBlock<SimplifyDriveBlockEntity> {

    public static final BooleanProperty HAS_CELL = BooleanProperty.create("has_cell");
    public static final EnumProperty<CellKind> CELL_KIND = EnumProperty.create("cell_kind", CellKind.class);

    public enum CellKind implements net.minecraft.util.StringRepresentable {
        ITEM("item"), FLUID("fluid"), CHEMICAL("chemical");

        private final String name;

        CellKind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public SimplifyDriveBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(FORMED, false)
                .setValue(HAS_CELL, false)
                .setValue(CELL_KIND, CellKind.ITEM)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_CELL, CELL_KIND);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                    InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SimplifyDriveBlockEntity be)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!be.hasCell() && be.isItemValid(held)) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            be.insertCell(held);
            if (!player.isCreative()) {
                held.shrink(1);
                if (held.isEmpty()) {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                }
            }
            return InteractionResult.CONSUME;
        }
        if (be.hasCell() && held.isEmpty() && player.isShiftKeyDown()) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            ItemStack removed = be.removeCell();
            if (!removed.isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, removed);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }
}
