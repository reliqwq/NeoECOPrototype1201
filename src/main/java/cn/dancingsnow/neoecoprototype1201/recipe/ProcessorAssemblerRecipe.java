package cn.dancingsnow.neoecoprototype1201.recipe;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.Registration;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A processor assembly recipe: three or four ingredients, one unit each, one output. It exists because AE2's
 * molecular assembler only accepts patterns that implement
 * {@link net.minecraft.world.item.crafting.Recipe} through its own pattern classes, and a plain shaped
 * recipe cannot say "these four slots are interchangeable" the way an inscriber press can.
 *
 * <p>This is the 1.20.1 shape of the 1.21.1 record of the same name. The differences are all engine-level and
 * each is measured against the jars on this line: {@code Recipe} is parameterised by {@code Container}
 * because {@code RecipeInput} and {@code CraftingInput} do not exist before 1.20.2; {@code getId()} is
 * abstract here; the serializer reads Gson by hand because {@code RecipeSerializer} has no {@code codec()}
 * and {@code StreamCodec} is absent; and the 3..4 bound is a checked condition rather than
 * {@code Codec.list(codec, min, max)}, which that overload is not in the 1.20.1 datafixerupper.</p>
 */
public final class ProcessorAssemblerRecipe implements Recipe<Container> {
    /** Below three the recipe is an inscriber press with one leg missing; above four the grid has no room. */
    public static final int MIN_INPUTS = 3;
    public static final int MAX_INPUTS = 4;
    /** The assembler's crafting grid, which is what a pattern is filled into. */
    public static final int GRID_SLOTS = 9;

    private final ResourceLocation id;
    private final List<Ingredient> ingredients;
    private final ItemStack result;

    public ProcessorAssemblerRecipe(ResourceLocation id, List<Ingredient> ingredients, ItemStack result) {
        this.id = id;
        this.ingredients = List.copyOf(ingredients);
        this.result = result.copy();
    }

    public List<Ingredient> ingredients() {
        return ingredients;
    }

    public ItemStack output() {
        return result.copy();
    }

    /**
     * True when the grid content can be assigned to the ingredients one-to-one, in any order. A grid slot
     * is one unit; {@link #matchesUnits} is the shared implementation and the pattern path feeds it units
     * that a single slot may stand for.
     */
    @Override
    public boolean matches(Container grid, Level level) {
        List<ItemStack[]> units = new ArrayList<>();
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (!stack.isEmpty()) {
                units.add(new ItemStack[] { stack });
            }
        }
        return matchesUnits(units);
    }

    /**
     * True when the ingredients can take the pattern's units one-to-one, in any order.
     *
     * <p>A unit is one item the pattern demands, not one pattern slot: AE2 merges repeated ingredients
     * into a single slot carrying an amount, and one slot may offer alternative items. Matching on slot
     * count therefore rejected perfectly good patterns, which the assembler then silently refused.</p>
     */
    public boolean matchesUnits(List<ItemStack[]> units) {
        return units.size() == ingredients.size() && assign(0, units, new boolean[units.size()]);
    }

    /** A unit satisfies an ingredient when any of its alternative items does; which one is chosen later. */
    private boolean assign(int index, List<ItemStack[]> units, boolean[] taken) {
        if (index == ingredients.size()) {
            return true;
        }
        Ingredient ingredient = ingredients.get(index);
        for (int unit = 0; unit < units.size(); unit++) {
            if (taken[unit] || Arrays.stream(units.get(unit)).noneMatch(ingredient::test)) {
                continue;
            }
            taken[unit] = true;
            if (assign(index + 1, units, taken)) {
                return true;
            }
            taken[unit] = false;
        }
        return false;
    }

    @Override
    public ItemStack assemble(Container grid, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.PROCESSOR_ASSEMBLER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Registration.PROCESSOR_ASSEMBLER_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.createWithCapacity(ingredients.size());
        list.addAll(ingredients);
        return list;
    }

    @Override
    public String getGroup() {
        return Prototype1201.MOD_ID;
    }

    /** Reads the 1.21.1 document shape as it is written: {@code {"item": "...", "count": n}}. */
    public static final class Serializer implements RecipeSerializer<ProcessorAssemblerRecipe> {
        @Override
        public ProcessorAssemblerRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            JsonArray raw = GsonHelper.getAsJsonArray(json, "ingredients");
            if (raw.size() < MIN_INPUTS || raw.size() > MAX_INPUTS) {
                throw new JsonParseException("a processor assembly recipe takes " + MIN_INPUTS + " to "
                        + MAX_INPUTS + " ingredients, " + recipeId + " has " + raw.size());
            }
            List<Ingredient> parsed = new ArrayList<>();
            for (JsonElement element : raw) {
                Ingredient ingredient = Ingredient.fromJson(element);
                if (ingredient.isEmpty()) {
                    throw new JsonParseException("empty ingredient in " + recipeId);
                }
                parsed.add(ingredient);
            }
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new ProcessorAssemblerRecipe(recipeId, parsed, output);
        }

        @Override
        public ProcessorAssemblerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            List<Ingredient> parsed = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                parsed.add(Ingredient.fromNetwork(buffer));
            }
            return new ProcessorAssemblerRecipe(recipeId, parsed, buffer.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ProcessorAssemblerRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.result);
        }
    }
}
