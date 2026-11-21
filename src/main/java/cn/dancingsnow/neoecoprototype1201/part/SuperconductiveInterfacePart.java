package cn.dancingsnow.neoecoprototype1201.part;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.api.parts.IPartItem;
import appeng.helpers.InterfaceLogic;
import appeng.parts.PartModel;
import appeng.parts.misc.InterfacePart;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifySuperconductiveInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype1201.integration.ae2.OversizeInterfaceLogic;
import net.minecraft.resources.ResourceLocation;

/**
 * Cable-mounted superconductive interface: 4,000 AE/t and the oversize amount rows, on a cable face.
 * Ported from the 1.21.1 line with the same drop as the powered part - the menus stay AE2's own,
 * because this line's interfaces keep AE2's screen on the block side too.
 */
public class SuperconductiveInterfacePart extends InterfacePart implements IPassiveEnergyGenerator {
    private static final ResourceLocation BASE_MODEL = ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "part/superconductive_interface");
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

    public SuperconductiveInterfacePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode()
                .setIdlePowerUsage(0)
                .addService(IPassiveEnergyGenerator.class, this);
    }

    @Override
    protected InterfaceLogic createLogic() {
        var logic = new InterfaceLogic(getMainNode(), this, getPartItem().asItem(), 18);
        OversizeInterfaceLogic.install(logic);
        return logic;
    }

    /** Suppressed or not, this is what the part would produce: AE2 compares these rates to re-pick
     *  its generator, so answering 0 while suppressed would strand the network on the weaker one. */
    @Override
    public double getRate() {
        return SimplifySuperconductiveInterfaceBlockEntity.GENERATION_RATE;
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
