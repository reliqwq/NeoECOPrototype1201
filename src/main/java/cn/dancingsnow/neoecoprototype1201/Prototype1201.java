package cn.dancingsnow.neoecoprototype1201;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

/**
 * Skeleton probe. It compiles only if the 1.20.1 eco and AE2 surfaces still match what the 1.21.1
 * addon uses, which is the whole question this project exists to answer.
 */
@Mod(Prototype1201.MOD_ID)
public final class Prototype1201 {
    public static final String MOD_ID = "neoecoprototype1201";

    public Prototype1201() {
    }

    /**
     * eco 20.5.0's builder is {@code id / desc / typeCount}: the row carries its own id and has no
     * visibility flag, unlike the 1.21.1 line where it is {@code desc / typeCount / visible}.
     */
    static ECOCellType probeCellType() {
        return ECOCellType.builder()
                .id(ResourceLocation.fromNamespaceAndPath(MOD_ID, "probe"))
                .desc(Component.translatable("row.neoecoprototype1201.probe"))
                .typeCount(1)
                .build();
    }

    /** The per-byte divisor our capacity maths depends on. */
    static int probeAmountPerByte() {
        return AEKeyType.items().getAmountPerByte();
    }
}
