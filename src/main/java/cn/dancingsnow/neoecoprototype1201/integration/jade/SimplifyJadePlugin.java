package cn.dancingsnow.neoecoprototype1201.integration.jade;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoprototype1201.integration.jade.provider.SimplifyCraftingHostProvider;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade support for the L1 machines, mirroring eco's own Jade plugin.
 *
 * <p>Nothing here needs a {@code ModList.isLoaded} gate, and that is the point of doing it through Jade's
 * plugin table rather than by calling Jade from our tooltip code: Jade discovers this class by its
 * annotation and is the only reader, so on a pack without Jade the class is never loaded and its Jade
 * imports are never resolved. The one reason this line ever needed a gate - our own code reaching across -
 * does not exist on this path.</p>
 *
 * <p>eco's own Jade providers are registered against eco's block entity classes, and our hosts extend
 * AE2/eco bases rather than eco's host classes, so upstream's tooltip lines never run for our machines.
 * Whatever we want shown has to be named here.</p>
 */
@WailaPlugin(Prototype1201.MOD_ID)
public class SimplifyJadePlugin implements IWailaPlugin {
    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger("neoeco1201-jade");

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, path);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(
                SimplifyCraftingHostProvider.INSTANCE, SimplifyCraftingSystemBlockEntity.class);
        // Logged because discovery is the silent half of this path: Jade reaches our class through its own
        // annotation scan, so if that scan never finds us nothing else complains - the mind line simply does
        // not appear on any tooltip, which reads to a player as a feature that was never built.
        LOGGER.info("jade plugin registered the L1 crafting host's mind data provider");
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(
                SimplifyCraftingHostProvider.INSTANCE, SimplifyCraftingSystemBlock.class);
    }
}
