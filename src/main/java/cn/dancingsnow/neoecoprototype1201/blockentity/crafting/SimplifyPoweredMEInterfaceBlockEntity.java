package cn.dancingsnow.neoecoprototype1201.blockentity.crafting;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.helpers.InterfaceLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import cn.dancingsnow.neoecoprototype1201.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * L1 powered ME interface: an 18-slot AE2 interface with a dedicated two-row screen, plus a passive
 * 200 AE/t feed into the grid.
 *
 * <p>Same two node calls the 1.21.1 line makes: register the generator as a grid service and declare no
 * idle draw. AE2's {@code InterfaceBlockEntity.createMainNode()} builds the node through
 * {@code GridHelper.createManagedNode} and never sets an idle figure itself, so the zero is a statement of
 * intent - a machine that pays the network should not also be billed by it - rather than an override of a
 * measured default.</p>
 */
public class SimplifyPoweredMEInterfaceBlockEntity extends InterfaceBlockEntity
        implements IPassiveEnergyGenerator {
    public static final double GENERATION_RATE = 200.0D;

    private boolean suppressed;

    public SimplifyPoweredMEInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode()
                .setIdlePowerUsage(0)
                .addService(IPassiveEnergyGenerator.class, this);
    }

    @Override
    protected InterfaceLogic createLogic() {
        return new InterfaceLogic(getMainNode(), this, getBlockState().getBlock().asItem(), 18);
    }

    /**
     * AE2's version answers with {@code AEBlocks.INTERFACE} itself, so every sub-screen opened from this
     * machine - the priority panel's back button is where players actually see it - carries AE2's icon and
     * AE2's name. The cable part does not have this problem: {@code InterfacePart} answers with
     * {@code getPartItem()}, which is already ours. This puts the block side on the same footing.
     */
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
