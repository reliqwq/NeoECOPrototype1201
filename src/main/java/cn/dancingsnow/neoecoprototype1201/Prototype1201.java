package cn.dancingsnow.neoecoprototype1201;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype1201.block.computation.SimplifyComputationSystemBlock;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyComputationClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyCraftingClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype1201.registration.ModBlockEntities;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
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
        Registration.CELL_TYPES.register(modBus);
        Registration.TABS.register(modBus);
        Registration.RECIPE_TYPES.register(modBus);
        Registration.RECIPE_SERIALIZERS.register(modBus);
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
        reportInterfaceMixinApplied();
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
