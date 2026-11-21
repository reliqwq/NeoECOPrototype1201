package cn.dancingsnow.neoecoprototype1201.integration.kubejs;

import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoprototype1201.items.CustomInfiniteCellItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;

/** Client-side model registration for KubeJS storage matrices. */
public final class ScriptedMatrixClientModels {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(ScriptedMatrixClientModels.class);

    private ScriptedMatrixClientModels() {
    }

    /** Register the eco default cell model or each script's explicit override. */
    public static void registerCellModels(ResourceLocation defaultModel) {
        for (var item : BuiltInRegistries.ITEM) {
            if (item instanceof CustomInfiniteCellItem custom) {
                ECOCellModels.register(custom, custom.getDriveModel() != null
                        ? custom.getDriveModel()
                        : defaultModel);
            }
        }
    }

    /** Register explicit KubeJS cell model overrides as standalone baked models. */
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (var item : BuiltInRegistries.ITEM) {
            if (item instanceof CustomInfiniteCellItem custom && custom.getDriveModel() != null) {
                event.register(custom.getDriveModel());
            }
        }
    }

    /**
     * Register the in-drive model of every KubeJS-created storage matrix.
     *
     * <p>Script-created finite matrices are plain {@code SimplifyStorageCellItem} instances, not
     * {@link CustomInfiniteCellItem}, so the loop above never sees them and their drive slot would
     * render without a model.
     */
    public static int registerScriptedCellModels() {
        int registered = 0;
        for (var entry : cn.dancingsnow.neoecoprototype1201.api.StorageMatrixRegistry.definitions().entrySet()) {
            ResourceLocation driveModel = entry.getKey().driveModel();
            if (driveModel != null) {
                ECOCellModels.register(entry.getValue(), driveModel);
                registered++;
                LOGGER.debug(
                        "scripted matrix {} ({} {}) -> inventory {} / drive {}",
                        entry.getKey().id(), entry.getKey().type(), entry.getKey().size(),
                        entry.getKey().inventoryModel(), driveModel);
            }
        }
        return registered;
    }

    /** Standalone models for the scripted drive overrides registered above. */
    public static void registerScriptedAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (var entry : cn.dancingsnow.neoecoprototype1201.api.StorageMatrixRegistry.definitions().entrySet()) {
            ResourceLocation driveModel = entry.getKey().driveModel();
            if (driveModel != null) {
                event.register(driveModel);
            }
        }
    }
}
