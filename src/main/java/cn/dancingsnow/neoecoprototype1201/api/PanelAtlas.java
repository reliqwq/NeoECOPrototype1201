package cn.dancingsnow.neoecoprototype1201.api;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * The six panel sheets eco resolves per tier, and the L1 copy we ask it to draw instead.
 *
 * <p>eco hands the name, not the path: {@code NETextures} registers each sheet under a constant string and
 * every sprite on it is cut through {@code PanelAtlasSprite}, which asks {@code IECOTier#getPanelAtlas}
 * first and keeps its own sheet when that answers null. The six names below were read off that class on
 * 20.5.3, and all six of eco's default sheets are byte-identical between 20.5.3 and 21.2.13 - which is why
 * the same art serves both lines.
 */
public enum PanelAtlas {
    COMPUTATION_BACKGROUND("Computation-BACKGROUND_ATLAS", "eco_cpu_controller"),
    COMPUTATION_WIDGETS("Computation-WIDGETS_ATLAS", "cpu_widget"),
    CRAFTING_CONTROLLER("Crafting-CONTROLLER_ATLAS", "crafting_controller"),
    CRAFTING_WIDGETS("Crafting-WIDGETS_ATLAS", "crafting_widgets"),
    STORAGE_CONTROLLER("Storage-CONTROLLER_ATLAS", "storage_controller"),
    STORAGE_WIDGETS("Storage-ATLAS", "storage_widget");

    private final String atlas;
    private final ResourceLocation texture;

    PanelAtlas(String atlas, String file) {
        this.atlas = atlas;
        this.texture = ResourceLocation.fromNamespaceAndPath(
                Prototype1201.MOD_ID, "textures/gui/panel/atlas/" + file + ".png");
    }

    /** The name eco asks with. */
    public String atlas() {
        return atlas;
    }

    /** Our own sheet, which has to be the same pixel size as the one it replaces. */
    public ResourceLocation texture() {
        return texture;
    }

    @Nullable
    public static ResourceLocation resolve(String atlas) {
        for (var own : values()) {
            if (own.atlas.equals(atlas)) {
                return own.texture;
            }
        }
        return null;
    }
}
