package cn.dancingsnow.neoecoprototype1201.block;

import cn.dancingsnow.neoecoprototype1201.config.NeoECOPrototypeServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Drops eco's crystal; only the breaker is chilled, and an existing chill is never stacked. */
public class SimplifyCryotheumOreBlock extends Block {
    public SimplifyCryotheumOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player != null) {
            int ticks = NeoECOPrototypeServerConfig.CRYOTHEUM_ORE_FREEZE_TICKS.get();
            if (ticks > 0 && player.getTicksFrozen() <= 0) {
                player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze(), ticks));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
