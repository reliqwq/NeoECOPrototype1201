package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.client.NEItemColors;
import cn.dancingsnow.neoecoprototype1201.client.renderer.blockentity.SimplifyDriveRenderer;
import cn.dancingsnow.neoecoprototype1201.registration.ModBlockEntities;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

/** Client wiring for the cell family: drive model and renderer, model baking, and the status light tint. */
@Mod.EventBusSubscriber(modid = Prototype1201.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Prototype1201Client {
    /** eco's built-in cell models all draw its blue level band; this addon ships a green one. */
    private static final ResourceLocation CELL_MODEL =
            ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "block/cell/prototype_item_cell");

    private Prototype1201Client() {
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // A cell model the drive draws is not baked unless something asks for it explicitly.
        event.register(CELL_MODEL);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Registration.CELLS.forEach(cell -> ECOCellModels.register(cell.get(), CELL_MODEL));
            // The copied models carry no render_type (that key is NeoForge-only). Only these four
            // have transparent pixels in their textures - drive, casing and vent are fully opaque and
            // must stay on the solid layer. Mipped matters: RenderType#cutout samples the block atlas
            // with BLOCK_SHEET (no mipmaps) while the atlas is built with mips, so every face moires
            // at grazing angles until the player squints at it.
            RenderType cutout = RenderType.cutoutMipped();
            for (Block block : new Block[]{
                    ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                    ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get(),
                    ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                    ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get()}) {
                ItemBlockRenderTypes.setRenderLayer(block, cutout);
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DRIVE.get(), SimplifyDriveRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        NEItemColors.registerEcoCellStatusLights(event, Registration.CELLS.stream()
                .map(RegistryObject::get)
                .toArray(Item[]::new));
    }
}
