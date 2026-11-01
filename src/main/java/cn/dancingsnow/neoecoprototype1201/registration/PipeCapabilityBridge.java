package cn.dancingsnow.neoecoprototype1201.registration;

import cn.dancingsnow.neoecoae.platform.ForgeBlockCapabilities;
import cn.dancingsnow.neoecoae.util.DrainOnlyFluidHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * eco hands pipes their inventories through {@link ForgeBlockCapabilities}, whose table is keyed by
 * {@code BlockEntityType}: its {@code attach} listener looks up {@code be.getType()} and returns
 * immediately when that map holds nothing. eco only binds its own types, and every machine on this line
 * is registered under ours, so a pipe asking our fluid input hatch for a tank was answered "no
 * capability" while the tank sat in the block entity. Each binding below copies the lambda eco uses for
 * that same class, including the fact that it ignores the side.
 */
public final class PipeCapabilityBridge {

    public static void register() {
        ForgeBlockCapabilities.register(ForgeCapabilities.FLUID_HANDLER,
                ModBlockEntities.FLUID_INPUT_HATCH.get(), (be, side) -> be.getFluidTankHandler());
        ForgeBlockCapabilities.register(ForgeCapabilities.FLUID_HANDLER,
                ModBlockEntities.FLUID_OUTPUT_HATCH.get(),
                (be, side) -> new DrainOnlyFluidHandler(be.getFluidTankHandler()));
        ForgeBlockCapabilities.register(ForgeCapabilities.ITEM_HANDLER,
                ModBlockEntities.DRIVE.get(), (be, side) -> be.HANDLER);
        ForgeBlockCapabilities.register(ForgeCapabilities.ITEM_HANDLER,
                ModBlockEntities.CRAFTING_PATTERN_BUS.get(), (be, side) -> be.itemHandler);
        ForgeBlockCapabilities.register(ForgeCapabilities.ITEM_HANDLER,
                ModBlockEntities.COMPUTATION_DRIVE.get(), (be, side) -> be.getItemHandler());
    }

    private PipeCapabilityBridge() {
    }
}
