package cn.dancingsnow.neoecoprototype1201.blockentity.crafting;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.helpers.InterfaceLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoprototype1201.integration.ae2.OversizeInterfaceLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Energized superconductive interface: an 18-slot AE2 interface with a dedicated two-row screen, one marker
 * able to stock 8192 instead of a stack, and a passive 4000 AE/t feed into the grid.
 *
 * <p>The marker count stays at AE2's nine because the shared panel has nine amount buttons per row; widening
 * the count without widening the panel would hide the extra slots.</p>
 */
public class SimplifySuperconductiveInterfaceBlockEntity extends InterfaceBlockEntity
        implements IPassiveEnergyGenerator {
    public static final double GENERATION_RATE = 4000.0D;

    private boolean suppressed;

    public SimplifySuperconductiveInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode()
                .setIdlePowerUsage(0)
                .addService(IPassiveEnergyGenerator.class, this);
    }

    @Override
    protected InterfaceLogic createLogic() {
        var logic = new InterfaceLogic(getMainNode(), this, getBlockState().getBlock().asItem(), 18);
        OversizeInterfaceLogic.install(logic);
        return logic;
    }

    /** Our own block, not AE2's: see {@code SimplifyPoweredMEInterfaceBlockEntity#getMainMenuIcon}. */
    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getBlockState().getBlock());
    }

    @Override
    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Registration.POWERED_INTERFACE_PART_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(Registration.POWERED_INTERFACE_PART_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public double getRate() {
        return suppressed ? 0.0D : GENERATION_RATE;
    }

    @Override
    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    @Override
    public boolean isSuppressed() {
        return suppressed;
    }
}
