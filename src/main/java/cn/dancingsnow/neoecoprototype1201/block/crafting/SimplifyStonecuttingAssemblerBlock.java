package cn.dancingsnow.neoecoprototype1201.block.crafting;

import appeng.block.crafting.MolecularAssemblerBlock;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import cn.dancingsnow.neoecoprototype1201.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * L1 processor assembler. Green cube over AE2's assembler behaviour, AE2's assembler GUI, and our own menu
 * type so the encoded-pattern slot can be refused - see
 * {@link cn.dancingsnow.neoecoprototype1201.menu.ProcessorAssemblerMenu}. What was already ours before this
 * screen existed is the pattern gate:
 * {@link cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyStonecuttingAssemblerBlockEntity}.
 */
public class SimplifyStonecuttingAssemblerBlock extends MolecularAssemblerBlock {
    public SimplifyStonecuttingAssemblerBlock(Properties properties) {
        super(properties);
    }

    /**
     * AE2's own implementation names {@code MolecularAssemblerMenu.TYPE} here in hard-coded form, so the
     * only way to hand the GUI to our menu type is this override. Everything else - the alternate-use
     * (wrench) branch, the client/server answer - is kept exactly as AE2 writes it.
     */
    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player player, InteractionHand hand,
                                         ItemStack heldItem, BlockHitResult hit) {
        MolecularAssemblerBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null || InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            MenuOpener.open(Registration.PROCESSOR_ASSEMBLER_MENU.get(), player,
                    MenuLocators.forBlockEntity(blockEntity));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
