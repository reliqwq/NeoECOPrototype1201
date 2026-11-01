package cn.dancingsnow.neoecoprototype1201.menu;

import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.api.inventories.InternalInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.locator.MenuLocators;
import appeng.menu.locator.MenuLocator;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.OutputSlot;
import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyStonecuttingAssemblerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * The L1 processor assembler's menu: AE2's molecular-assembler layout without the encoded-pattern slot,
 * because this machine is meant to be driven by a pattern provider. The grid slots are display-only.
 *
 * <p>The pattern slot is the manual hole. AE2 picks up any hand-placed crafting pattern inside
 * {@code MolecularAssemblerBlockEntity.recalculatePlan()}, which never passes {@code pushPattern} - so the
 * pattern gate on the block entity cannot refuse it, and leaving the slot in would make this machine a
 * green-skinned AE2 assembler. The slot has to go, and going through AE2's own menu class is not an option
 * either: its constructor names {@code MolecularAssemblerMenu.TYPE} as its own type, and Forge 1.20.1 puts
 * {@code AbstractContainerMenu.getType()} on the wire ({@code NetworkHooks.openScreen}, offset 142), so a
 * subclass of AE2's menu would still be sent - and opened - as AE2's machine.</p>
 */
public class ProcessorAssemblerMenu extends UpgradeableMenu<SimplifyStonecuttingAssemblerBlockEntity>
        implements IProgressProvider {
    /** AE2's assembler inventory is grid 0..8, output 9, encoded pattern 10 - we simply stop at 9. */
    private static final int GRID_SLOTS = 9;
    private static final int OUTPUT_SLOT = 9;

    private final SimplifyStonecuttingAssemblerBlockEntity assembler;
    private int craftProgress;

    public ProcessorAssemblerMenu(int id, Inventory inv, SimplifyStonecuttingAssemblerBlockEntity host) {
        super(Registration.PROCESSOR_ASSEMBLER_MENU.get(), id, inv, host);
        assembler = host;
    }

    @Override
    protected void setupConfig() {
        super.setupConfig();
        // setupConfig runs from the super constructor, before this.assembler is assigned.
        InternalInventory inventory = ((SimplifyStonecuttingAssemblerBlockEntity) getHost())
                .getSubInventory(MolecularAssemblerBlockEntity.INV_MAIN);
        for (int slot = 0; slot < GRID_SLOTS; slot++) {
            addSlot(new FakeSlot(inventory, slot), SlotSemantics.MACHINE_CRAFTING_GRID);
        }
        addSlot(new OutputSlot(inventory, OUTPUT_SLOT, null), SlotSemantics.MACHINE_OUTPUT);
    }

    /**
     * AE2's {@code MolecularAssemblerMenu} implements this for its own grid slots; the same numbers drive
     * the vertical progress bar drawn from our style file.
     */
    @Override
    public void broadcastChanges() {
        craftProgress = assembler.getCraftingProgress();
        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return craftProgress;
    }

    @Override
    public int getMaxProgress() {
        return 100;
    }

    /**
     * Builds the menu on whichever side received the open packet, reading the two values AE2's own
     * {@code MenuTypeBuilder} writes: the locator that names the machine, then the sub-screen flag.
     */
    public static ProcessorAssemblerMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buf) {
        MenuLocator locator = MenuLocators.readFromPacket(buf);
        SimplifyStonecuttingAssemblerBlockEntity host =
                locator.locate(inv.player, SimplifyStonecuttingAssemblerBlockEntity.class);
        if (host == null) {
            throw new IllegalStateException("Could not find a processor assembler to open "
                    + Registration.PROCESSOR_ASSEMBLER_MENU.getId());
        }
        ProcessorAssemblerMenu menu = new ProcessorAssemblerMenu(windowId, inv, host);
        menu.setReturnedFromSubScreen(buf.readBoolean());
        return menu;
    }
}
