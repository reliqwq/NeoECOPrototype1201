package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.client.NEItemColors;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

/** Client wiring for the cell family: drive model, model baking, and the status light tint. */
@Mod.EventBusSubscriber(modid = Prototype1201.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Prototype1201Client {
    private Prototype1201Client() {
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // A cell model the drive draws is not baked unless something asks for it explicitly.
        event.register(ECOCellModels.STORAGE_CELL_L4_ITEM);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> Registration.CELLS.forEach(cell ->
                ECOCellModels.register(cell.get(), ECOCellModels.STORAGE_CELL_L4_ITEM)));
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        NEItemColors.registerEcoCellStatusLights(event, Registration.CELLS.stream()
                .map(RegistryObject::get)
                .toArray(Item[]::new));
    }
}
