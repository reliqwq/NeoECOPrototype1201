package cn.dancingsnow.neoecoprototype1201.block.storage;

import cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider;
import cn.dancingsnow.neoecoae.blocks.storage.ECOStorageSystemBlock;
import cn.dancingsnow.neoecoae.blocks.entity.storage.ECOStorageSystemBlockEntity;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.multiblock.calculator.SimplifyStorageClusterCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * L1 storage subsystem host block: eco's storage host, so the right-click routing, the reach check, the
 * menu opening and the drops are eco's own. The only thing added here is the panel artwork - eco asks the
 * block behind the machine for the two painted cells of its host panel.
 */
public class SimplifyStorageControllerBlock extends ECOStorageSystemBlock implements IHostPanelTextureProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyStorageControllerBlock.class);
    /** The L1 badge, cut by eco at 13x18, and the host bracket, read as two 13x8 halves. */
    private static final ResourceLocation PANEL_TIER =
            new ResourceLocation(Prototype1201.MOD_ID, "textures/gui/panel/storage_tier.png");
    private static final ResourceLocation PANEL_MARKER =
            new ResourceLocation(Prototype1201.MOD_ID, "textures/gui/panel/storage_marker.png");

    public SimplifyStorageControllerBlock(Properties properties) {
        super(properties);
    }

    /**
     * Whether the matrix's interface slot holds a communication interface rather than a plain one. eco
     * 20.5.3 has no such concept anywhere - the string appears in none of its classes - so the property and
     * the model that reads it are ours, and the value is decided by our host's {@code updateState} override.
     * It joins the definition after eco's own properties, and {@code any()} takes the first value of each,
     * so an unformed host starts at false.
     */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMMUNICATION_INTERFACE);
    }

    /**
     * Sneak + right-click prints why the machine is not formed. eco's own click path only ever opens the
     * panel, and {@code SimplifyStorageClusterCalculator.diagnose} had no caller at all, so a shell that
     * refused to form gave nothing to read.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown() && level instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(pos) instanceof ECOStorageSystemBlockEntity host) {
            List<String> issues = SimplifyStorageClusterCalculator.diagnose(serverLevel, host);
            // The geometry half was already readable; this is the half that answers 'it formed but
            // won't take items': which drives the cluster actually collected, what each
            // mounted cell reports for bytes and types, and what eco's own storage fault log says.
            var cluster = host.getCluster();
            var drives = cluster == null ? java.util.List.<cn.dancingsnow.neoecoae.blocks.entity.storage
                    .ECODriveBlockEntity>of() : cluster.getDrives();
            var cells = new StringBuilder();
            for (var drive : drives) {
                var mounted = drive.getCellStack();
                cells.append(' ').append(drive.getBlockPos().toShortString())
                        .append(" isMounted=").append(drive.isMounted())
                        .append(" cellStack=").append(mounted == null ? "null"
                                : mounted.getCount() + "x " + mounted.getItem());
                var inventory = drive.getCellInventory();
                if (inventory == null) {
                    cells.append(" inventory=null");
                } else {
                    cells.append(" invBytes=").append(inventory.getUsedBytes()).append('/')
                            .append(inventory.getTotalBytes())
                            .append(" invTypes=").append(inventory.getStoredItemTypes()).append('/')
                            .append(inventory.getTotalItemTypes())
                            .append(" invTier=").append(inventory.getTier());
                }
                // The item asked directly. This is what separates 'the cell itself reports 0' from
                // 'nobody ever built an inventory for the cell'.
                var item = mounted == null ? net.minecraft.world.item.ItemStack.EMPTY : mounted.getItem();
                if (item instanceof cn.dancingsnow.neoecoae.api.storage.IBasicECOCellItem cellItem) {
                    cells.append(" itemBytes=").append(cellItem.getBytes())
                            .append(" itemTypes=").append(cellItem.getTotalTypes())
                            .append(" itemBytesPerType=").append(cellItem.getBytesPerType())
                            .append(" itemClass=").append(item.getClass().getName());
                } else {
                    cells.append(" not-an-eco-cell-item=").append(item.getClass().getName());
                }
                // And what is physically sitting in the drive's slots.
                var handler = drive.HANDLER;
                cells.append(" slots=").append(handler.getSlots());
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    var stack = handler.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        cells.append(" [").append(slot).append("]=").append(stack.getCount())
                                .append('x').append(stack.getItem());
                    }
                }
            }
            LOGGER.info("L1 storage diagnose at {} formed={} issues={} cluster={} drives={} cells={}"
                    + " faults={} diagnostic={}",
                    pos, host.isFormed(), issues, cluster, drives.size(), cells,
                    host.storageFailures(), host.storageDiagnosticText());
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("L1 存储矩阵诊断 formed=" + host.isFormed()
                        + " 问题数=" + issues.size() + " 驱动器=" + drives.size()
                        + " 故障=" + host.storageFailures().size()), false);
                for (String issue : issues) {
                    serverPlayer.displayClientMessage(Component.literal("  - " + issue), false);
                }
                serverPlayer.displayClientMessage(Component.literal("  驱动与细胞:" + cells), false);
                host.storageFailures().forEach(fault -> serverPlayer.displayClientMessage(
                        Component.literal("  故障 " + fault), false));
            }
            return InteractionResult.CONSUME;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    @Nullable
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part) {
        return switch (part) {
            case STORAGE_TIER -> PANEL_TIER;
            case STORAGE_MARKER -> PANEL_MARKER;
            default -> null;
        };
    }
}
