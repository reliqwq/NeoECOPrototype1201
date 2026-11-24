package cn.dancingsnow.neoecoprototype1201.menu;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.FakeSlot;
import net.minecraft.world.item.ItemStack;

/** A preview slot that refuses writes because the underlying assembler grid persists real stacks. */
public final class DisplayOnlyGridSlot extends FakeSlot {

    public DisplayOnlyGridSlot(InternalInventory inventory, int slot) {
        super(inventory, slot);
    }

    @Override
    public void set(ItemStack stack) {
    }

    @Override
    public void setFilterTo(ItemStack stack) {
    }
}
