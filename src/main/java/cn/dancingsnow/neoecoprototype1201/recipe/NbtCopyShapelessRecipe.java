package cn.dancingsnow.neoecoprototype1201.recipe;

import cn.dancingsnow.neoecoprototype1201.Registration;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

/** A shapeless recipe that carries one input's NBT into its result, as cell upgrades require. */
public final class NbtCopyShapelessRecipe implements CraftingRecipe {
    private final ShapelessRecipe delegate;
    private final int sourceIndex;

    public NbtCopyShapelessRecipe(ShapelessRecipe delegate, int sourceIndex) {
        this.delegate = delegate;
        if (sourceIndex < 0 || sourceIndex >= delegate.getIngredients().size()) {
            throw new IllegalArgumentException("NBT source is outside the ingredient list: " + sourceIndex);
        }
        this.sourceIndex = sourceIndex;
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return delegate.matches(grid, level);
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        Ingredient source = getIngredients().get(sourceIndex);
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (!source.test(stack)) {
                continue;
            }
            ItemStack output = delegate.assemble(grid, registryAccess);
            CompoundTag tag = stack.getTag();
            output.setTag(tag == null ? null : tag.copy());
            return output;
        }
        throw new IllegalStateException("Matched upgrade recipe has no NBT source in its grid");
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return delegate.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return delegate.getResultItem(registryAccess);
    }

    @Override
    public ResourceLocation getId() {
        return delegate.getId();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.NBT_COPY_SHAPELESS_SERIALIZER.get();
    }

    @Override
    public CraftingBookCategory category() {
        return delegate.category();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return delegate.getIngredients();
    }

    @Override
    public String getGroup() {
        return delegate.getGroup();
    }

    public int sourceIndex() {
        return sourceIndex;
    }

    public static final class Serializer implements RecipeSerializer<NbtCopyShapelessRecipe> {
        private final ShapelessRecipe.Serializer vanilla = new ShapelessRecipe.Serializer();

        @Override
        public NbtCopyShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapelessRecipe delegate = vanilla.fromJson(recipeId, json);
            int sourceIndex = GsonHelper.getAsInt(json, "copy_nbt_from", -1);
            if (sourceIndex < 0 || sourceIndex >= delegate.getIngredients().size()) {
                throw new JsonParseException("copy_nbt_from must name an ingredient in " + recipeId);
            }
            return new NbtCopyShapelessRecipe(delegate, sourceIndex);
        }

        @Override
        public NbtCopyShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new NbtCopyShapelessRecipe(vanilla.fromNetwork(recipeId, buffer), buffer.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, NbtCopyShapelessRecipe recipe) {
            vanilla.toNetwork(buffer, recipe.delegate);
            buffer.writeVarInt(recipe.sourceIndex());
        }
    }
}
