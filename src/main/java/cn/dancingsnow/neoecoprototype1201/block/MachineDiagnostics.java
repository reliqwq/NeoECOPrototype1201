package cn.dancingsnow.neoecoprototype1201.block;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The two rules the three L1 hosts' sneak + right-click reading lives under.
 *
 * <p>It stays: this line has no {@code @GameTest} source at all, so formed state, drive mounting, the CPU
 * list and the pattern-index counters have no suite face - the reading is the only instrument that turns
 * them into numbers, and both the CPU count and the twelve packet crashes were found with it.</p>
 *
 * <p>It answers builders only, and it does not speak in chat. The dump is three or four lines of
 * untranslated key names ({@code total= avail= status=}); in a Chinese pack that is debug text pasted into
 * the player's chat, and a survival player sneaking against a machine expects the machine. So the whole
 * reading goes to the log, and the screen gets one action-bar sentence saying where it went.</p>
 */
public final class MachineDiagnostics {
    /**
     * Whether this player asked as a builder. Permission level 2 is on the list because the machine worth
     * reading is usually on a server where the reader is OP but not in creative.
     */
    public static boolean askedByBuilder(Player player) {
        return player.isCreative() || player.hasPermissions(2);
    }

    /** One action-bar line, untranslated-free: the text comes from lang, in both shipped languages. */
    public static void tellWhereTheReadingWent(Player player, BlockState state) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(
                    "diagnose." + Prototype1201.MOD_ID + ".log_written", state.getBlock().getName()), true);
        }
    }

    private MachineDiagnostics() {
    }
}
