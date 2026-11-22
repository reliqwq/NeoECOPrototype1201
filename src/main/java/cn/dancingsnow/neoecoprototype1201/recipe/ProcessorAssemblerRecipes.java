package cn.dancingsnow.neoecoprototype1201.recipe;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoprototype1201.config.NeoECOPrototypeServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds the processor assembly recipe an AE2 pattern asks for, and derives extra ones from AE2's inscriber
 * so pack content that adds press recipes needs no data file here. A press recipe consumes printed parts,
 * and each printed part stands for the material that was inscribed into it, so the printed part is replaced
 * by its own input.
 *
 * <p>The 1.20.1 shape of the 1.21.1 helper. Two differences, both measured: {@code RecipeManager
 * .getAllRecipesFor} answers a plain {@code List<Recipe>} on this line (there is no {@code RecipeHolder}
 * before 1.20.2), and the derivation is unconditional - this line has no config file at all yet, so there
 * is no switch to read, and the shipped default upstream is on.</p>
 */
public final class ProcessorAssemblerRecipes {
    private ProcessorAssemblerRecipes() {
    }

    /**
     * Derived additions that are not already covered by {@code existing}. The bundled JSON recipes were
     * authored from this same rule, so without this they would each be listed twice.
     */
    public static List<ProcessorAssemblerRecipe> derived(Level level, List<ProcessorAssemblerRecipe> existing) {
        List<InscriberRecipe> inscriber = level.getRecipeManager().getAllRecipesFor(AERecipeTypes.INSCRIBER);

        // printed part -> the material inscribed into it
        Map<Item, Ingredient> inscribedFrom = new HashMap<>();
        for (InscriberRecipe recipe : inscriber) {
            if (recipe.getProcessType() != InscriberProcessType.INSCRIBE) {
                continue;
            }
            ItemStack result = recipe.getResultItem();
            if (!result.isEmpty()) {
                inscribedFrom.put(result.getItem(), recipe.getMiddleInput());
            }
        }

        Set<String> covered = existing.stream().map(ProcessorAssemblerRecipes::signature)
                .collect(Collectors.toSet());
        List<ProcessorAssemblerRecipe> derived = new ArrayList<>();
        for (InscriberRecipe press : inscriber) {
            if (press.getProcessType() != InscriberProcessType.PRESS) {
                continue;
            }
            List<Ingredient> ingredients = List.of(
                    press.getMiddleInput(), press.getTopOptional(), press.getBottomOptional());
            // Press-making and dust recipes leave a slot empty; the assembler needs at least three.
            if (ingredients.stream().anyMatch(Ingredient::isEmpty)) {
                continue;
            }
            List<Ingredient> simplified = ingredients.stream()
                    .map(ingredient -> substitute(ingredient, inscribedFrom))
                    .collect(Collectors.toList());
            ProcessorAssemblerRecipe recipe = new ProcessorAssemblerRecipe(
                    press.getId(), simplified, press.getResultItem());
            if (!covered.contains(signature(recipe))) {
                derived.add(recipe);
            }
        }
        return derived;
    }

    /**
     * Finds the declared or derived recipe whose complete outputs and item inputs match this AE2 pattern.
     * Supported output shapes are item-only, fluid-only, or one item plus one fluid; all outputs must encode
     * the same positive integral run count. This lookup does not itself make a machine execute fluid jobs:
     * the machine completion path must deliver them before accepting such a pattern.
     */
    @Nullable
    public static ProcessorAssemblerRecipe resolve(Level level, IPatternDetails pattern, boolean includeDerived) {
        GenericStack[] outputs = pattern.getOutputs();
        if (outputs.length == 0 || outputs.length > 2) {
            return null;
        }
        for (GenericStack output : outputs) {
            if (output.amount() <= 0) {
                return null;
            }
        }
        List<SlotDemand> demands = slotDemands(pattern);
        if (demands == null) {
            return null;
        }
        List<ProcessorAssemblerRecipe> declared = level.getRecipeManager()
                .getAllRecipesFor(Registration.PROCESSOR_ASSEMBLER_TYPE.get());
        for (ProcessorAssemblerRecipe recipe : declared) {
            if (!isDisabled(recipe) && matchesPattern(recipe, demands, outputs)) {
                return recipe;
            }
        }
        if (!includeDerived || !NeoECOPrototypeServerConfig.DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER.get()) {
            return null;
        }
        for (ProcessorAssemblerRecipe recipe : derived(level, declared)) {
            if (!isDisabled(recipe) && matchesPattern(recipe, demands, outputs)) {
                return recipe;
            }
        }
        return null;
    }

    private static boolean isDisabled(ProcessorAssemblerRecipe recipe) {
        return recipe.hasItemOutput()
                && NeoECOPrototypeServerConfig.isProcessorRecipeDisabled(recipe.output().getItem());
    }

    /** Existing item-only callers remain source-compatible and cannot mistake fluid-only recipes for items. */
    @Nullable
    public static ProcessorAssemblerRecipe resolve(Level level, IPatternDetails pattern, Item output) {
        return resolve(level, pattern, output, true);
    }

