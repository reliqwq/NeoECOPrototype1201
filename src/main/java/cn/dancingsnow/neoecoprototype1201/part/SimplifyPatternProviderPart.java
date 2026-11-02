package cn.dancingsnow.neoecoprototype1201.part;

import appeng.api.parts.IPartItem;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.parts.PartModel;
import appeng.parts.crafting.PatternProviderPart;
import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyPatternProviderBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * The provider on one face of a cable bus, same 27 slots as the block.
 *
 * <p>Base model is ours (AE2's own part draws its purple front); the three status overlays are AE2's
 * interface ones, because AE2's provider has no status art of its own and borrows exactly those - so the
 * channel ring behaves the same here as it does in AE2's machine.
 *
 * <p>This is the first part on the line, which is why the model paths go through
 * {@code PartModels.registerModels} in the client class: this generation gives no registration hook for a
 * part type at all (an {@code IPartItem} in our item register is the whole of it), but the cable-bus model
 * only bakes names that were handed to that list before AE2 freezes it.
 */
public class SimplifyPatternProviderPart extends PatternProviderPart {
    private static final ResourceLocation BASE_MODEL =
            ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "part/pattern_provider");
    private static final PartModel MODELS_OFF = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_off"));
    private static final PartModel MODELS_ON = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_on"));
    private static final PartModel MODELS_HAS_CHANNEL = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_has_channel"));

    /**
     * Only our own model path: the three status overlays are AE2's interface models and AE2 already hands
     * them to that list itself. What has to be added before the freeze is ours, or the cable bus looks up a
     * name that was never baked and the part draws nothing.
     */
    public static final java.util.List<ResourceLocation> MODELS = java.util.List.of(BASE_MODEL);

    public SimplifyPatternProviderPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(getMainNode(), this,
                SimplifyPatternProviderBlockEntity.PATTERN_SLOTS);
    }

    /**
     * Same silent-overload trap as on the block entity: this generation's host interface names the parameter
     * {@code MenuLocator}, 1.21.1 names it {@code MenuHostLocator}.
     */
    @Override
    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Registration.L1_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(Registration.L1_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public PartModel getStaticModels() {
        if (isActive() && isPowered()) {
            return MODELS_HAS_CHANNEL;
        }
        return isPowered() ? MODELS_ON : MODELS_OFF;
    }
}
