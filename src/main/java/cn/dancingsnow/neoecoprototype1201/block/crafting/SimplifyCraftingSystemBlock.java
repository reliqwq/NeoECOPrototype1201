package cn.dancingsnow.neoecoprototype1201.block.crafting;

import appeng.helpers.patternprovider.PatternContainer;
import cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider;
import cn.dancingsnow.neoecoae.api.IECOPatternStorageService;
import cn.dancingsnow.neoecoae.api.PatternStorageHost;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingSystem;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.block.MachineDiagnostics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * F1 crafting host. Publishes which of the two interface blocks the finished machine holds so the
 * blockstate file can give each case its own formed model. The calculator writes it; placement never does.
 */
public class SimplifyCraftingSystemBlock extends ECOCraftingSystem implements IHostPanelTextureProvider {
    /**
     * True when the interface cell holds {@code simplify_crafting_network_interface} -- the 通讯接口, the one
     * that opens eco's interface UI. Same rule as C1 and L1: the id with "network" in it is the panel the
     * player uses, and the plain one is the endpoint the one-click builder places.
     */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");

    /**
     * The easter egg: once per host, the machine rolls a one-in-sixteen chance to grow a face, and the
     * result is saved on the host so re-forming never re-rolls it. Only meaningful with
     * {@code formed=true}, which is where the blockstate hands over to the {@code *_face} models.
     */
    public static final BooleanProperty HAS_MIND = BooleanProperty.create("has_mind");

    /**
     * How content the mind currently is, 0..2, published by the host once a second and only when it crosses
     * a band. It rides the same deferred write as {@link #HAS_MIND} - the value is applied on the next server
     * tick, never from inside AE2's cluster recalculation - and only the {@code *_face} models read it, so a
     * machine without a face has exactly one appearance whatever this says.
     */
    public static final IntegerProperty MIND_TIER = IntegerProperty.create("mind_tier", 0, 2);

    public SimplifyCraftingSystemBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(COMMUNICATION_INTERFACE, false)
                .setValue(HAS_MIND, false)
                .setValue(MIND_TIER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMMUNICATION_INTERFACE, HAS_MIND, MIND_TIER);
    }

    /** The L1 badge, cut by eco at 13x18, and the host bracket, read as two 13x8 halves. */
    private static final ResourceLocation PANEL_TIER = panel("crafting_tier.png");
    private static final ResourceLocation PANEL_MARKER = panel("crafting_marker.png");
    private static final ResourceLocation MODE_ON = panel("crafting_mode_button_on.png");
    private static final ResourceLocation MODE_ON_HOVER = panel("crafting_mode_button_on_hover.png");

    @Nullable
    private static ResourceLocation panel(String file) {
        return ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "textures/gui/panel/" + file);
    }

    @Override
    @Nullable
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part) {
        return switch (part) {
            case CRAFTING_TIER -> PANEL_TIER;
            case CRAFTING_MARKER -> PANEL_MARKER;
            default -> null;
        };
    }

    /**
     * The two mode buttons' tiles. {@code index} says which: eco derives it from the JSON's {@code mode}
     * field, so 0 is active cooling and 1 is overclock. One green tile serves both, so the answer ignores it,
     * and the off state is left to eco because its off tile is the same pixels as the artwork sheet's.
     */
    @Override
    @Nullable
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part,
                                            int index, boolean on, boolean hover) {
        if (part != IHostPanelTextureProvider.Part.CRAFTING_MODE || !on) {
            return null;
        }
        return hover ? MODE_ON_HOVER : MODE_ON;
    }

    /**
     * Sneak + right-click dumps eco's external pattern index - the thing behind "正在建立索引 0%".
     *
     * <p>The progress a player sees is read out of {@code PatternCatalog}'s own counters, and every path
     * that moves them is private: {@code ensureExternalPatternIndex} starts a build when the index is dirty
     * and not already building, {@code onServerEndTick} steps it only while {@code externalPatternIndexBuilding}
     * is set, and the source list it steps over is built from {@code IGrid.getActiveMachines} minus every
     * {@code PatternStorageHost}. So a machine that hangs at zero can be in one of three different states
     * and the screen says the same sentence for all three. Reading the fields is the only way to tell them
     * apart, and nothing here writes them.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown() && MachineDiagnostics.askedByBuilder(player)
                && level instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(pos) instanceof ECOCraftingSystemBlockEntity host) {
            // Same rule as the other two diagnostics: the read walks upstream objects, and a throw here
            // would cost both the reading and the interaction. A failure prints a warning naming the block.
            String report;
            try {
                report = describeIndex(host);
            } catch (RuntimeException e) {
                LOGGER.warn("L1 pattern index diagnose at {} failed", pos, e);
                report = "diagnostic failed: " + e;
            }
            LOGGER.info("L1 pattern index diagnose at {} {}", pos, report);
            MachineDiagnostics.tellWhereTheReadingWent(player, state);
            return InteractionResult.CONSUME;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(Prototype1201.MOD_ID + "-pattern-index");

    private static String describeIndex(ECOCraftingSystemBlockEntity host) {
        var grid = host.getMainNode().getGrid();
        if (grid == null) {
            return "no-grid";
        }
        var catalog = grid.getService(IECOPatternStorageService.class);
        if (catalog == null) {
            return "no-pattern-catalog-service";
        }
        var sb = new StringBuilder();
        sb.append("catalog=").append(catalog.getClass().getName())
                .append(" building=").append(read(catalog, "externalPatternIndexBuilding"))
                .append(" dirty=").append(read(catalog, "externalPatternIndexDirty"))
                .append(" age=").append(read(catalog, "externalPatternIndexAge"))
                .append(" scanned=").append(read(catalog, "externalPatternScannedSlots"))
                .append(" total=").append(read(catalog, "externalPatternTotalSlots"))
                .append(" sourceIdx=").append(read(catalog, "externalPatternSourceIndex"))
                .append(" slotIdx=").append(read(catalog, "externalPatternSlotIndex"))
                .append(" available=").append(size(read(catalog, "externalAvailable")));
        var sources = read(catalog, "externalPatternSources");
        sb.append(" sources=").append(size(sources));
        if (sources instanceof java.util.List<?> list) {
            for (var source : list) {
                sb.append(" [").append(source.getClass().getName())
                        .append(" storageHost=").append(source instanceof PatternStorageHost)
                        .append(']');
            }
        }
        // What the pool would have been: every pattern container the grid reports, and how many of them
        // eco drops because they are pattern storages rather than sources.
        int containers = 0;
        int hosts = 0;
        for (Class<?> machineClass : grid.getMachineClasses()) {
            if (!PatternContainer.class.isAssignableFrom(machineClass)) {
                continue;
            }
            for (var machine : grid.getActiveMachines(machineClass.asSubclass(PatternContainer.class))) {
                containers++;
                if (machine instanceof PatternStorageHost) {
                    hosts++;
                }
            }
        }
        sb.append(" gridContainers=").append(containers).append(" ofWhichStorageHost=").append(hosts);
        return sb.toString();
    }

    private static Object size(Object collection) {
        return collection instanceof java.util.Collection<?> c ? c.size() : String.valueOf(collection);
    }

    /** Walks the class hierarchy because every field this report wants is private in eco's class. */
    private static Object read(Object target, String field) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                var declared = c.getDeclaredField(field);
                declared.setAccessible(true);
                return declared.get(target);
            } catch (NoSuchFieldException ignored) {
                // try the superclass
            } catch (IllegalAccessException e) {
                return "<" + e.getClass().getSimpleName() + ">";
            }
        }
        return "<absent>";
    }
}
