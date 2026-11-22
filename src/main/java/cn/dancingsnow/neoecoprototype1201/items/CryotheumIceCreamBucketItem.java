package cn.dancingsnow.neoecoprototype1201.items;

import cn.dancingsnow.neoecoprototype1201.block.CryotheumIceCreamSnowBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** A 1000 mB fluid container that places the dessert's powder-snow block on both emptying paths. */
public class CryotheumIceCreamBucketItem extends BucketItem {
    private final Supplier<CryotheumIceCreamSnowBlock> placeable;

    public CryotheumIceCreamBucketItem(Supplier<CryotheumIceCreamSnowBlock> placeable,
                                       Supplier<? extends Fluid> content, Properties properties) {
        super(content, properties);
        this.placeable = placeable;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        // Forge 47's BucketItem.initCapabilities only handles getClass() == BucketItem.class.
        return new FluidBucketWrapper(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!level.isEmptyBlock(pos) || !level.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }
        if (player != null && !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.PASS;
        }
        if (!placeCell(level, pos, player)) {
            return InteractionResult.PASS;
        }
        if (player != null && !player.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack container = new ItemStack(Items.BUCKET);
            if (stack.isEmpty()) {
                player.setItemInHand(context.getHand(), container);
            } else if (!player.getInventory().add(container)) {
                player.drop(container, false);
            }
        }
        return InteractionResult.sidedSuccess(false);
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos,
                                 @Nullable BlockHitResult result) {
        return level.isEmptyBlock(pos) && placeCell(level, pos, player);
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos,
                                 @Nullable BlockHitResult result, @Nullable ItemStack container) {
        return emptyContents(player, level, pos, result);
    }

    private boolean placeCell(Level level, BlockPos pos, @Nullable Player player) {
        if (!level.setBlock(pos, placeable.get().defaultBlockState(), Block.UPDATE_ALL)) {
            return false;
        }
        level.playSound(player, pos, SoundEvents.POWDER_SNOW_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }
}
