package cn.dancingsnow.neoecoprototype1201.integration.kubejs;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/**
 * KubeJS 插件入口。仅在 KubeJS 在场时由 KubeJS 自身通过
 * {@code kubejs.plugins.txt} 加载；本插件本体不引用任何其他模组类，而且这条线上
 * 没有任何我们自己的代码无条件引用它——eco 20.5.3 的 {@code NeoECOAEKubeJSPlugin}
 * 就是这个先例：{@code extends} 一个缺席的 KubeJS 父类，只要没人主动加载它，
 * 不装 KubeJS 的包照常启动（生产包 F:/1201prod 实测）。
 *
 * <p>形状差（对着 kubejs-forge-2001.6.5-build.26 量的，见 AGENTS §4.7）：2001.6.x 的
 * 插件是**类**（本类 {@code extends} 它），1.21.1 的 {@code plugin.KubeJSPlugin} 是接口；
 * builder 类型注册走 {@code RegistryInfo.of(...).addType(...)}（{@code init()} 里调，
 * 与 BuiltinKubeJSPlugin.init 的字节码同型）；schema 注册走
 * {@code registerRecipeSchemas(RegisterRecipeSchemasEvent)}。
 *
 * <p>注册三个物品 builder 类型，脚本即可创建存储矩阵与 eco 供能元件：
 *
 * <pre>{@code
 * StartupEvents.registry('item', event => {
 *   event.create('infinite_dirt', 'neoecoprototype1201:infinite_storage_matrix')
 *        .itemType('minecraft:dirt')
 * })
 * }</pre>
 */
public class KJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        var items = RegistryInfo.of(Registries.ITEM, Item.class);
        // Registered unconditionally: the two optional backends this builder can reach for live
        // behind their own classes (ChemicalMatrixSupport, OmniMatrixSupport), so naming the
        // builder no longer pulls Mekanism or Omni Cells into a pack that lacks them.
        items.addType("infinite_storage_matrix", InfiniteMatrixBuilder.class, InfiniteMatrixBuilder::new);
        items.addType("storage_matrix", StorageMatrixBuilder.class, StorageMatrixBuilder::new);
        items.addType("eco_supply_cell", EcoSupplyCellBuilder.class, EcoSupplyCellBuilder::new);
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        ProcessorAssemblerRecipeSchema.register(event);
    }
}
