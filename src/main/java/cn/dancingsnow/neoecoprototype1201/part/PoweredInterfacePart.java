package cn.dancingsnow.neoecoprototype1201.part;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.api.parts.IPartItem;
import appeng.helpers.InterfaceLogic;
import appeng.parts.PartModel;
import appeng.parts.misc.InterfacePart;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyPoweredMEInterfaceBlockEntity;
import net.minecraft.resources.ResourceLocation;

/**
 * Cable-mounted AE2 interface that also provides 200 AE/t. Ported from the 1.21.1 line with one
 * deliberate drop: that version overrides openMenu to open this addon's own interface screen, but
 * this line's interfaces keep AE2's own screen on both the block and the part (the block side is
 * AE2's InterfaceBlock with no subclass), so the part inherits AE2's opener and the two forms
 * behave identically.
 */
public class PoweredInterfacePart extends InterfacePart implements IPassiveEnergyGenerator {
    private static final ResourceLocation BASE_MODEL = ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "part/powered_me_interface");
    private static final PartModel MODELS_OFF = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_off"));
    private static final PartModel MODELS_ON = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_on"));
    private static final PartModel MODELS_HAS_CHANNEL = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_has_channel"));

    /**
     * Only our own model path: the three status overlays are AE2's interface models and AE2 already
     * hands them to that list itself. What has to be added before the freeze is ours, or the cable bus
     * looks up a name that was never baked and the part draws nothing.
     */
    public static final java.util.List<ResourceLocation> MODELS = java.util.List.of(BASE_MODEL);

    private boolean suppressed;

    public PoweredInterfacePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode().setIdlePowerUsage(0).addService(IPassiveEnergyGenerator.class, this);
    }

    @Override
    protected InterfaceLogic createLogic() {
        return new InterfaceLogic(getMainNode(), this, getPartItem().asItem(), 18);
    }

    /** Suppressed or not, this is what the part would produce: AE2 compares these rates to re-pick
     *  its generator, so answering 0 while suppressed would strand the network on the weaker one. */
    @Override
    public double getRate() {
        return SimplifyPoweredMEInterfaceBlockEntity.GENERATION_RATE;
    }

    @Override
    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    @Override
    public boolean isSuppressed() {
        return suppressed;
    }

    @Override
    public PartModel getStaticModels() {
        if (isActive() && isPowered()) {
            return MODELS_HAS_CHANNEL;
        }
        return isPowered() ? MODELS_ON : MODELS_OFF;
    }
}
