package cn.dancingsnow.neoecoprototype1201.block;

import cn.dancingsnow.neoecoprototype1201.registration.ColdContent;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** Powder snow's sinking, extinguishing and freezing, with our own bucket and self-drop loot table. */
public class CryotheumIceCreamSnowBlock extends PowderSnowBlock {
    public CryotheumIceCreamSnowBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        return new ItemStack(ColdContent.CRYOTHEUM_ICE_CREAM_BUCKET.get());
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL_POWDER_SNOW);
    }
}
