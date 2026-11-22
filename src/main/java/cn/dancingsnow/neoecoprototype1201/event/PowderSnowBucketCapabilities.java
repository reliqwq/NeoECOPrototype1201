package cn.dancingsnow.neoecoprototype1201.event;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.items.PowderSnowBucketFluidHandler;
import cn.dancingsnow.neoecoprototype1201.registration.ColdContent;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge bus attachment, limited to vanilla's solid powder-snow bucket. */
@Mod.EventBusSubscriber(modid = Prototype1201.MOD_ID)
public final class PowderSnowBucketCapabilities {
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "powder_snow_bucket");

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (!stack.is(Items.POWDER_SNOW_BUCKET)) {
            return;
        }
        Provider provider = new Provider(stack);
        event.addCapability(ID, provider);
        event.addListener(provider::invalidate);
    }

    // Own the optional here: FluidBucketWrapper's private holder has no invalidation hook in Forge 47.
    private static final class Provider implements ICapabilityProvider {
        private final LazyOptional<IFluidHandlerItem> handler;

        private Provider(ItemStack stack) {
            handler = LazyOptional.of(() ->
                    new PowderSnowBucketFluidHandler(stack, ColdContent.POWDER_SNOW_FLUID::get));
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                        @Nullable Direction side) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(capability, handler);
        }

        private void invalidate() {
            handler.invalidate();
        }
    }

    private PowderSnowBucketCapabilities() {
    }
}
