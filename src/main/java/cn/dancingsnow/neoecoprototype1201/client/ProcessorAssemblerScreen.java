package cn.dancingsnow.neoecoprototype1201.client;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import cn.dancingsnow.neoecoprototype1201.menu.ProcessorAssemblerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The assembler's screen: AE2's molecular-assembler layout, drawn from a style file of our own.
 *
 * <p>The style has to be ours because it carries the dialog title, and the title is the point: AE2's file
 * names {@code gui.ae2.MolecularAssembler}, so reusing it would label this machine 分子装配室 - the same
 * identity leak the crafting-status list had. AE2's own {@code StyleManager} resolves every style path
 * against AE2's namespace ({@code AppEng.makeId}), so the file is read here, from our namespace, rather than
 * through {@code InitScreens}.</p>
 */
public class ProcessorAssemblerScreen extends UpgradeableScreen<ProcessorAssemblerMenu> {
    private static final ResourceLocation STYLE =
            ResourceLocation.fromNamespaceAndPath(Prototype1201.MOD_ID, "screens/processor_assembler.json");

    /**
     * Parsed once, the way AE2 caches its own styles: a style is immutable data. A missing or malformed file
     * shows up here as the exception naming the path, at the moment a player right-clicks the machine.
     */
    private static ScreenStyle cachedStyle;

    private final ProcessorAssemblerMenu assemblerMenu;
    private final ProgressBar progressBar;

    public ProcessorAssemblerScreen(ProcessorAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, sharedStyle());
        assemblerMenu = menu;
        progressBar = new ProgressBar(menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("progressBar", progressBar);
    }

    private static ScreenStyle sharedStyle() {
        if (cachedStyle == null) {
            try (var reader = Minecraft.getInstance().getResourceManager().openAsReader(STYLE)) {
                ScreenStyle loaded = ScreenStyle.GSON.fromJson(reader, ScreenStyle.class);
                loaded.validate();
                cachedStyle = loaded;
            } catch (Exception e) {
                throw new IllegalStateException("Could not load assembler screen style " + STYLE, e);
            }
        }
        return cachedStyle;
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        int max = assemblerMenu.getMaxProgress();
        progressBar.setFullMsg(Component.literal(max > 0 ? assemblerMenu.getCurrentProgress() * 100 / max + "%"
                : "0%"));
    }
}
