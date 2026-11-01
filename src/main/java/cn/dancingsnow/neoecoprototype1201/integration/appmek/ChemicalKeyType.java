package cn.dancingsnow.neoecoprototype1201.integration.appmek;

import appeng.api.stacks.AEKeyType;
import me.ramidzkh.mekae2.ae2.MekanismKeyType;

/**
 * The only class on this line that names Applied Mekanistics.
 *
 * <p>eco's own chemical cell does the same thing - its constructor hands {@code MekanismKeyType.TYPE} to
 * eco's cell constructor - so this is the very key type the drive already knows how to file, not a second
 * one we invent. The name lives here alone so that the class which decides whether to register the family
 * never has to mention it: an unexecuted method body is not resolved.
 */
public final class ChemicalKeyType {

    public static AEKeyType get() {
        return MekanismKeyType.TYPE;
    }

    private ChemicalKeyType() {
    }
}
