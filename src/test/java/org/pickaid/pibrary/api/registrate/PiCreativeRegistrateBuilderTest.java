package org.pickaid.pibrary.api.registrate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tterrag.registrate.util.nullness.NonNullFunction;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.junit.jupiter.api.Test;

class PiCreativeRegistrateBuilderTest {
    @Test
    void normalEntrypointsDeclareCreativeAwareBuilders() throws NoSuchMethodException {
        Method item = PiBaseRegistrate.class.getMethod("item", String.class, NonNullFunction.class);
        Method block = PiBaseRegistrate.class.getMethod("block", String.class, NonNullFunction.class);

        assertEquals(PiItemBuilder.class, item.getReturnType());
        assertEquals(PiBlockBuilder.class, block.getReturnType());
    }

    @Test
    void creativeMethodsStayOnNormalBuilderChains() {
        assertItemBuilderApi((PiItemBuilder<Item, ?>) null);
        assertBlockBuilderApi((PiBlockBuilder<Block, ?>) null);
    }

    private static void assertItemBuilderApi(PiItemBuilder<Item, ?> builder) {
        if (builder == null) {
            return;
        }
        builder
                .section("scrolls")
                .variant("fireball", stack -> stack.getOrCreateTag().putString("spell", "fireball"))
                .variants(List.of("ice", "fire"), spell -> spell, (stack, spell) ->
                        stack.getOrCreateTag().putString("spell", spell))
                .variants(() -> List.of(1, 2, 3), level -> "level_" + level, (stack, level) ->
                        stack.getOrCreateTag().putInt("spell_level", level))
                .searchOnly()
                .parentOnly()
                .hidden();
    }

    private static void assertBlockBuilderApi(PiBlockBuilder<Block, ?> builder) {
        if (builder == null) {
            return;
        }
        builder
                .section("machines")
                .variant("charged", stack -> stack.getOrCreateTag().putBoolean("charged", true))
                .variants(List.of("north", "south"), facing -> facing, (stack, facing) ->
                        stack.getOrCreateTag().putString("facing", facing))
                .variants(() -> List.of(0, 1), level -> "level_" + level, (stack, level) ->
                        stack.getOrCreateTag().putInt("tier", level))
                .searchOnly()
                .parentOnly()
                .hidden();
    }

    @SuppressWarnings("unused")
    private static void compileOnlyEntrypointSample(PiBaseRegistrate<?> registrate) {
        PiItemBuilder<Item, ?> item = registrate.item("mana_dust", Item::new);
        PiBlockBuilder<Block, ?> block = registrate.block("charging_table", Block::new);
        NonNullFunction<Item.Properties, Item> itemFactory = Item::new;
        NonNullFunction<BlockBehaviour.Properties, Block> blockFactory = Block::new;
        if (System.nanoTime() == Long.MIN_VALUE) {
            registrate.creativeTab("main", tab -> tab
                    .title("itemGroup.example.main")
                    .icon((ItemLike) null)
                    .sections("materials", "machines"));
            registrate.creativeSections("materials", "machines");
            registrate.item("sample_item", itemFactory);
            registrate.block("sample_block", blockFactory);
        }
    }
}
