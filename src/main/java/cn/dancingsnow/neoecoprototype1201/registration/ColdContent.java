package cn.dancingsnow.neoecoprototype1201.registration;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.block.CryotheumIceCreamSnowBlock;
import cn.dancingsnow.neoecoprototype1201.block.SimplifyCryotheumOreBlock;
import cn.dancingsnow.neoecoprototype1201.items.CryotheumIceCreamBucketItem;
import cn.dancingsnow.neoecoprototype1201.items.CryotheumIceCreamItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

/** Bounded cold-content port: no worldgen, dissociator or new recipe serializer. */
public final class ColdContent {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, Prototype1201.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Prototype1201.MOD_ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Prototype1201.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, Prototype1201.MOD_ID);

    public static final RegistryObject<SimplifyCryotheumOreBlock> CRYOTHEUM_ORE =
            BLOCKS.register("cryotheum_ore", () -> new SimplifyCryotheumOreBlock(ore(MapColor.STONE)));
    public static final RegistryObject<SimplifyCryotheumOreBlock> NETHER_CRYOTHEUM_ORE =
            BLOCKS.register("nether_cryotheum_ore", () -> new SimplifyCryotheumOreBlock(ore(MapColor.COLOR_RED)));
    public static final RegistryObject<SimplifyCryotheumOreBlock> END_CRYOTHEUM_ORE =
            BLOCKS.register("end_cryotheum_ore", () -> new SimplifyCryotheumOreBlock(ore(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<BlockItem> CRYOTHEUM_ORE_ITEM = ITEMS.register("cryotheum_ore",
            () -> new BlockItem(CRYOTHEUM_ORE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> NETHER_CRYOTHEUM_ORE_ITEM = ITEMS.register("nether_cryotheum_ore",
            () -> new BlockItem(NETHER_CRYOTHEUM_ORE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> END_CRYOTHEUM_ORE_ITEM = ITEMS.register("end_cryotheum_ore",
            () -> new BlockItem(END_CRYOTHEUM_ORE.get(), new Item.Properties()));

    public static final RegistryObject<CryotheumIceCreamItem> CRYOTHEUM_ICE_CREAM =
            ITEMS.register("cryotheum_ice_cream", () -> new CryotheumIceCreamItem(new Item.Properties()
                    .stacksTo(64).craftRemainder(Items.GLASS_BOTTLE)
                    .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6F).alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4800), 1.0F)
                            .build())));

    public static final RegistryObject<FluidType> DISSOLUTION_SOLUTION_TYPE =
            FLUID_TYPES.register("dissolution_solution", () -> new FluidType(FluidType.Properties.create()
                    .supportsBoating(true)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return id("block/dissolution_solution_still");
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return id("block/dissolution_solution_flow");
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> DISSOLUTION_SOLUTION = FLUIDS.register("dissolution_solution",
            () -> new ForgeFlowingFluid.Source(dissolutionProps()));
    public static final RegistryObject<FlowingFluid> FLOWING_DISSOLUTION_SOLUTION =
            FLUIDS.register("flowing_dissolution_solution", () -> new ForgeFlowingFluid.Flowing(dissolutionProps()));
    public static final RegistryObject<LiquidBlock> DISSOLUTION_SOLUTION_BLOCK =
            BLOCKS.register("dissolution_solution", () -> new LiquidBlock(DISSOLUTION_SOLUTION,
                    liquid()));
    public static final RegistryObject<BucketItem> DISSOLUTION_BUCKET = ITEMS.register("dissolution_bucket",
            () -> new BucketItem(DISSOLUTION_SOLUTION, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<FluidType> CRYOTHEUM_ICE_CREAM_TYPE =
            FLUID_TYPES.register("cryotheum_ice_cream", () -> new FluidType(FluidType.Properties.create()
                    .supportsBoating(true).density(1100).viscosity(1600)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return id("block/cryotheum_ice_cream_block");
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return id("block/cryotheum_ice_cream_block");
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> CRYOTHEUM_ICE_CREAM_FLUID =
            FLUIDS.register("cryotheum_ice_cream", () -> new ForgeFlowingFluid.Source(iceCreamProps()));
    public static final RegistryObject<FlowingFluid> FLOWING_CRYOTHEUM_ICE_CREAM =
            FLUIDS.register("flowing_cryotheum_ice_cream", () -> new ForgeFlowingFluid.Flowing(iceCreamProps()));
    public static final RegistryObject<LiquidBlock> CRYOTHEUM_ICE_CREAM_FLUID_BLOCK =
            BLOCKS.register("cryotheum_ice_cream", () -> new LiquidBlock(CRYOTHEUM_ICE_CREAM_FLUID,
                    liquid().sound(SoundType.HONEY_BLOCK)));
    public static final RegistryObject<CryotheumIceCreamSnowBlock> CRYOTHEUM_ICE_CREAM_BLOCK =
            BLOCKS.register("cryotheum_ice_cream_block", () -> new CryotheumIceCreamSnowBlock(
                    BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion()
                            .mapColor(MapColor.COLOR_CYAN).strength(0.125F).sound(SoundType.POWDER_SNOW)));
    public static final RegistryObject<BlockItem> CRYOTHEUM_ICE_CREAM_BLOCK_ITEM =
            ITEMS.register("cryotheum_ice_cream_block",
                    () -> new BlockItem(CRYOTHEUM_ICE_CREAM_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<CryotheumIceCreamBucketItem> CRYOTHEUM_ICE_CREAM_BUCKET =
            ITEMS.register("cryotheum_ice_cream_bucket", () -> new CryotheumIceCreamBucketItem(
                    CRYOTHEUM_ICE_CREAM_BLOCK, CRYOTHEUM_ICE_CREAM_FLUID,
                    new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    /** Container-only coolant: vanilla owns the bucket and placed powder snow; no LiquidBlock. */
    public static final RegistryObject<FluidType> POWDER_SNOW_FLUID_TYPE =
            FLUID_TYPES.register("powder_snow", () -> new FluidType(FluidType.Properties.create()) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return ResourceLocation.fromNamespaceAndPath("minecraft", "block/powder_snow");
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return ResourceLocation.fromNamespaceAndPath("minecraft", "block/powder_snow");
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> POWDER_SNOW_FLUID =
            FLUIDS.register("powder_snow", () -> new ForgeFlowingFluid.Source(powderSnowProps()));
    public static final RegistryObject<FlowingFluid> FLOWING_POWDER_SNOW =
            FLUIDS.register("flowing_powder_snow", () -> new ForgeFlowingFluid.Flowing(powderSnowProps()));

    private static ForgeFlowingFluid.Properties powderSnowProps() {
        return new ForgeFlowingFluid.Properties(POWDER_SNOW_FLUID_TYPE,
                POWDER_SNOW_FLUID, FLOWING_POWDER_SNOW)
                .bucket(() -> Items.POWDER_SNOW_BUCKET);
    }

    private static BlockBehaviour.Properties ore(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASEDRUM)
                .strength(3.0F, 3.0F).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties liquid() {
        return BlockBehaviour.Properties.of().noCollission().replaceable().strength(100.0F).noLootTable();
    }

    private static ForgeFlowingFluid.Properties dissolutionProps() {
        return new ForgeFlowingFluid.Properties(DISSOLUTION_SOLUTION_TYPE,
                DISSOLUTION_SOLUTION, FLOWING_DISSOLUTION_SOLUTION)
                .block(DISSOLUTION_SOLUTION_BLOCK).bucket(DISSOLUTION_BUCKET);
    }

    private static ForgeFlowingFluid.Properties iceCreamProps() {
        return new ForgeFlowingFluid.Properties(CRYOTHEUM_ICE_CREAM_TYPE,
                CRYOTHEUM_ICE_CREAM_FLUID, FLOWING_CRYOTHEUM_ICE_CREAM)
                .block(CRYOTHEUM_ICE_CREAM_FLUID_BLOCK).bucket(CRYOTHEUM_ICE_CREAM_BUCKET);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, path);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
    }

    private ColdContent() {
    }
}
