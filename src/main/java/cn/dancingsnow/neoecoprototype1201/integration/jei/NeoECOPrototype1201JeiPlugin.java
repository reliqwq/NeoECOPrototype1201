package cn.dancingsnow.neoecoprototype1201.integration.jei;

import cn.dancingsnow.neoecoae.integration.jei.NeoECOAEJeiPlugin;
import cn.dancingsnow.neoecoae.integration.xei.multiblock.MultiBlockInfoWrapper;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.Registration;
import cn.dancingsnow.neoecoprototype1201.multiblock.definition.SimplifyComputationDefinition;
import cn.dancingsnow.neoecoprototype1201.multiblock.definition.SimplifyCraftingDefinition;
import cn.dancingsnow.neoecoprototype1201.multiblock.definition.SimplifyStorageDefinition;
import cn.dancingsnow.neoecoprototype1201.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype1201.recipe.ProcessorAssemblerRecipes;
import cn.dancingsnow.neoecoprototype1201.registration.ModRegistration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Gives the L1 processor assembler a recipe page, a catalyst entry and an encode button. */
@JeiPlugin
public final class NeoECOPrototype1201JeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new ProcessorAssemblerCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    /**
     * What the machine can build: the shipped JSON recipes plus the ones derived from AE2's inscriber,
     * which is the whole set the assembler accepts, because that derivation is what its pattern gate also
     * answers with. The page asks for a world to read the recipe book from, so nothing is registered for
     * the loading screen.
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Our three L1 structures, into eco's own multiblock page - by category object rather than by name,
        // because asking JEI for the category by uid made this depend on plugin order and it threw when EMI
        // was installed (measured on the 1.21.1 line). They are not in eco's list to begin with: our
        // definitions deliberately take the builder overload that does not append to
        // NEMultiBlocks.DEFINITIONS, since joining it runs eco's class initialiser before its server config
        // loads and leaves its own definitions with a build range of 1 .. -4. Without this hand-over our
        // hosts simply have no page.
        registration.addRecipes(NeoECOAEJeiPlugin.MULTIBLOCK_TYPE, List.of(
                new MultiBlockInfoWrapper(SimplifyStorageDefinition.L1),
                new MultiBlockInfoWrapper(SimplifyComputationDefinition.L1),
                new MultiBlockInfoWrapper(SimplifyCraftingDefinition.L1)));
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<ProcessorAssemblerRecipe> declared =
                level.getRecipeManager().getAllRecipesFor(Registration.PROCESSOR_ASSEMBLER_TYPE.get());
        List<ProcessorAssemblerRecipe> recipes = new ArrayList<>(declared);
        recipes.addAll(ProcessorAssemblerRecipes.derived(level, declared));
        registration.addRecipes(ProcessorAssemblerCategory.TYPE, recipes);
        registration.addIngredientInfo(
                new ItemStack(ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get().asItem()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei." + Prototype1201.MOD_ID + ".processor_assembler.encode_hint"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Type first, and ItemLike... rather than the (ItemStack, RecipeType) order the 1.21.1 line calls:
        // JEI 15.59 does not have that overload.
        registration.addRecipeCatalysts(ProcessorAssemblerCategory.TYPE,
                ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get());
        registration.addRecipeCatalysts(NeoECOAEJeiPlugin.MULTIBLOCK_TYPE,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new ProcessorAssemblerTransferHandler(
                registration.getTransferHelper()), ProcessorAssemblerCategory.TYPE);
    }
}
