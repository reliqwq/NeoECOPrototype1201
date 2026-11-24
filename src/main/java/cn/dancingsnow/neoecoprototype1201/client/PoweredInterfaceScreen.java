package cn.dancingsnow.neoecoprototype1201.client;

import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import cn.dancingsnow.neoecoprototype1201.menu.PoweredInterfacePartMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The interface's screen: AE2's interface behavior over the two-group panel.
 *
 * <p>The panel draws four groove rows - config, storage, gap, config, storage - so that each config row has a
 * blank band of its own for its nine amount buttons. AE2's grid only stacks rows at 18px, so the second row of
 * both sections moves down by one extra row pair here, after {@code AEBaseScreen.init} has placed every slot
 * from the style. Both the click target and the drawing read {@code Slot.y}, so this is the one place the
 * offset has to be applied.</p>
 *
 * <p>1.21.1 does the same in its {@code PoweredInterfaceScreen}, through a mixin: there {@code Slot.y} is
 * private. On this line the field is public and AE2's own access transformer already strips its {@code final}
 * at runtime, which is what the entry in our {@code accesstransformer.cfg} asks javac for at compile time.</p>
 */
public class PoweredInterfaceScreen extends InterfaceScreen<PoweredInterfacePartMenu> {
    /** The gap the texture leaves between the two groups, one row pair. */
    private static final int SECOND_ROW_SHIFT = 36;
    private static final Logger LOGGER = LoggerFactory.getLogger("neoeco1201-powered-interface");

    public PoweredInterfaceScreen(PoweredInterfacePartMenu menu, Inventory inventory, Component title,
                                  ScreenStyle style) {
        super(menu, inventory, title, style);
        // AE2 draws the header from the style's dialog_title, and one style serves both interface variants on
        // both carriers, so the header has to follow the host. AE2's own name getters already answer with a
        // player's rename when there is one, which is the rule the terminal list uses for these machines.
        var host = menu.getHost();
        Component hostName = host instanceof appeng.parts.AEBasePart part
                ? part.getName()
                : ((appeng.blockentity.AEBaseBlockEntity) host).getName();
        setTextContent(TEXT_ID_DIALOG_TITLE, hostName);
        LOGGER.info("opened {} as '{}' at {}", host.getClass().getSimpleName(), hostName.getString(),
                host instanceof appeng.blockentity.AEBaseBlockEntity blockEntity
                        ? blockEntity.getBlockPos() : "cable");
    }

    @Override
    protected void init() {
        super.init();
        moveSecondRow(SlotSemantics.CONFIG);
        moveSecondRow(SlotSemantics.STORAGE);
    }

    private void moveSecondRow(SlotSemantic semantic) {
        var slots = getMenu().getSlots(semantic);
        for (int i = 9; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            slot.y += SECOND_ROW_SHIFT;
        }
    }
}
