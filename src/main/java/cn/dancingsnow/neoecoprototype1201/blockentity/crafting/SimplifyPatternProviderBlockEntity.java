package cn.dancingsnow.neoecoprototype1201.blockentity.crafting;

import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import cn.dancingsnow.neoecoprototype1201.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * AE2's provider behaviour under the L1 green box, with the frame's own slot count.
 *
 * <p>Three rows of nine, the same number the 1.21.1 line pins. AE2's two-argument
 * {@code PatternProviderLogic} constructor is {@code this(mainNode, host, 9)} - measured, not inferred -
 * so the count only becomes 27 by overriding {@link #createLogic()} and calling the three-argument one.
 *
 * <p>Nothing is overridden for the menu on purpose: {@code PatternProviderBlock.onActivated} asks the
 * block entity, whose {@code openMenu} is a default method on {@code PatternProviderLogicHost} that opens
 * AE2's own provider menu, and that menu adds one slot per {@code getPatternInv().size()} (measured: the
 * loop bound is the inventory's own size, not a literal nine). Twenty-seven slots therefore work in
 * AE2's GUI; the drawn row is the reason this machine still wants its own screen.
 */
public class SimplifyPatternProviderBlockEntity extends PatternProviderBlockEntity {
    /** Pinned to the art: the box face draws three rows of nine, and the 1.21.1 line ships the same. */
    public static final int PATTERN_SLOTS = 27;

    public SimplifyPatternProviderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(getMainNode(), this, PATTERN_SLOTS);
    }

    /**
     * Both of these are default methods on this generation's {@code PatternProviderLogicHost}, and their
     * parameter type is {@code MenuLocator} - 1.21.1 calls it {@code MenuHostLocator}, so copying the 1.21
     * override verbatim would compile as a brand-new overload and silently keep AE2's own menu, whose style
     * lays all nine-odd extra slots off the panel.
     */
    @Override
    public void openMenu(net.minecraft.world.entity.player.Player player, appeng.menu.locator.MenuLocator locator) {
        appeng.menu.MenuOpener.open(Registration.L1_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(net.minecraft.world.entity.player.Player player,
                                 appeng.menu.ISubMenu subMenu) {
        appeng.menu.MenuOpener.returnTo(Registration.L1_PATTERN_PROVIDER_MENU.get(), player,
                subMenu.getLocator());
    }
}
