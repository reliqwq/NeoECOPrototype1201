package cn.dancingsnow.neoecoprototype1201.menu;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.menu.locator.MenuLocators;
import appeng.menu.locator.MenuLocator;
import cn.dancingsnow.neoecoprototype1201.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/**
 * AE2's provider menu under a menu type in our own namespace.
 *
 * <p>This generation's {@code MenuTypeBuilder} can only name entries {@code ae2:} (its {@code build(String)}
 * runs the name through {@code AppEng.makeId} and it has no {@code buildUnregistered}), so the type is built
 * by Forge and re-taught to {@code MenuOpener} in {@link cn.dancingsnow.neoecoprototype1201.Prototype1201},
 * the same two steps the assembler's menu needs. The subclass exists only to reach the constructor that
 * takes a {@code MenuType}: AE2's public three-argument one would hand the client AE2's own type.
 *
 * <p>Nothing else is overridden. The 27 slots come from the block entity's logic, not from here: AE2's
 * screen adds one slot widget per {@code getPatternInv().size()} and the shipped style lays that group out
 * as {@code BREAK_AFTER_9COLS}, three rows of nine.
 */
public class L1PatternProviderMenu extends PatternProviderMenu {
    public L1PatternProviderMenu(MenuType<? extends PatternProviderMenu> type, int windowId, Inventory inv,
                                 PatternProviderLogicHost host) {
        super(type, windowId, inv, host);
    }

    /**
     * Reads back the two values our opener writes - the locator naming the machine, then the sub-screen
     * flag - which is what AE2's own builder writes for its types.
     */
    public static L1PatternProviderMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buf) {
        MenuLocator locator = MenuLocators.readFromPacket(buf);
        PatternProviderLogicHost host = locator.locate(inv.player, PatternProviderLogicHost.class);
        if (host == null) {
            throw new IllegalStateException("Could not find a pattern provider to open "
                    + Registration.L1_PATTERN_PROVIDER_MENU.getId());
        }
        L1PatternProviderMenu menu = new L1PatternProviderMenu(
                Registration.L1_PATTERN_PROVIDER_MENU.get(), windowId, inv, host);
        menu.setReturnedFromSubScreen(buf.readBoolean());
        return menu;
    }
}
