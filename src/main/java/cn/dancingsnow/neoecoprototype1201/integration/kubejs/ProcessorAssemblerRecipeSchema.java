package cn.dancingsnow.neoecoprototype1201.integration.kubejs;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import net.minecraft.resources.ResourceLocation;

/**
 * Teaches KubeJS which JSON fields of {@code neoecoprototype1201:processor_assembler} are inputs and
 * which is the output, so scripts can match and remove recipes by field. Deliberately declares no
 * constructor: KubeJS cannot coerce a plain JS array into the recipe's ingredient list, so scripts
 * add recipes with {@code event.custom({ ... })} instead of a builder call.
 *
 * <p>Ported from the 1.21.1 line. The component surface is named differently on KubeJS 6 (this
 * line's generation): {@code ItemComponents.INPUT_ARRAY/OUTPUT} wrap {@code InputItem/OutputItem}
 * rather than 1.21.1's raw {@code Ingredient/ItemStack} keys, and the schema marks the output id
 * with {@code uniqueOutputId} - the {@code uniqueId(RecipeKey)} overload does not exist here.
 */
public final class ProcessorAssemblerRecipeSchema {
    private static final RecipeKey<InputItem[]> INGREDIENTS =
            ItemComponents.INPUT_ARRAY.key("ingredients");
    private static final RecipeKey<OutputItem> RESULT =
            ItemComponents.OUTPUT.key("result");

    public static final RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULT)
            .uniqueOutputId(RESULT);

    private ProcessorAssemblerRecipeSchema() {
    }

    public static void register(RegisterRecipeSchemasEvent event) {
        event.namespace(Prototype1201.MOD_ID).register("processor_assembler", SCHEMA);
    }
}
