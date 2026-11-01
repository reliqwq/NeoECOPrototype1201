package cn.dancingsnow.neoecoprototype1201.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import cn.dancingsnow.neoecoprototype1201.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype1201.recipe.ProcessorAssemblerRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembler for the addon's processor recipes; it refuses ordinary molecular-assembly patterns.
 *
 * <p>Inheriting AE2's assembler makes this machine a plain molecular assembler to anything that asks by
 * behaviour - a pattern provider included - so an ordinary pattern can be pushed in from the network. That
 * is refused on purpose, and the refusal is logged, because a silent one leaves the player watching
 * materials that never move.</p>
 */
public class SimplifyStonecuttingAssemblerBlockEntity extends MolecularAssemblerBlockEntity {
    /** Named, because a refusal is the reading a player reports and a stack trace is not. */
    private static final Logger LOGGER = LoggerFactory.getLogger("neoeco1201-assembler");

    /** The last pattern output we logged a refusal for, so a provider retrying every tick logs once. */
    @Nullable
    private AEItemKey refusedOutput;

    public SimplifyStonecuttingAssemblerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * AE2's implementation names and icons this machine as its own assembler block - both halves are
     * hard-coded constants that never look at the block state - so an inheriting addon machine handed its
     * own identity to every asker: terminals listed our assembler as "分子装配室" with AE2's item icon.
     */
    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        PatternContainerGroup inherited = super.getCraftingMachineInfo();
        return new PatternContainerGroup(
                AEItemKey.of(getBlockState().getBlock()),
                hasCustomName() ? getCustomName() : getBlockState().getBlock().getName(),
                inherited.tooltip());
    }

    @Override
    public boolean pushPattern(IPatternDetails pattern, KeyCounter[] inputs, Direction direction) {
        if (level == null || pattern instanceof ProcessorAssemblyPattern) {
            return super.pushPattern(pattern, inputs, direction);
        }
        ProcessorAssemblerRecipe recipe = matchRecipe(pattern);
        if (recipe == null) {
            logRefusal(pattern, "这台机器只跑处理器装配配方，普通分子装配样板请交给分子装配室");
            return false;
        }
        ProcessorAssemblyPattern plan = new ProcessorAssemblyPattern(pattern, recipe, this::deliverSurplus);
        // Filling the grid throws when the batch cannot be held, and a throw here rides straight out of
        // AE2's grid tick and crashes the world - so the fit is asked about first and refused quietly.
        if (plan.fittingRuns(inputs) <= 0) {
            logRefusal(pattern, "一批原料摆不进 9 格工作台，或者一次产出装不下一组");
            return false;
        }
        refusedOutput = null;
        return super.pushPattern(plan, inputs, direction);
    }

    /**
     * Where the part of a finished cycle that did not fit into the one ejected stack goes: this grid's
     * storage, since a cycle can only hand over a single stack of an item. Whatever the network will not
     * take falls next to the machine - the alternative is that the materials simply stop existing.
     */
    private void deliverSurplus(AEItemKey what, long amount) {
        long left = amount;
        var grid = getMainNode().getGrid();
        if (grid != null) {
            left -= grid.getStorageService().getInventory().insert(what, left,
                    Actionable.MODULATE, IActionSource.ofMachine(this));
        }
        if (left <= 0) {
            return;
        }
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            LOGGER.warn("[stonecutting assembler] had no output for {} x {} at {}",
                    what, left, worldPosition);
            return;
        }
        var center = worldPosition.getCenter();
        int stackSize = what.toStack(1).getMaxStackSize();
        while (left > 0) {
            int count = (int) Math.min(stackSize, left);
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, center.x, center.y, center.z,
                    new ItemStack(what.getItem(), count)));
            left -= count;
        }
    }

    /**
     * Refusing silently is what made this impossible to diagnose from a report: the crafting CPU had
     * already accepted the job, so all the player sees is materials that never move. Logged once per
     * machine per output, because a provider retries a refused pattern every tick. Each slot prints
     * candidates x per-candidate amount x multiplier - the multiplier is where AE2 keeps "this slot needs
     * two of the item", and reading past it is how the repeated-ingredient recipes were miscounted.
     */
    private void logRefusal(IPatternDetails pattern, String reason) {
        GenericStack[] outputs = pattern.getOutputs();
        AEItemKey output = outputs.length == 1 && outputs[0].what() instanceof AEItemKey key ? key : null;
        if (output == null || output.equals(refusedOutput)) {
            return;
        }
        refusedOutput = output;
        StringBuilder slots = new StringBuilder();
        for (IPatternDetails.IInput slot : pattern.getInputs()) {
            GenericStack[] possible = slot.getPossibleInputs();
            slots.append('[').append(possible.length).append('x')
                    .append(possible.length > 0 ? possible[0].amount() : 0).append('x')
                    .append(slot.getMultiplier()).append(']');
        }
        LOGGER.warn("[stonecutting assembler] refused a pattern at {}: {}；outputs={} "
                        + "patternClass={} slots={} askedBy={} (每槽 = 候选数 x 每候选数量 x 批量倍数)",
                worldPosition, reason, java.util.Arrays.toString(outputs),
                pattern.getClass().getSimpleName(), slots, describeCallers());
    }

    /**
     * The first few callers outside our own code, so a refusal says who pushed. Inheriting AE2's assembler
     * makes this machine a target for anything that walks ICraftingMachine, and which thing walked in is
     * not something the refusal should leave to inference.
     */
    private String describeCallers() {
        var picked = new java.util.ArrayList<String>();
        for (var frame : Thread.currentThread().getStackTrace()) {
            String cls = frame.getClassName();
            if (cls.startsWith("java.") || cls.startsWith("jdk.")
                    || cls.startsWith("cn.dancingsnow.neoecoprototype1201.")) {
                continue;
            }
            picked.add(cls.substring(cls.lastIndexOf('.') + 1) + "#" + frame.getMethodName());
            if (picked.size() == 4) {
                break;
            }
        }
        return picked.isEmpty() ? "unknown" : String.join(" < ", picked);
    }

    /** What the pattern asks for, or null when no declared or derived recipe covers it. */
    @Nullable
    private ProcessorAssemblerRecipe matchRecipe(IPatternDetails pattern) {
        GenericStack[] outputs = pattern.getOutputs();
        if (outputs.length != 1 || !(outputs[0].what() instanceof AEItemKey outputKey)) {
            return null;
        }
        return ProcessorAssemblerRecipes.resolve(level, pattern, outputKey.getItem());
    }
}
