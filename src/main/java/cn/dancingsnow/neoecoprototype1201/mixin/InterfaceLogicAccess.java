package cn.dancingsnow.neoecoprototype1201.mixin;

import appeng.helpers.InterfaceLogic;
import appeng.util.ConfigInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/* Version lock: Forge 47.4.20, AE2 15.4.10. Re-checked against that jar on 2026-10-31: InterfaceLogic still
 * holds `private final ConfigInventory config` and `... storage` (both need @Mutable to swap), and still
 * declares the two private listeners AE2 hands each inventory - onConfigRowChanged() for config,
 * onStorageChanged() for storage (bootstrap order 1 and 2 of the four-argument constructor).
 * remap = false because the target is a mod, not Minecraft: these names survive into the shipped jar. */
@Mixin(value = InterfaceLogic.class, remap = false)
public interface InterfaceLogicAccess {
    @Mutable
    @Accessor("config")
    void neoecoprototype$setConfig(ConfigInventory config);

    @Mutable
    @Accessor("storage")
    void neoecoprototype$setStorage(ConfigInventory storage);

    @Invoker("onConfigRowChanged")
    void neoecoprototype$onConfigRowChanged();

    @Invoker("onStorageChanged")
    void neoecoprototype$onStorageChanged();
}
