package cn.dancingsnow.neoecoprototype1201.client;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import net.minecraft.resources.ResourceLocation;

/**
 * The four computation cable models, the same two pairs the 1.21.1 line ships.
 *
 * <p>eco 20.5.3 keys its cable table by tier alone and has no host block in the lookup, so the pair a
 * cell gets cannot be chosen by the frame it sits in from inside that table. The split is made one level
 * up instead: eco's renderer only ever draws eco's drives, and ours only ever draws ours, so the table
 * carries the glowing pair - which is what a cell in an eco host should show - while
 * {@code SimplifyComputationDriveRenderer} reads the plain pair straight from here.
 */
public final class ComputationCableModels {
    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                Prototype1201.MOD_ID, "block/computation_cable/" + name);
    }

    /** Worn by our own L1 frame; hard-coded in our renderer rather than looked up. */
    public static final ResourceLocation LOWER_CONNECTED = id("cable_l1");
    public static final ResourceLocation LOWER_DISCONNECTED = id("cable_l1_dis");
    /** Handed to eco's table, so a cell of ours in one of eco's hosts glows like eco's own. */
    public static final ResourceLocation UPPER_CONNECTED = id("cable_l1r");
    public static final ResourceLocation UPPER_DISCONNECTED = id("cable_l1r_dis");

    private ComputationCableModels() {
    }
}
