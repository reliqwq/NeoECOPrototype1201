package cn.dancingsnow.neoecoprototype1201;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** 1.20.1 line of the L1 storage addon. See README for what is deliberately not ported. */
@Mod(Prototype1201.MOD_ID)
public final class Prototype1201 {
    public static final String MOD_ID = "neoecoprototype1201";

    /** Forge 47 only ever calls a no-argument constructor; the injected bus is NeoForge-only. */
    public Prototype1201() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.ITEMS.register(modBus);
        Registration.CELL_TYPES.register(modBus);
        Registration.TABS.register(modBus);
    }
}
