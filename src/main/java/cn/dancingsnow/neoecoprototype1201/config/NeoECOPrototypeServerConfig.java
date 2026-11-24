package cn.dancingsnow.neoecoprototype1201.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/** Server settings for production, machine tiers and self-growing cells. */
public final class NeoECOPrototypeServerConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue CRYOTHEUM_ORE_FREEZE_TICKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CRYOTHEUM_ORE_DIMENSIONS;
    public static final ForgeConfigSpec.BooleanValue CRYOTHEUM_METEORITE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DISABLED_PROCESSOR_RECIPES;
    public static final ForgeConfigSpec.IntValue PROCESSOR_ASSEMBLER_MAX_RUNS_PER_CYCLE;
    public static final int PROCESSOR_ASSEMBLER_MAX_RUNS_PER_CYCLE_DEFAULT = 64;
    public static final int PROCESSOR_ASSEMBLER_MAX_RUNS_PER_CYCLE_LIMIT = 192;
    public static final boolean MIGRATION_DRIVE_REFUND_BLOCKS_DEFAULT = false;
    public static final ForgeConfigSpec.BooleanValue MIGRATION_DRIVE_REFUND_BLOCKS;
    public static final ForgeConfigSpec.LongValue SINGULARITY_CELL_TICKS_PER_BATCH;
    public static final ForgeConfigSpec.LongValue SINGULARITY_CELL_AMOUNT_PER_BATCH;
    public static final ForgeConfigSpec.LongValue SKY_STONE_DUST_CELL_TICKS_PER_BATCH;
    public static final ForgeConfigSpec.LongValue SKY_STONE_DUST_CELL_AMOUNT_PER_BATCH;
    public static final ForgeConfigSpec.LongValue SOURCE_CELL_TICKS_PER_BATCH;
    public static final ForgeConfigSpec.LongValue SOURCE_CELL_AMOUNT_PER_BATCH;
    public static final long CELL_TICKS_PER_BATCH_MIN = 60L;
    public static final long CELL_TICKS_PER_BATCH_MAX = 1L << 24;
    public static final long CELL_AMOUNT_PER_BATCH_MAX = Long.MAX_VALUE;

    static {
        var builder = new ForgeConfigSpec.Builder();
        builder.push("cryotheum_ore");
        CRYOTHEUM_ORE_DIMENSIONS = builder
                .comment("Reserved for the later worldgen batch; currently does not generate ores.",
                        "Dimensions by full ID. Default: [] (all off).",
                        "为后续世界生成移植预留；当前不会生成矿石。默认 []（全部关闭）。")
                .defineListAllowEmpty(List.of("dimensions"), List::of,
                        value -> value instanceof String id && ResourceLocation.tryParse(id) != null);
        CRYOTHEUM_ORE_FREEZE_TICKS = builder
                .comment("Freeze ticks per broken cryotheum ore. Default: 60; range: 0..600; 0 disables it.",
                        "每挖掉一块天外寒冰矿获得的冰冻刻数。默认 60，范围 0..600，0 为关闭。")
                .defineInRange("freeze_ticks", 60, 0, 600);
        builder.pop();
        builder.push("cryotheum_meteorite");
        CRYOTHEUM_METEORITE_ENABLED = builder
                .comment("Reserved for the later worldgen batch; currently has no effect. Default: false.",
                        "为后续世界生成移植预留；当前无效果。默认 false。")
                .define("enabled", false);
        builder.pop();
        builder.push("processor_assembler");
        DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER = builder
                .comment("Whether EC9 accepts recipes derived from AE2 inscriber recipes. Default: true.",
                        "Changes apply after /reload.",
                        "EC9 是否接受从 AE2 压印器配方推导的配方。默认 true。",
                        "改动后执行 /reload 生效。")
                .define("derive_processor_recipes_from_inscriber", true);
        DISABLED_PROCESSOR_RECIPES = builder
                .comment("Processor output IDs disabled in both declared and inscriber-derived recipes. Default: [].",
                        "Example: [\"ae2:logic_processor\"]. Changes apply after /reload.",
                        "同时禁用声明配方与压印器推导配方的产物 ID。默认 []。",
                        "示例：[\"ae2:logic_processor\"]。改动后执行 /reload 生效。")
                .defineListAllowEmpty("disabled_processor_recipes", List::of,
                        value -> value instanceof String id && ResourceLocation.tryParse(id) != null);
        PROCESSOR_ASSEMBLER_MAX_RUNS_PER_CYCLE = builder
                .comment("Maximum runs per EC9 cycle. Default: 64; range: 1..192.",
                        "The basic L1 assembler always runs exactly one recipe unit.",
                        "EC9 单周期最大配方份数。默认 64，范围 1..192。",
                        "基础 L1 装配室每次固定执行一份。")
                .defineInRange("processor_assembler_max_runs_per_cycle",
                        PROCESSOR_ASSEMBLER_MAX_RUNS_PER_CYCLE_DEFAULT, 1,
                        PROCESSOR_ASSEMBLER_MAX_RUNS_PER_CYCLE_LIMIT);
        builder.pop();
        builder.push("singularity_cell");
        SINGULARITY_CELL_TICKS_PER_BATCH = builder
                .comment("Ticks per batch. Default: 600 | Range: 60~16777216.",
                        "奇点单元产出间隔（刻）。默认 600 | 范围 60~16777216。")
                .defineInRange("ticks_per_batch", 600L, CELL_TICKS_PER_BATCH_MIN, CELL_TICKS_PER_BATCH_MAX);
        SINGULARITY_CELL_AMOUNT_PER_BATCH = builder
                .comment("Singularities per batch. Default: 120 | Range: 1~Long.MAX.",
                        "每批产出的奇点数量。默认 120 | 范围 1~Long.MAX。")
                .defineInRange("amount_per_batch", 120L, 1L, CELL_AMOUNT_PER_BATCH_MAX);
        builder.pop();
        builder.push("sky_stone_dust_cell");
        SKY_STONE_DUST_CELL_TICKS_PER_BATCH = builder
                .comment("Ticks per batch for the meteorite-dust cell. Default: 600 | Range: 60~16777216.",
                        "Note: independent from the singularity cell.",
                        "陨石粉单元产出间隔（刻）。默认 600 | 范围 60~16777216。",
                        "注：与奇点单元配置相互独立。")
                .defineInRange("ticks_per_batch", 600L, CELL_TICKS_PER_BATCH_MIN, CELL_TICKS_PER_BATCH_MAX);
        SKY_STONE_DUST_CELL_AMOUNT_PER_BATCH = builder
                .comment("Dust per batch. Default: 120 | Range: 1~Long.MAX.",
                        "每批产出的陨石粉数量。默认 120 | 范围 1~Long.MAX。")
                .defineInRange("amount_per_batch", 120L, 1L, CELL_AMOUNT_PER_BATCH_MAX);
        builder.pop();
        builder.push("source_cell");
        SOURCE_CELL_TICKS_PER_BATCH = builder
                .comment("Ticks per batch for the source cell. Default: 600 | Range: 60~16777216.",
                        "Note: independent from the singularity and meteorite-dust cells.",
                        "魔源单元产出间隔（刻）。默认 600 | 范围 60~16777216。",
                        "注：与奇点单元、陨石粉单元的配置相互独立。")
                .defineInRange("ticks_per_batch", 600L, CELL_TICKS_PER_BATCH_MIN, CELL_TICKS_PER_BATCH_MAX);
        SOURCE_CELL_AMOUNT_PER_BATCH = builder
                .comment("Source per batch. Default: 120 | Range: 1~Long.MAX.",
                        "每批产出的魔源数量。默认 120 | 范围 1~Long.MAX。")
                .defineInRange("amount_per_batch", 120L, 1L, CELL_AMOUNT_PER_BATCH_MAX);
        builder.pop();
        builder.push("migration_drive");
        MIGRATION_DRIVE_REFUND_BLOCKS = builder
                .comment("Whether a drive returns the blocks it replaces. Default: false.",
                        "Note: a mounted cell and stored patterns are preserved regardless of this switch.",
                        "升格迁移驱动是否返还换下的旧方块。默认 false。",
                        "注：机器内部的存储矩阵与样板无论开关均会保留。")
                .define("refund_replaced_blocks", MIGRATION_DRIVE_REFUND_BLOCKS_DEFAULT);
        builder.pop();
        SPEC = builder.build();
    }

    private NeoECOPrototypeServerConfig() {
    }

    public static <T> T current(ForgeConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public static long singularityCellTicksPerBatch() {
        return Math.max(CELL_TICKS_PER_BATCH_MIN, current(SINGULARITY_CELL_TICKS_PER_BATCH));
    }

    public static long singularityCellAmountPerBatch() {
        return Math.max(1L, Math.min(CELL_AMOUNT_PER_BATCH_MAX, current(SINGULARITY_CELL_AMOUNT_PER_BATCH)));
    }

    public static long skyStoneDustCellTicksPerBatch() {
        return Math.max(CELL_TICKS_PER_BATCH_MIN, current(SKY_STONE_DUST_CELL_TICKS_PER_BATCH));
    }

    public static long skyStoneDustCellAmountPerBatch() {
        return Math.max(1L, Math.min(CELL_AMOUNT_PER_BATCH_MAX, current(SKY_STONE_DUST_CELL_AMOUNT_PER_BATCH)));
    }

    public static long sourceCellTicksPerBatch() {
        return Math.max(CELL_TICKS_PER_BATCH_MIN, current(SOURCE_CELL_TICKS_PER_BATCH));
    }

    public static long sourceCellAmountPerBatch() {
        return Math.max(1L, Math.min(CELL_AMOUNT_PER_BATCH_MAX, current(SOURCE_CELL_AMOUNT_PER_BATCH)));
    }

    public static int processorAssemblerMaxRunsPerCycle() {
        return PROCESSOR_ASSEMBLER_MAX_RUNS_PER_CYCLE.get();
    }

    public static boolean isProcessorRecipeDisabled(net.minecraft.world.item.Item output) {
        ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(output);
        return id != null && DISABLED_PROCESSOR_RECIPES.get().contains(id.toString());
    }
}
