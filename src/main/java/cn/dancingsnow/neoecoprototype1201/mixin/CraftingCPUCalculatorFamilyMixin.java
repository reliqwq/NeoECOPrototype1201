package cn.dancingsnow.neoecoprototype1201.mixin;

/*
 * Version lock: Forge 47.4.20, AE2 15.4.10. Checked against that jar on 2026-11-03: CraftingCPUCalculator
 * declares public CraftingCPUCalculator(CraftingBlockEntity) and overrides two questions its scan asks -
 * public boolean isValidBlockEntity(BlockEntity), which MBCalculator#isValidBlockEntityAt calls for every
 * cell it thinks about adding (measured: invokevirtual at offset 16 of that private method, reached from
 * seven call sites inside the scan), and public boolean checkMultiblockScale(BlockPos, BlockPos), which
 * judges the box once the growth stops (invokevirtual at offset 294). All three requires below bind to
 * those; if any disappears this does not go quiet, it fails at class transformation.
 * remap = false because the targets are AE2's own members, which keep their names in the shipped jar.
 */

import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.me.cluster.implementations.CraftingCPUCalculator;
import cn.dancingsnow.neoecoprototype1201.block.crafting.DollCraftingUnitBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Who may stand in a plushie's CPU, and in what shape.
 *
 * <p>An AE2 crafting CPU is found by geometry rather than by network: the scan asks one question per cell -
 * "are you a crafting block entity" - and then one question about the box it grew - "is this size mine". A
 * plushie answers both narrowly. It refuses AE2's storage cells on both sides, so an AE2 wall stood against
 * a plushie stays its own machine and the plushie stays its own; and it accepts one shape only - a single
 * plushie - so a pile of them is no CPU at all rather than a bigger one nobody meant to build.</p>
 */
@Mixin(value = CraftingCPUCalculator.class, remap = false)
public abstract class CraftingCPUCalculatorFamilyMixin {
    /** Whether the block this scan belongs to is a plushie rather than one of AE2's own crafting units. */
    @Unique private boolean neoecoprototype$plushieScan;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void neoecoprototype$rememberWhoseWallThisScanIs(CraftingBlockEntity target, CallbackInfo ci) {
        this.neoecoprototype$plushieScan = target.getBlockState().getBlock() instanceof DollCraftingUnitBlock;
    }

    @Inject(method = "isValidBlockEntity", at = @At("HEAD"), cancellable = true, require = 1)
    private void neoecoprototype$keepPlushiesOutOfStorageWalls(BlockEntity candidate,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (!(candidate instanceof CraftingBlockEntity cpu)) {
            return;
        }
        cir.setReturnValue(this.neoecoprototype$plushieScan
                == (cpu.getBlockState().getBlock() instanceof DollCraftingUnitBlock));
    }

    @Inject(method = "checkMultiblockScale", at = @At("HEAD"), cancellable = true, require = 1)
    private void neoecoprototype$plushieFormsSoloOrThreeCubed(BlockPos min, BlockPos max,
                                                              CallbackInfoReturnable<Boolean> cir) {
        if (!this.neoecoprototype$plushieScan) {
            return;
        }
        // AE2 hands over the two corners, so a single plushie is the box that spans 0 on every axis.
        cir.setReturnValue(fillsThisSpan(min, max, 0));
    }

    /** Whether the box between the two corners is exactly {@code span} cells wide on all three axes. */
    @Unique private boolean fillsThisSpan(BlockPos min, BlockPos max, int span) {
        return max.getX() - min.getX() == span && max.getY() - min.getY() == span
                && max.getZ() - min.getZ() == span;
    }
}