    /** Family-aware compatibility overload for callers that already know the requested item output. */
    @Nullable
    public static ProcessorAssemblerRecipe resolve(Level level, IPatternDetails pattern, Item output,
                                                   boolean includeDerived) {
        ProcessorAssemblerRecipe recipe = resolve(level, pattern, includeDerived);
        return recipe != null && recipe.hasItemOutput() && recipe.output().is(output) ? recipe : null;
    }

    private static boolean matchesPattern(ProcessorAssemblerRecipe recipe, List<SlotDemand> demands,
                                          GenericStack[] outputs) {
        int expectedOutputs = (recipe.hasItemOutput() ? 1 : 0) + (recipe.hasFluidOutput() ? 1 : 0);
        if (outputs.length != expectedOutputs) {
            return false;
        }
        long runs = -1;
        boolean sawItem = false;
        boolean sawFluid = false;
        for (GenericStack output : outputs) {
            long perRun;
            boolean matches;
            if (output.what() instanceof AEItemKey item && recipe.hasItemOutput() && !sawItem) {
                sawItem = true;
                matches = item.matches(recipe.output());
                perRun = recipe.output().getCount();
            } else if (output.what() instanceof AEFluidKey fluid && recipe.hasFluidOutput() && !sawFluid) {
                sawFluid = true;
                matches = fluid.matches(recipe.fluidResult());
                perRun = recipe.fluidResult().getAmount();
            } else {
                return false;
            }
            if (!matches || perRun <= 0 || output.amount() <= 0 || output.amount() % perRun != 0) {
                return false;
            }
            long outputRuns = output.amount() / perRun;
            if (runs >= 0 && runs != outputRuns) {
                return false;
            }
            runs = outputRuns;
        }
        if (runs <= 0) {
            return false;
        }
        List<ItemStack[]> units = unitsFor(recipe, demands, runs);
        return units != null && recipe.matchesUnits(units);
    }

    /**
     * The recipe's own units: each slot's demand divided by the integral run count shared by every output.
     * AE2 stores repeated-ingredient counts and batch counts in the same slot multiplier; the output set
     * disambiguates them before this method is called.
     */
    @Nullable
    private static List<ItemStack[]> unitsFor(ProcessorAssemblerRecipe recipe, List<SlotDemand> demands,
                                              long runs) {
        if (runs <= 0) {
            return null;
        }
        List<ItemStack[]> units = new ArrayList<>();
        for (SlotDemand demand : demands) {
            if (demand.demand() % runs != 0) {
                return null;
            }
            long perRun = demand.demand() / runs;
            if (perRun <= 0 || perRun > ProcessorAssemblerRecipe.MAX_INPUTS) {
                return null;
            }
            for (long unit = 0; unit < perRun; unit++) {
                units.add(demand.alternatives());
            }
        }
        return units.size() > ProcessorAssemblerRecipe.MAX_INPUTS ? null : units;
    }

    /** What one pattern slot accepts, and how many it asks for before the batch factor is divided out. */
    private record SlotDemand(ItemStack[] alternatives, long demand) {
    }

    /**
     * One entry per pattern slot: its candidate items and its raw demand, which is the candidate amount
     * times the slot multiplier. All candidates of a slot must ask for the same number, or the slot has no
     * single demand to compare against the recipe.
     */
    @Nullable
    private static List<SlotDemand> slotDemands(IPatternDetails pattern) {
        List<SlotDemand> demands = new ArrayList<>();
        for (IPatternDetails.IInput slot : pattern.getInputs()) {
            GenericStack[] possible = slot.getPossibleInputs();
            if (possible.length == 0) {
                return null;
            }
            long amount = possible[0].amount();
            if (amount <= 0) {
                return null;
            }
            ItemStack[] alternatives = new ItemStack[possible.length];
            for (int candidate = 0; candidate < possible.length; candidate++) {
                if (!(possible[candidate].what() instanceof AEItemKey key)
                        || possible[candidate].amount() != amount) {
                    return null;
                }
                alternatives[candidate] = key.toStack();
            }
            demands.add(new SlotDemand(alternatives, amount * slot.getMultiplier()));
        }
        return demands;
    }

    /** Output item plus the sorted registry names of every ingredient choice. */
    private static String signature(ProcessorAssemblerRecipe recipe) {
        return recipe.output().getItem() + Arrays.toString(recipe.ingredients().stream()
                .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
                .map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .sorted()
                .toArray());
    }

    /** Only a single-item ingredient can be traced back; tags and multi-item choices are kept as-is. */
    private static Ingredient substitute(Ingredient ingredient, Map<Item, Ingredient> inscribedFrom) {
        ItemStack[] choices = ingredient.getItems();
        if (choices.length != 1 || choices[0].isEmpty()) {
            return ingredient;
        }
        return inscribedFrom.getOrDefault(choices[0].getItem(), ingredient);
    }
}
