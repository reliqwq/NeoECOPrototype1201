package cn.dancingsnow.neoecoprototype1201.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.dancingsnow.neoecoprototype1201.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The bag the structure expansion card hands over instead of ninety loose blocks: right-click pours
 * everything into the player's inventory, whatever cannot fit falls at their feet, and the bag itself
 * turns into one empty migration drive - the tile it is drawn from.
 *
 * <p>What is in a bag is a plain saved {@link ItemStack} per entry, in the stack's own NBT tag - id,
 * count and any item NBT. The first version of this item stored id and count only, which was fine for
 * refund blocks and quietly fatal for the one thing a bag now also carries: a work-order pattern, whose
 * whole content is its NBT. An entry this game cannot resolve (a pack change) is kept in the bag rather
 * than quietly eaten, which is the same rule the card follows everywhere else: something the player owns
 * may move places, but it never just vanishes.
 *
 * <p>Ported from the 1.21.1 line, where the bag rode {@code DataComponents.CUSTOM_DATA} and the name
 * carried a per-stack tier colour through {@code ITEM_NAME}. 1.20.1 has no data components, so the bag
 * rides the stack tag and the tier colour is dropped - a bag wears its plain name on every line that has
 * no component system; what tells the bags apart is the pour, not the label.
 */
public class BackupDriveItem extends Item {

    private static final Logger LOGGER = LoggerFactory.getLogger("neoeco1201-backup-drive");

    /** The list inside the stack tag; each element is one saved item stack. */
    private static final String TAG_BLOCKS = "neoecoprototype:blocks";
    /**
     * Set only when the click that filled this drive <b>paid an item</b> - a card or a disk eaten - which is
     * what entitles the player to that item's shell back. The person who found the mint asked for exactly
     * this accounting: a card that costs nothing could be clicked again and again, each refusal printing a
     * ticket into a drive that pours into one more empty migration drive.
     */
    private static final String TAG_SHELL = "neoecoprototype:shell";

    public BackupDriveItem(Properties properties) {
        super(properties);
    }

    /** Mark that this drive owes its player the shell of the card or disk the click ate. */
    public static void oweShell(ItemStack drive) {
        drive.getOrCreateTag().putBoolean(TAG_SHELL, true);
    }

    /** Whether pouring this drive out should leave the player holding one empty migration drive. */
    public static boolean paysShell(ItemStack drive) {
        CompoundTag tag = drive.getTag();
        return tag != null && tag.getBoolean(TAG_SHELL);
    }

