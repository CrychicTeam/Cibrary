package org.pickaid.pibrary.dev.example;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.registrate.PiBlockProps;
import org.pickaid.pibrary.api.registrate.PiRegistrate;

/**
 * Small development sample for creative-tab sections, stack variants, and tint declarations.
 */
public final class CounterCreativeEntries {
    public static final PiRegistrate REGISTRATE = PiRegistrate.create(Pibrary.MOD_ID);

    public static final ItemEntry<Item> COUNTER_TOKEN = REGISTRATE
            .item("counter_token", Item::new)
            .section("materials")
            .register();

    public static final ItemEntry<Item> COUNTER_SCROLL = REGISTRATE
            .item("counter_scroll", Item::new)
            .section("scrolls")
            .variants(List.of("red", "blue"), color -> color, (stack, color) ->
                    stack.getOrCreateTag().putString("counter_color", color))
            .searchOnly()
            .properties(properties -> properties.stacksTo(1))
            .register();

    public static final BlockEntry<Block> COUNTER_TABLE = REGISTRATE
            .block("counter_table", Block::new)
            .section("machines")
            .initialProperties(PiBlockProps::metal)
            .simpleItem()
            .register();

    public static final RegistryEntry<CreativeModeTab> MAIN_TAB = REGISTRATE
            .creativeTab("main", tab -> tab
                    .title("itemGroup.pibrary.main")
                    .iconStack(CounterCreativeEntries::tabIcon)
                    .sections("materials", "scrolls", "machines"))
            .register();

    private CounterCreativeEntries() {
    }

    public static void register() {
    }

    @SuppressWarnings("unchecked")
    public static void registerClientTints() {
        REGISTRATE
                .tintBlock(COUNTER_TABLE, tint -> tint.layer(0).constant(0x55AAFF))
                .tintBlockItem(COUNTER_TABLE)
                .tintItem(COUNTER_SCROLL, tint -> tint.layer(0).stack(CounterCreativeEntries::scrollColor));
    }

    private static ItemStack tabIcon() {
        return new ItemStack(COUNTER_TABLE.get());
    }

    private static int scrollColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0xFFFFFF;
        }
        return switch (tag.getString("counter_color")) {
            case "red" -> 0xDD4433;
            case "blue" -> 0x3377DD;
            default -> 0xFFFFFF;
        };
    }
}
