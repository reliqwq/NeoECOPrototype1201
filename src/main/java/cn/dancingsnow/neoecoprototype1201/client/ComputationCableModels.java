package cn.dancingsnow.neoecoprototype1201.client;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import net.minecraft.resources.ResourceLocation;

/**
 * The four computation cable models in one place. Only the plain pair is wired on this line: eco 20.5.3
 * keys its cable table by tier alone, with no host block in the key, so the glowing {@code _normal} pair
 * that the 1.21.1 line hands to eco's own drives cannot be told apart from ours and is left unused.
 */
public final class ComputationCableModels {
    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                Prototype1201.MOD_ID, "block/computation_cable/" + name);
    }

    /** Worn by the L1 frame, and registered as the model of both of our tiers. */
    public static final ResourceLocation LOWER_CONNECTED = id("cable_l1");
    public static final ResourceLocation LOWER_DISCONNECTED = id("cable_l1_dis");

    private ComputationCableModels() {
    }
}
