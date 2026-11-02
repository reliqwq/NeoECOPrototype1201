package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.api.ECOComputationModels;
import cn.dancingsnow.neoecoae.client.NEItemColors;
import cn.dancingsnow.neoecoae.client.rendering.FixedBlockEntityRenderers;
import cn.dancingsnow.neoecoprototype1201.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype1201.client.ComputationCableModels;
import cn.dancingsnow.neoecoprototype1201.client.renderer.blockentity.GiantPlushieRenderer;
import cn.dancingsnow.neoecoprototype1201.client.renderer.blockentity.SimplifyComputationDriveRenderer;
import cn.dancingsnow.neoecoprototype1201.client.renderer.blockentity.SimplifyDriveRenderer;
import cn.dancingsnow.neoecoprototype1201.integration.appmek.AppMekIntegration;
import cn.dancingsnow.neoecoprototype1201.integration.appflux.FeStorageIntegration;
import cn.dancingsnow.neoecoprototype1201.registration.ModBlockEntities;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

/** Client wiring for the cell family: drive model and renderer, model baking, and the status light tint. */
@Mod.EventBusSubscriber(modid = Prototype1201.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Prototype1201Client {
    /**
     * AE2's part-model list is write-once, and the deadline is not a lifecycle event we can outrank: the
     * freeze runs wherever {@code PartModels.freeze()} is called, which is
     * {@code appeng.init.client.InitAdditionalModels.init(ModelEvent.RegisterAdditional)} - and
     * {@code appeng.client.render.cablebus.CableBusModel} reaches the same place while a model graph
     * resolves. {@code registerModels} then throws
     * {@code IllegalStateException: Cannot register models after the pre-initialization phase!}, and on
     * Forge 1.20.1 that throw arrives inside the client-setup work queue, so it does not just fail the
     * part - it crashes the game on launch (measured on 2026-11-02 with exactly that stack).
     *
     * <p>Mod construction is the one phase where no mod can have baked anything yet, which is why the
     * caller lives in {@link Prototype1201}'s constructor behind a dist check rather than in any event
     * handler here. A missing entry is worse than a late one: {@code CableBusBakedModel} then dies with
     * "Trying to use an unregistered part model", so there is no try/catch here either.</p>
     */
    public static void registerPartModels() {
        appeng.api.parts.PartModels.registerModels(
                cn.dancingsnow.neoecoprototype1201.part.SimplifyPatternProviderPart.MODELS);
    }

    /**
     * One 3D cell model per capacity per medium, in the order the family's own list registers its items:
     * eco's built-in cell models draw its own housing and level band, and the L4 one it used to fall back
     * to paints orange - a tier this addon does not have. The three shared sheets (housing, level band,
     * type glyph) are ours; the 4M cell swaps only the level band, which is why it is a separate model.
     */
    private static String[] rungs(String medium) {
        return new String[] {"storage_cell_l0_" + medium + "_1k", "storage_cell_l0_" + medium + "_16k",
                "storage_cell_l0_" + medium + "_64k", "storage_cell_l1_" + medium,
                "storage_cell_l1r_" + medium};
    }

    /** appflux's own ladder starts at 1M, so its 1K rung is ours alone and it has no 16k or 64k. */
    private static final String[] FE_RUNGS = {
            "storage_cell_l0_fe_1k", "storage_cell_l1_fe", "storage_cell_l1r_fe"};
    private static final String PIGMEE_RUNG = "storage_cell_l1_pigmee";
    private static final String CONCRETE_RUNG = "storage_cell_l1_concrete";

    /** Everything the drive can be asked to draw, for the bake pass. */
    private static String[] everyCellModel() {
        String[][] groups = {rungs("item"), rungs("fluid"), rungs("chemical"), FE_RUNGS};
        java.util.List<String> all = new java.util.ArrayList<>();
        for (String[] group : groups) {
            all.addAll(java.util.List.of(group));
        }
        all.add(PIGMEE_RUNG);
        all.add(CONCRETE_RUNG);
        return all.toArray(new String[0]);
    }

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

    /** Pair a family's items, taken in its own list order, with the rung models declared above. */
    private static void registerLadder(java.util.List<RegistryObject<Item>> cells, String[] models) {
        for (int slot = 0; slot < cells.size() && slot < models.length; slot++) {
            ECOCellModels.register(cells.get(slot).get(), cellModel(models[slot]));
        }
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // A cell model the drive draws is not baked unless something asks for it explicitly.
        for (String name : everyCellModel()) {
            event.register(cellModel(name));
        }
        // Same rule for the computation cells and the cables the computation drive tessellates.
        event.register(COMPUTATION_CELL_L1);
        event.register(COMPUTATION_CELL_L1_FORMED);
        event.register(COMPUTATION_CELL_L4);
        event.register(COMPUTATION_CELL_L4_FORMED);
        event.register(ComputationCableModels.LOWER_CONNECTED);
        event.register(ComputationCableModels.LOWER_DISCONNECTED);
        event.register(ComputationCableModels.UPPER_CONNECTED);
        event.register(ComputationCableModels.UPPER_DISCONNECTED);
    }

    /**
     * Declare, on purpose, that the assembler's recipes have no place in the vanilla recipe book.
     * {@code ClientRecipeBook.getCategory} asks {@code RecipeBookManager} for every recipe it is handed and
     * logs one {@code Unknown recipe category: <type>/<id>} warning per recipe when the answer is null -
     * once per world join, seven lines for the seven declared assembler recipes. Answering
     * {@link RecipeBookCategories#UNKNOWN} is what the fallback already does; the difference is that the
     * log now says nothing. It must not be a crafting category: {@code RecipeBookCategories.getCategories}
     * buckets recipes by category alone, with no filter on the recipe type, so answering
     * {@code CRAFTING_MISC} would render recipes nobody can craft in a crafting table inside that book's
     * Misc tab. UNKNOWN is in no book's tab list, so nothing appears - which is correct here, because the
     * assembler's own screen has no recipe book and the viewer for these recipes is JEI. A real tab would
     * need {@code RecipeBookCategories.create} plus a {@code RecipeBookType} that one of our menus
     * returns; AE2's three {@code ae2:charger} lines are its own type to declare.
     */
    @SubscribeEvent
    public static void onRegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(Registration.PROCESSOR_ASSEMBLER_TYPE.get(),
                recipe -> RecipeBookCategories.UNKNOWN);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerLadder(Registration.CELLS, rungs("item"));
            registerLadder(Registration.FLUID_CELLS, rungs("fluid"));
            // Both of these lists stay empty unless the mod constructor took its branch, so a pack
            // without the medium simply registers nothing.
            registerLadder(AppMekIntegration.CELLS, rungs("chemical"));
            registerLadder(FeStorageIntegration.CELLS, FE_RUNGS);
            ECOCellModels.register(Registration.PIGMEE_STORAGE_CELL.get(), cellModel(PIGMEE_RUNG));
            ECOCellModels.register(Registration.SIMPLIFY_CONCRETE_STORAGE_CELL.get(),
                    cellModel(CONCRETE_RUNG));
            // No ItemBlockRenderTypes.setRenderLayer here on purpose. Forge 1.20.1 reads "render_type" from
            // the model itself (NamedRenderTypeManager registers solid/cutout/cutout_mipped/translucent, and
            // BlockModel bakes with a RenderTypeGroup), and its "forge:composite" dispatches each child to
            // that child's own layer - which is how eco does it: 194 of its models declare a render_type, 19
            // are composites, and 0 classes touch ItemBlockRenderTypes. The copied models carry those keys
            // now, so a code-side layer would only be a second pass over the same quads.
            // One rename is applied while porting the art: 1.21.1 says "cutout", which on this engine maps to
            // RenderType.cutout() - the no-mipmap block sheet that moires at grazing angles. Our models say
            // "cutout_mipped" instead.

            // Two slots per computation cell (idle and working).
            ECOComputationModels.registerCellModel(Registration.SIMPLIFY_COMPUTATION_CELL_1M,
                    COMPUTATION_CELL_L1, COMPUTATION_CELL_L1_FORMED);
            ECOComputationModels.registerCellModel(Registration.ENERGIZED_COMPUTATION_CELL_4M,
                    COMPUTATION_CELL_L4, COMPUTATION_CELL_L4_FORMED);
            // The glowing pair goes into eco's table. Reaching that table at all means eco's renderer is
            // drawing the drive, so this is the host-specific pair the 1.21.1 line registers against
            // eco's own drive block; our frame never reads it, it hard-codes the plain pair.
            ECOComputationModels.registerCableModel(SimplifyTier.L1,
                    ComputationCableModels.UPPER_DISCONNECTED, ComputationCableModels.UPPER_CONNECTED);
            ECOComputationModels.registerCableModel(SimplifyTier.L1_REINFORCED,
                    ComputationCableModels.UPPER_DISCONNECTED, ComputationCableModels.UPPER_CONNECTED);
            ECOComputationModels.runDeferredRegistration();
            // The assembler's screen. AE2's InitScreens cannot be used for this entry: it resolves the style
            // path through AppEng.makeId, and the style has to come from our namespace because it is what
            // carries the dialog title. Registering against vanilla's table is the same call AE2 itself makes.
            net.minecraft.client.gui.screens.MenuScreens.register(
                    Registration.PROCESSOR_ASSEMBLER_MENU.get(),
                    cn.dancingsnow.neoecoprototype1201.client.ProcessorAssemblerScreen::new);
            // The provider's screen is AE2's own class, pointed at our style doc. Here the ae2-namespace
            // resolution the assembler had to work around is exactly what we want: the style has to
            // "include" AE2's ../pattern_provider.json, so it can only live in AE2's screens folder.
            // Its BREAK_AFTER_9COLS group is what turns our 27 slots into three rows of nine; AE2's own
            // provider style lays its nine out in one row, and that is the only reason 27 needs a doc.
            appeng.init.client.InitScreens.<cn.dancingsnow.neoecoprototype1201.menu.L1PatternProviderMenu,
                    appeng.client.gui.implementations.PatternProviderScreen<
                            cn.dancingsnow.neoecoprototype1201.menu.L1PatternProviderMenu>>register(
                    Registration.L1_PATTERN_PROVIDER_MENU.get(),
                    (menu, inventory, title, style) -> new appeng.client.gui.implementations
                            .PatternProviderScreen<>(menu, inventory, title, style),
                    "/screens/neoecoprototype/l1_pattern_provider.json");
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
        // The dolls draw themselves from their block models; this renderer exists only for the giant
        // plushie, which is an INVISIBLE block scaled up over the plain doll's model. One type covers all
        // five because renderers are registered per block entity type, not per block.
        event.registerBlockEntityRenderer(ModBlockEntities.FUMO_DOLL_BE.get(), GiantPlushieRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        // eco 20.5.3 exposes the tint function itself; the per-item registration helper is gone, so every
        // cell we ship has to be handed over. The two gated families' lists are empty without their mod.
        java.util.List<Item> tinted = new java.util.ArrayList<>();
        Registration.CELLS.forEach(cell -> tinted.add(cell.get()));
        Registration.FLUID_CELLS.forEach(cell -> tinted.add(cell.get()));
        tinted.add(Registration.PIGMEE_STORAGE_CELL.get());
        AppMekIntegration.CELLS.forEach(cell -> tinted.add(cell.get()));
        FeStorageIntegration.CELLS.forEach(cell -> tinted.add(cell.get()));
        // The infinite matrix is not tinted: it reports the same full status forever, so a band that
        // never moves would only be a second name for the same pixel.
        event.register(NEItemColors::getCellColor, tinted.toArray(new Item[0]));
    }
}
