package cn.dancingsnow.neoecoprototype1201.util;

import javax.annotation.Nullable;

import net.minecraft.world.level.Level;

/**
 * Grid work has to stop before the server tears its levels down; AE2 throws when a node is touched
 * afterwards. 1.21's eco exposes this on its block-entity base, so this line keeps it in one place.
 */
public final class SimplifyServerLifecycle {
    private SimplifyServerLifecycle() {
    }

    public static boolean isStopping(@Nullable Level level) {
        return level == null || level.getServer() == null || level.getServer().isShutdown();
    }
}
