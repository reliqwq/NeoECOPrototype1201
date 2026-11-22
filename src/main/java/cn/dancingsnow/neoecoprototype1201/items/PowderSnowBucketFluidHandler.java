package cn.dancingsnow.neoecoprototype1201.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import java.util.function.Supplier;

/**
 * Reads vanilla's SolidBucketItem as 1000 mB of coolant. Forge's normal BucketItem capability
 * does not cover it. Filling an iron bucket still uses FluidUtil and the fluid's vanilla bucket;
 * placing that bucket still follows vanilla's solid powder-snow placement path.
 */
public final class PowderSnowBucketFluidHandler extends FluidBucketWrapper {
    private final Supplier<Fluid> content;

    public PowderSnowBucketFluidHandler(ItemStack container, Supplier<Fluid> content) {
        super(container);
        this.content = content;
    }

    @Override
    public FluidStack getFluid() {
        return container.is(Items.POWDER_SNOW_BUCKET)
                ? new FluidStack(content.get(), FluidType.BUCKET_VOLUME)
                : super.getFluid();
    }

    @Override
    protected void setFluid(FluidStack fluidStack) {
        container = fluidStack.isEmpty()
                ? new ItemStack(Items.BUCKET)
                : net.minecraftforge.fluids.FluidUtil.getFilledBucket(fluidStack);
    }
}
