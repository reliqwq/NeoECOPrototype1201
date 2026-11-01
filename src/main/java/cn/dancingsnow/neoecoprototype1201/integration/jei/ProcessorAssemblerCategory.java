package cn.dancingsnow.neoecoprototype1201.integration.jei;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/** Shows the combinations the L1 processor assembler accepts, AE2-derived ones included. */
public class ProcessorAssemblerCategory implements IRecipeCategory<ProcessorAssemblerRecipe> {
    public static final RecipeType<ProcessorAssemblerRecipe> TYPE =
            RecipeType.create(Prototype1201.MOD_ID, "processor_assembler", ProcessorAssemblerRecipe.class);

    private final IDrawable icon;

    public ProcessorAssemblerCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(
                ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get().asItem()));
    }

    @Override
    public RecipeType<ProcessorAssemblerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block." + Prototype1201.MOD_ID + ".simplify_stonecutting_assembler");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    /** JEI needs an explicit size whenever the category supplies no background. */
    @Override
    public int getWidth() {
        return 110;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ProcessorAssemblerRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.ingredients();
        for (int slot = 0; slot < ingredients.size(); slot++) {
            builder.addSlot(RecipeIngredientRole.INPUT, slot * 20, 1).addIngredients(ingredients.get(slot));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 1).addItemStack(recipe.output());
    }
}
