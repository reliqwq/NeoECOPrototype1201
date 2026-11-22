package cn.dancingsnow.neoecoprototype1201.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import cn.dancingsnow.neoecoprototype1201.recipe.ProcessorAssemblerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One accepted processor-assembler push: its matched recipe, integral run count, and source pattern.
 * This is data only. The legacy assembler BE does not consume this record or accept fluid patterns yet.
 */
public record SimplifyAssemblerJob(IPatternDetails pattern, ProcessorAssemblerRecipe recipe, long runs) {
    public SimplifyAssemblerJob {
        Objects.requireNonNull(recipe, "recipe");
        if (runs <= 0) {
            throw new IllegalArgumentException("assembler job must contain at least one run");
        }
    }

    /** The run count encoded by every output, or zero for an invalid or inconsistent output set. */
    public static long requestedRuns(IPatternDetails pattern, ProcessorAssemblerRecipe recipe) {
        GenericStack[] outputs = pattern.getOutputs();
        if (outputs.length == 0) {
            return 0;
        }
        long runs = -1;
        boolean sawItem = false;
        boolean sawFluid = false;
        for (GenericStack output : outputs) {
            long perRun;
            if (output.what() instanceof AEItemKey item && recipe.hasItemOutput() && !sawItem
                    && item.matches(recipe.output())) {
                sawItem = true;
                perRun = recipe.output().getCount();
            } else if (output.what() instanceof AEFluidKey fluid && recipe.hasFluidOutput() && !sawFluid
                    && fluid.matches(recipe.fluidResult())) {
                sawFluid = true;
                perRun = recipe.fluidResult().getAmount();
            } else {
                return 0;
            }
            if (perRun <= 0 || output.amount() <= 0 || output.amount() % perRun != 0) {
                return 0;
            }
            long outputRuns = output.amount() / perRun;
            if (runs >= 0 && runs != outputRuns) {
                return 0;
            }
            runs = outputRuns;
        }
        boolean itemMatches = sawItem == recipe.hasItemOutput();
        boolean fluidMatches = sawFluid == recipe.hasFluidOutput();
        return itemMatches && fluidMatches && runs > 0 ? runs : 0;
    }

    /** Pattern-encoded item demand, merged by key from each slot's amount multiplied by its slot multiplier. */
    public static Map<AEItemKey, Long> encodedDemand(IPatternDetails pattern) {
        Map<AEItemKey, Long> demand = new HashMap<>();
        for (IPatternDetails.IInput input : pattern.getInputs()) {
            GenericStack[] possible = input.getPossibleInputs();
            if (possible.length == 0) {
                continue;
            }
            long amount = possible[0].amount();
            long multiplier = input.getMultiplier();
            if (amount <= 0 || multiplier <= 0 || amount > Long.MAX_VALUE / multiplier
                    || !(possible[0].what() instanceof AEItemKey key)) {
                continue;
            }
            demand.merge(key, amount * multiplier, Math::addExact);
        }
        return Map.copyOf(demand);
    }

    /**
     * Consumes exactly one pushed job. Any missing, extra, or non-item input rejects before mutation; once
     * validated, all demanded keys are removed. {@code false} leaves every input counter unchanged.
     */
    public static boolean consumeJobInputs(IPatternDetails pattern, KeyCounter[] inputs) {
        Map<AEItemKey, Long> demand = encodedDemand(pattern);
        if (demand.isEmpty()) {
            return false;
        }
        long totalDemand = 0;
        long totalAvailable = 0;
        try {
            for (var entry : demand.entrySet()) {
                long wanted = entry.getValue();
                if (wanted <= 0) {
                    return false;
                }
                long available = available(inputs, entry.getKey());
                if (available != wanted) {
                    return false;
                }
                totalDemand = Math.addExact(totalDemand, wanted);
                totalAvailable = Math.addExact(totalAvailable, available);
            }
            for (KeyCounter counter : inputs) {
                for (var entry : counter) {
                    if (!demand.containsKey(entry.getKey())) {
                        if (entry.getLongValue() < 0) {
                            return false;
                        }
                        totalAvailable = Math.addExact(totalAvailable, entry.getLongValue());
                    }
                }
            }
        } catch (ArithmeticException overflow) {
            return false;
        }
        if (totalDemand != totalAvailable) {
            return false;
        }
        for (var entry : demand.entrySet()) {
            take(inputs, entry.getKey(), entry.getValue());
        }
        return true;
    }

    private static long available(KeyCounter[] inputs, AEItemKey key) {
        long total = 0;
        for (KeyCounter counter : inputs) {
            try {
                total = Math.addExact(total, counter.get(key));
            } catch (ArithmeticException overflow) {
                return -1;
            }
        }
        return total;
    }

    private static void take(KeyCounter[] inputs, AEItemKey key, long amount) {
        long remaining = amount;
        for (KeyCounter counter : inputs) {
            long taken = Math.min(counter.get(key), remaining);
            if (taken <= 0) {
                continue;
            }
            counter.remove(key, taken);
            remaining -= taken;
            if (remaining == 0) {
                return;
            }
        }
        throw new IllegalStateException("Validated assembler input disappeared during consumption: " + key);
    }

    /** Fluid amount across the job's runs, or an empty stack for item-only recipes. */
    public FluidStack fluidOutput() {
        if (!recipe.hasFluidOutput()) {
            return FluidStack.EMPTY;
        }
        long amount = Math.multiplyExact(runs, recipe.fluidResult().getAmount());
        if (amount <= 0 || amount > Integer.MAX_VALUE) {
            return FluidStack.EMPTY;
        }
        FluidStack output = recipe.fluidResult();
        output.setAmount((int) amount);
        return output;
    }

    /** Item amount across the job's runs; the caller decides how to route amounts above one stack. */
    public ItemStack itemOutput() {
        if (!recipe.hasItemOutput()) {
            return ItemStack.EMPTY;
        }
        long amount = Math.multiplyExact(runs, recipe.output().getCount());
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack output = recipe.output();
        output.setCount((int) Math.min(output.getMaxStackSize(), amount));
        return output;
    }
}
