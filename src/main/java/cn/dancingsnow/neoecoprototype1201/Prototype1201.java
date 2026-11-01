package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationSystemBlock;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyComputationClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyCraftingClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.registration.ModBlockEntities;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import cn.dancingsnow.neoecoprototype1201.registration.PipeCapabilityBridge;
import cn.dancingsnow.neoecoprototype1201.integration.appmek.AppMekIntegration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** 1.20.1 line of the L1 storage addon. See the README for the scope of this checkout. */
@Mod(Prototype1201.MOD_ID)
public final class Prototype1201 {
    public static final String MOD_ID = "neoecoprototype1201";
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Prototype1201.class);

    /** Forge 47 only ever calls a no-argument constructor; the injected bus is NeoForge-only. */
    public Prototype1201() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistration.BLOCKS.register(modBus);
        ModRegistration.BLOCK_ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        Registration.ITEMS.register(modBus);
        // The chemical family exists only with Applied Mekanistics loaded, and the check has to live in
        // this method: taking the branch is what keeps the jar from ever resolving the third party on a
        // machine that does not have it. Entries must be added before the registry event fires, so this is
        // the constructor rather than commonSetup.
        if (ModList.get().isLoaded("appmek")) {
            AppMekIntegration.register(Registration.ITEMS);
        }
        // Same shape as the chemical family above, and the same reason for the branch living here: taking
        // it is what keeps this jar from resolving appflux on a machine that does not have it, because
        // eco's FE cell constructor hands FluxKeyType.TYPE to its own superclass. That is safe only because
        // the reference sits in a method body - the omni family could not be shipped this way, since its
        // superclass chain itself reaches OmniCells and Forge 1.20.1 force-loads every class in our jar
        // while building event handlers.
        if (ModList.get().isLoaded("appflux")) {
            cn.dancingsnow.neoecoprototype1201.integration.appflux.FeStorageIntegration
                    .register(Registration.ITEMS);
        }
        Registration.TABS.register(modBus);
        Registration.RECIPE_TYPES.register(modBus);
        Registration.RECIPE_SERIALIZERS.register(modBus);
        Registration.MENUS.register(modBus);
        modBus.addListener(Prototype1201::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(Prototype1201::serverStopping);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        // First, because everything below reads types that must already exist: AE2's representative-item
        // table is filled here rather than at registry time (see ModBlockEntities#registerRepresentativeItems).
        ModBlockEntities.registerRepresentativeItems();
        // Both hosts are our subclasses, but neither can hand over its calculator any other way: eco's
        // host constructors take (type, pos, state, tier) and install eco's own calculator through that,
        // so the per-type table is the only hand-over point - and it must come after the types exist.
        NEBlockEntity.registerCalculatorFactory(ModBlockEntities.COMPUTATION_SYSTEM.get(),
                SimplifyComputationClusterCalculator::new);
        NEBlockEntity.registerCalculatorFactory(ModBlockEntities.CRAFTING_SYSTEM.get(),
                SimplifyCraftingClusterCalculator::new);
        // Every storage member, not just the host: AE2's flood-fill can start the scan from whichever node
        // changed, and eco's own drive/vent/energy-cell constructors install eco's L4 calculator, so a scan
        // starting there would validate our shell against eco's geometry and never form.
        NEBlockEntity.registerCalculatorFactory(ModBlockEntities.STORAGE_HOST.get(),
                SimplifyStorageClusterCalculator::new);
        NEBlockEntity.registerCalculatorFactory(ModBlockEntities.DRIVE.get(),
                SimplifyStorageClusterCalculator::new);
        NEBlockEntity.registerCalculatorFactory(ModBlockEntities.STORAGE_VENT.get(),
                SimplifyStorageClusterCalculator::new);
        NEBlockEntity.registerCalculatorFactory(ModBlockEntities.ENERGY_CELL.get(),
                SimplifyStorageClusterCalculator::new);
        NEBlockEntity.registerCalculatorFactory(ModBlockEntities.STORAGE_CASING.get(),
                SimplifyStorageClusterCalculator::new);
        NEBlockEntity.registerCalculatorFactory(ModBlockEntities.STORAGE_INTERFACE.get(),
                SimplifyStorageClusterCalculator::new);
        NEBlockEntity.registerCalculatorFactory(ModBlockEntities.STORAGE_NETWORK_INTERFACE.get(),
                SimplifyStorageClusterCalculator::new);
        // Same shape of gap as the calculator table above: eco's pipe-facing capabilities are keyed by
        // BlockEntityType, so our types have to be handed over one by one.
        PipeCapabilityBridge.register();
        LOGGER.info("bound our hatch, drive, pattern bus and computation drive to eco's pipe capability table");
        // The infinite concrete matrix is vanilla-only, so its handler needs no mod check. eco's own drive
        // asks this table for every stack it mounts, which is what makes a stateless cell like ours work
        // without eco ever knowing the item.
        event.enqueueWork(cn.dancingsnow.neoecoprototype1201.items.InfiniteConcreteCellHandler::register);
        registerAssemblerMenu();
        reportJeiPluginScanData();
        reportInterfaceMixinApplied();
    }

    /**
     * Teaching AE2 to open our assembler menu. {@code MenuOpener} is AE2's own table of
     * "menu type - how do I ask the server to open it", and AE2 only fills it through
     * {@code MenuTypeBuilder}, which on this line can name an entry only inside AE2's own namespace. Our
     * type therefore joins the table by hand, writing the same two values AE2's builder writes so its
     * client-side reader finds what it expects.
     */
    /**
     * TEMP PROBE (2026-11-01, delete after reading): JEI finds plugins through Forge's annotation
     * scan data, and our @JeiPlugin class is not being handed to it in a dev run while eco's - which
     * sits in a jar - is. This says which half is missing: whether our own mod file enumerates the
     * class at all, and whether the annotation reaches the scan data.
     */
    private static void reportJeiPluginScanData() {
        int annotations = 0;
        int ours = 0;
        StringBuilder jei = new StringBuilder();
        for (var scanData : net.minecraftforge.fml.ModList.get().getAllScanData()) {
            for (var annotation : scanData.getAnnotations()) {
                annotations++;
                String member = annotation.memberName();
                if (member.startsWith("cn/dancingsnow/neoecoprototype1201/")
                        || member.startsWith("cn.dancingsnow.neoecoprototype1201.")) {
                    ours++;
                }
                if (member.contains("JeiPlugin")) {
                    jei.append(" [").append(annotation.annotationType()).append(" on ").append(member).append("]");
                }
            }
        }
        LOGGER.info("[probe] FML scan annotations={}, ours={}, JeiPlugin hits={}",
                annotations, ours, jei.length() == 0 ? "none" : jei.toString().trim());
    }

    private static void registerAssemblerMenu() {
        var menuType = Registration.PROCESSOR_ASSEMBLER_MENU.get();
        appeng.menu.MenuOpener.addOpener(menuType, (player, locator, linked) -> {
            if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
                return false;
            }
            var host = locator.locate(player,
                    cn.dancingsnow.neoecoprototype1201.blockentity.crafting
                            .SimplifyStonecuttingAssemblerBlockEntity.class);
            if (host == null) {
                return false;
            }
            // Built from the resolved host, not from the packet: this is how AE2's own MenuTypeBuilder opens
            // a machine menu, and on this line MenuType.create's third argument is the buffer, which only the
            // client ever has.
            var finalHost = host;
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,
                    new net.minecraft.world.SimpleMenuProvider((windowId, inventory, p) -> {
                        var menu = new cn.dancingsnow.neoecoprototype1201.menu.ProcessorAssemblerMenu(
                                windowId, inventory, finalHost);
                        menu.setLocator(locator);
                        return menu;
                    }, finalHost.getBlockState().getBlock().getName()),
                    buffer -> {
                        appeng.menu.locator.MenuLocators.writeToPacket(buffer, locator);
                        buffer.writeBoolean(linked);
                    });
            return true;
        });
        LOGGER.info("registered the processor assembler menu as {}",
                net.minecraftforge.registries.ForgeRegistries.MENU_TYPES.getKey(menuType));
    }

    /**
     * Whether the 8192 accessor really landed on AE2's class. This is the one mixin we ship, and an
     * unregistered config fails without any error of its own: {@code (InterfaceLogicAccess) (Object) logic}
     * just throws a ClassCastException at the moment a player places the block, which reads like a bug in
     * the block. The registration path differs per environment - the release jar declares its config through
     * the {@code MixinConfigs} manifest attribute while a dev run gets it from {@code --mixin.config} - so the
     * only thing both have in common is this reading.
     */
    private static void reportInterfaceMixinApplied() {
        boolean applied = false;
        for (Class<?> provided : appeng.helpers.InterfaceLogic.class.getInterfaces()) {
            if (provided == cn.dancingsnow.neoecoprototype1201.mixin.InterfaceLogicAccess.class) {
                applied = true;
            }
        }
        if (applied) {
            LOGGER.info("L1 interface accessor is mixed into appeng.helpers.InterfaceLogic; "
                    + "a superconductive marker may stock {}",
                    cn.dancingsnow.neoecoprototype1201.integration.ae2.OversizeInterfaceLogic.MAX_AMOUNT);
        } else {
            LOGGER.warn("L1 interface accessor is NOT mixed into appeng.helpers.InterfaceLogic - the "
                    + "mixin config did not register, so the 8192 marker cap is inactive and placing a "
                    + "superconductive interface will throw. Release jar needs MixinConfigs in its manifest; "
                    + "a dev run needs --mixin.config neoecoprototype1201.mixins.json.");
        }
    }

    private static void serverStopping(ServerStoppingEvent event) {
        // The parked shapes are keyed by dimension id, so they outlive a save; drop them with the server.
        SimplifyComputationSystemBlock.clearPendingShapes();
    }
}
