package cn.dancingsnow.neoecoprototype1201.mixin;

/*
 * Version lock: Forge 47.4.20, AE2 15.4.10. Checked against that jar on 2026-11-03: CraftingBlockEntity
 * declares public Set<Direction> getGridConnectableSides(BlockOrientation), and the constructor of its
 * parent AENetworkBlockEntity asks that question itself - offsets 9-38 of that constructor run
 * createMainNode, setVisualRepresentation, setInWorldNode(true), setTagName and then
 * onGridConnectableSidesChanged(), which is what re-reads it. updateSubType(boolean) asks again on every
 * change of formation. The require below binds to that method: if it disappears this does not go quiet, it
 * fails at class transformation. remap = false because the target is a mod, not Minecraft: AE2's own method
 * names survive into the shipped jar, so no refmap is needed to land this one.
 */

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.crafting.CraftingBlockEntity;
import cn.dancingsnow.neoecoprototype1201.block.crafting.DollCraftingUnitBlock;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;
import java.util.Set;

/**
 * A plushie hands its channels out of its bottom face, so it has to be sat on top of the network and a cable
 * stood beside it reaches nobody.
 *
 * <p>This cannot be done from our side by re-calling {@code setExposedOnSides}: AE2 refreshes the exposed
 * sides from {@link CraftingBlockEntity#getGridConnectableSides} every time formation changes, and the block
 * entity must stay exactly AE2's class for the grid's machine table to find it. So the answer is given here,
 * and only for our block - every other crafting unit keeps AE2's six-sided behaviour.</p>
 */
@Mixin(value = CraftingBlockEntity.class, remap = false)
public abstract class CraftingBlockEntityDollSidesMixin {
    @Inject(method = "getGridConnectableSides", at = @At("HEAD"), cancellable = true, require = 1)
    private void neoecoprototype$plushieCpuConnectsDownwardsOnly(BlockOrientation orientation,
                                                                CallbackInfoReturnable<Set<Direction>> cir) {
        CraftingBlockEntity self = (CraftingBlockEntity) (Object) this;
        if (!(self.getBlockState().getBlock() instanceof DollCraftingUnitBlock)) {
            return;
        }
        // The plushie is its own machine, so a formed one answers on its bottom face and nowhere else:
        // down is how it reaches the network, and a cable touching any other face finds nobody.
        cir.setReturnValue(self.isFormed() ? EnumSet.of(Direction.DOWN) : EnumSet.noneOf(Direction.class));
    }
}