    /**
     * A bag holding these stacks. The caller is expected to have merged them already - one entry per item,
     * each within the item's own stack limit - because that is the whole point of the container.
     */
    public static ItemStack withContents(List<ItemStack> stacks) {
        ItemStack bag = new ItemStack(Registration.BACKUP_DRIVE.get());
        ListTag list = new ListTag();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            list.add(stack.save(new CompoundTag()));
        }
        CompoundTag tag = bag.getOrCreateTag();
        tag.put(TAG_BLOCKS, list);
        return bag;
    }

    /** What the bag holds right now; entries this game cannot name are skipped here. */
    public static List<ItemStack> contentsOf(ItemStack bag) {
        List<ItemStack> contents = new ArrayList<>();
        for (CompoundTag entry : entries(bag)) {
            ItemStack stack = ItemStack.of(entry);
            if (!stack.isEmpty()) {
                contents.add(stack);
            }
        }
        return contents;
    }

    /** Entries the bag holds that this game cannot name - they stay in the bag and are counted here. */
    public static int unresolvableOf(ItemStack bag) {
        int unresolvable = 0;
        for (CompoundTag entry : entries(bag)) {
            if (ItemStack.of(entry).isEmpty()) {
                unresolvable++;
            }
        }
        return unresolvable;
    }

    /** How many entries the bag holds, counted whether or not this game can name them. */
    public static int kindsOf(ItemStack bag) {
        return entries(bag).size();
    }

    private static List<CompoundTag> entries(ItemStack bag) {
        List<CompoundTag> entries = new ArrayList<>();
        CompoundTag tag = bag.getTag();
        if (tag == null || !tag.contains(TAG_BLOCKS, Tag.TAG_LIST)) {
            return entries;
        }
        for (Tag element : tag.getList(TAG_BLOCKS, Tag.TAG_COMPOUND)) {
            if (element instanceof CompoundTag entry) {
                entries.add(entry);
            }
        }
        return entries;
    }

    /**
     * Right-click: everything the bag holds goes into the player's inventory, whatever cannot fit falls out
     * as an ordinary item drop at their feet, and the bag itself turns into one empty migration drive - the
     * tile it is drawn from. Nothing is left holding a residue, and nothing has to be hunted for twice.
     *
     * <p>The one case that refuses is a bag holding an entry this game cannot name, which only happens after
     * a pack change. Emptying it would drop what cannot be rebuilt and destroying it would delete things
     * nobody was told about, so the bag stays and the tooltip says how many entries are the problem.</p>
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        List<ItemStack> contents = contentsOf(bag);
        int unresolvable = unresolvableOf(bag);
        if (unresolvable > 0) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(
                        "card.neoecoprototype1201.backup_drive.unresolvable_refusal", unresolvable), true);
            }
            return InteractionResultHolder.fail(bag);
        }
        if (contents.isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("card.neoecoprototype1201.backup_drive.empty"), true);
            }
            return InteractionResultHolder.fail(bag);
        }
        if (!level.isClientSide) {
            int poured = 0;
            int dropped = 0;
            for (ItemStack stack : contents) {
                if (player.getInventory().add(stack.copy())) {
                    poured += stack.getCount();
                    continue;
                }
                dropped += stack.getCount();
                var drop = new ItemEntity(level, player.getX(), player.getEyeY(), player.getZ(),
                        stack.copy());
                // Ten ticks, so a stack that just fell out cannot go straight back into the hand that
                // threw it while the bag is still being held.
                drop.setPickUpDelay(10);
                level.addFreshEntity(drop);
            }
            // The empty drive is what a spent disk leaves in your hand, and this tile is that tile
            // darkened - so a drive whose click actually ate an item closes that loop: it turns back into
            // the shell. A drive from a click that paid nothing (a refusal, or creative) pours out and is
            // simply gone, or every spare card in a base would be a mint.
            var shell = paysShell(bag)
                    ? new ItemStack(Registration.MIGRATION_DRIVE_EMPTY.get())
                    : ItemStack.EMPTY;
            player.setItemInHand(hand, shell);
            LOGGER.info(
                    "backup drive poured: {} entries, {} into the inventory, {} on the floor at {}, shell"
                            + " owed = {}, the hand now holds {}",
                    contents.size(), poured, dropped, player.blockPosition(), paysShell(bag),
                    shell.isEmpty() ? "nothing" : net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .getKey(shell.getItem()));
            return InteractionResultHolder.sidedSuccess(shell, level.isClientSide);
        }
        return InteractionResultHolder.pass(bag);
    }

    @Override
    public void appendHoverText(ItemStack bag, @Nullable Level level, List<Component> lines,
                                net.minecraft.world.item.TooltipFlag flag) {
        List<ItemStack> contents = contentsOf(bag);
        if (contents.isEmpty() && unresolvableOf(bag) == 0) {
            lines.add(Component.translatable("card.neoecoprototype1201.backup_drive.empty_contents"));
            return;
        }
        int shown = 0;
        for (ItemStack stack : contents) {
            if (shown == 4) {
                lines.add(Component.translatable("card.neoecoprototype1201.backup_drive.more",
                        contents.size() - shown));
                break;
            }
            lines.add(Component.translatable("card.neoecoprototype1201.backup_drive.line",
                    stack.getHoverName(), stack.getCount()));
            shown++;
        }
        int unresolvable = unresolvableOf(bag);
        if (unresolvable > 0) {
            lines.add(Component.translatable("card.neoecoprototype1201.backup_drive.unresolvable", unresolvable));
        }
    }
}
