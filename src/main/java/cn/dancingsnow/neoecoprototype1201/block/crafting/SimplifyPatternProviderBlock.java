package cn.dancingsnow.neoecoprototype1201.block.crafting;

import appeng.block.crafting.PatternProviderBlock;

/**
 * The L1 pattern provider's block: AE2's own provider under our green box.
 *
 * <p>Nothing is overridden, and nothing can be: this generation's {@code PatternProviderBlock} has only a
 * no-argument constructor - it takes AE2's {@code metalProps()} itself and there is no {@code Properties}
 * overload for a subclass to feed - and its {@code onActivated} ends by calling {@code openMenu} on the
 * block entity, so the machine's behaviour is entirely whatever the block entity answers.
 */
public class SimplifyPatternProviderBlock extends PatternProviderBlock {
    public SimplifyPatternProviderBlock() {
        super();
    }
}
