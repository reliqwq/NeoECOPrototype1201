package cn.dancingsnow.neoecoprototype1201.menu;

import appeng.helpers.InterfaceLogicHost;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.locator.MenuLocators;
import cn.dancingsnow.neoecoprototype1201.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/**
 * Dedicated shared menu identity for the L1 powered and superconductive interface blocks and cable parts.
 * AE2 owns the slot/menu behavior; this type supplies its two-row screen style.
 */
public final class PoweredInterfacePartMenu extends InterfaceMenu {
    public PoweredInterfacePartMenu(MenuType<? extends InterfaceMenu> type, int windowId, Inventory inventory,
                                    InterfaceLogicHost host) {
        super(type, windowId, inventory, host);
    }

    /** AE2 menu payload: part locator followed by the sub-screen flag. */
    public static PoweredInterfacePartMenu fromNetwork(int windowId, Inventory inventory, FriendlyByteBuf buffer) {
        var locator = MenuLocators.readFromPacket(buffer);
        InterfaceLogicHost host = locator.locate(inventory.player, InterfaceLogicHost.class);
        if (host == null) {
            throw new IllegalStateException("Could not find a powered interface part for "
                    + Registration.POWERED_INTERFACE_PART_MENU.getId());
        }
        var menu = new PoweredInterfacePartMenu(
                Registration.POWERED_INTERFACE_PART_MENU.get(), windowId, inventory, host);
        menu.setReturnedFromSubScreen(buffer.readBoolean());
        return menu;
    }
}
