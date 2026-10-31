package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.api.ECOComputationModels;
import cn.dancingsnow.neoecoae.client.NEItemColors;
import cn.dancingsnow.neoecoae.client.rendering.FixedBlockEntityRenderers;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype1201.client.ComputationCableModels;
import cn.dancingsnow.neoecoprototype1201.client.renderer.blockentity.SimplifyComputationDriveRenderer;
import cn.dancingsnow.neoecoprototype1201.client.renderer.blockentity.SimplifyDriveRenderer;
import cn.dancingsnow.neoecoprototype1201.registration.ModBlockEntities;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
    /**
     * One 3D cell model per capacity, in the order {@code Registration.SUFFIXES} registers the items:
     * eco's built-in cell models draw its own housing and level band, and the L4 one it used to fall back
     * to paints orange - a tier this addon does not have. The three shared sheets (housing, level band,
     * type glyph) are ours; the 4M cell swaps only the level band, which is why it is a separate model.
     */
    private static final String[] STORAGE_CELL_MODEL_NAMES = {
            "storage_cell_l0_item_1k", "storage_cell_l0_item_16k", "storage_cell_l0_item_64k",
            "storage_cell_l1_item", "storage_cell_l1r_item"};

    private static ResourceLocation cellModel(String name) {
        return ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "block/cell/" + name);
    }

    private static final ResourceLocation COMPUTATION_CELL_L1 =
            computationModel("cell_l1");
    private static final ResourceLocation COMPUTATION_CELL_L1_FORMED =
            computationModel("cell_l1_formed");
    private static final ResourceLocation COMPUTATION_CELL_L4 =
            computationModel("cell_l4");
    private static final ResourceLocation COMPUTATION_CELL_L4_FORMED =
            computationModel("cell_l4_formed");

    private static ResourceLocation computationModel(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                Prototype1201.MOD_ID, "block/computation_cell/" + name);
    }

    private Prototype1201Client() {
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // A cell model the drive draws is not baked unless something asks for it explicitly.
        for (String name : STORAGE_CELL_MODEL_NAMES) {
            event.register(cellModel(name));
        }
        // Same rule for the computation cells and the cables the computation drive tessellates.
        event.register(COMPUTATION_CELL_L1);
        event.register(COMPUTATION_CELL_L1_FORMED);
        event.register(COMPUTATION_CELL_L4);
        event.register(COMPUTATION_CELL_L4_FORMED);
        event.register(ComputationCableModels.LOWER_CONNECTED);
        event.register(ComputationCableModels.LOWER_DISCONNECTED);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (int slot = 0; slot < Registration.CELLS.size() && slot < STORAGE_CELL_MODEL_NAMES.length; slot++) {
                ECOCellModels.register(Registration.CELLS.get(slot).get(),
                        cellModel(STORAGE_CELL_MODEL_NAMES[slot]));
            }
            // No ItemBlockRenderTypes.setRenderLayer here on purpose. Forge 1.20.1 reads "render_type" from
            // the model itself (NamedRenderTypeManager registers solid/cutout/cutout_mipped/translucent, and
            // BlockModel bakes with a RenderTypeGroup), and its "forge:composite" dispatches each child to
            // that child's own layer - which is how eco does it: 194 of its models declare a render_type, 19
            // are composites, and 0 classes touch ItemBlockRenderTypes. The copied models carry those keys
            // now, so a code-side layer would only be a second pass over the same quads.
            // One rename is applied while porting the art: 1.21.1 says "cutout", which on this engine maps to
            // RenderType.cutout() - the no-mipmap block sheet that moires at grazing angles. Our models say
            // "cutout_mipped" instead.

            // Two slots per computation cell (idle and working). Both of our tiers wear the plain
            // cable pair: on this line eco keys the cable table by tier alone, so a host-specific
            // pair cannot be told apart from our own frame's.
            ECOComputationModels.registerCellModel(Registration.SIMPLIFY_COMPUTATION_CELL_1M,
                    COMPUTATION_CELL_L1, COMPUTATION_CELL_L1_FORMED);
            ECOComputationModels.registerCellModel(Registration.ENERGIZED_COMPUTATION_CELL_4M,
                    COMPUTATION_CELL_L4, COMPUTATION_CELL_L4_FORMED);
            ECOComputationModels.registerCableModel(SimplifyTier.L1,
                    ComputationCableModels.LOWER_DISCONNECTED, ComputationCableModels.LOWER_CONNECTED);
            ECOComputationModels.registerCableModel(SimplifyTier.L1_REINFORCED,
                    ComputationCableModels.LOWER_DISCONNECTED, ComputationCableModels.LOWER_CONNECTED);
            ECOComputationModels.runDeferredRegistration();
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DRIVE.get(), SimplifyDriveRenderer::new);
        // eco bakes fixed block-entity geometry into the chunk mesh, so the type has to be in its
        // table for the cell and cable to appear. The normal renderer is empty and registered only
        // because the 1.21.1 line does the same.
        FixedBlockEntityRenderers.register(ModBlockEntities.COMPUTATION_DRIVE.get(),
                new SimplifyComputationDriveRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.COMPUTATION_DRIVE.get(),
                SimplifyComputationDriveRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        // eco 20.5.3 exposes the tint function itself; the per-item registration helper is gone.
        event.register(NEItemColors::getCellColor, Registration.CELLS.stream()
                .map(RegistryObject::get)
                .toArray(Item[]::new));
    }
}
