package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.api.ECOCellModels;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Tells eco which drive model to draw for our cells. */
@Mod.EventBusSubscriber(modid = Prototype1201.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Prototype1201Client {
    private Prototype1201Client() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ECOCellModels.register(Registration.PROBE_CELL.get(),
                ECOCellModels.STORAGE_CELL_L4_ITEM));
    }
}
