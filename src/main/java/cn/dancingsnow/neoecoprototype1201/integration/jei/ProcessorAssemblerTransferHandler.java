package cn.dancingsnow.neoecoprototype1201.integration.jei;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.recipe.ProcessorAssemblerRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JEI's transfer button encodes one of our assembler recipes into a processing pattern.
 *
 * <p>Why it has to be ours: ae2jeiintegration registers a <em>universal</em> handler for the encoding
 * terminal that collects inputs into a set keyed by item, so two identical ingredients collapse into one
 * slot -- our three-unit recipes would arrive as a two-unit pattern, which the assembler rightly refuses.
 * JEI prefers a category-specific handler over a universal one, so registering this against
 * {@link ProcessorAssemblerCategory#TYPE} wins, and every ingredient stays its own slot, which is the shape
 * {@code ProcessorAssemblerRecipes.resolve} expects.</p>
 */
public final class ProcessorAssemblerTransferHandler
        implements IRecipeTransferHandler<PatternEncodingTermMenu, ProcessorAssemblerRecipe> {
    private final IRecipeTransferHandlerHelper transferHelper;

    public ProcessorAssemblerTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<? extends PatternEncodingTermMenu> getContainerClass() {
        return PatternEncodingTermMenu.class;
    }

    @Override
    public Optional<MenuType<PatternEncodingTermMenu>> getMenuType() {
        return Optional.of(PatternEncodingTermMenu.TYPE);
    }

    @Override
    public RecipeType<ProcessorAssemblerRecipe> getRecipeType() {
        return ProcessorAssemblerCategory.TYPE;
    }

    /**
     * One entry per ingredient, each offering that ingredient's own items. The count is always one because a
     * vanilla {@link Ingredient} carries no amount -- repeating an ingredient is what asks for two of it.
     */
    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(PatternEncodingTermMenu menu, ProcessorAssemblerRecipe recipe,
                                               IRecipeSlotsView recipeSlotsView, Player player,
                                               boolean simulate, boolean handleErrors) {
        List<List<GenericStack>> inputs = new ArrayList<>(recipe.ingredients().size());
        for (Ingredient ingredient : recipe.ingredients()) {
            ItemStack[] choices = ingredient.getItems();
            if (choices.length == 0) {
                // An empty tag: nothing could fill this slot, so say so instead of encoding a pattern the
                // assembler would only refuse later.
                return transferHelper.createUserErrorWithTooltip(Component.translatable(
                        "jei." + Prototype1201.MOD_ID + ".processor_assembler.no_match"));
            }
            List<GenericStack> slot = new ArrayList<>(choices.length);
            for (ItemStack choice : choices) {
                slot.add(new GenericStack(AEItemKey.of(choice), 1));
            }
            inputs.add(slot);
        }
        if (!simulate) {
            EncodingHelper.encodeProcessingRecipe(menu, inputs,
                    List.of(new GenericStack(AEItemKey.of(recipe.output()), recipe.output().getCount())));
        }
        return null;
    }
}
