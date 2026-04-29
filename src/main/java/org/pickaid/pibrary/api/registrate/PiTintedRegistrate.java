package org.pickaid.pibrary.api.registrate;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.pickaid.pibrary.api.render.tint.PiBlockTintBuilder;
import org.pickaid.pibrary.api.render.tint.PiItemTintBuilder;
import org.pickaid.pibrary.api.render.tint.PiTintRegistry;

/**
 * Tint shortcuts mixed into Pibrary's Registrate base.
 */
@SuppressWarnings({"unchecked", "varargs"})
public interface PiTintedRegistrate<S extends PiTintedRegistrate<S>> {
    PiTintRegistry tints();

    default S tintBlock(Supplier<? extends Block> block, int color) {
        tints().blockConstant(color, block);
        return self();
    }

    default S tintBlock(Supplier<? extends Block> block, Consumer<PiBlockTintBuilder> configure) {
        PiBlockTintBuilder builder = PiBlockTintBuilder.create();
        Objects.requireNonNull(configure, "configure").accept(builder);
        tints().block(builder.build(), block);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintBlocks(int color, Supplier<? extends Block>... blocks) {
        tints().blockConstant(color, blocks);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintGrass(Supplier<? extends Block>... blocks) {
        tints().blockGrass(blocks);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintFoliage(Supplier<? extends Block>... blocks) {
        tints().blockFoliage(blocks);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintWater(Supplier<? extends Block>... blocks) {
        tints().blockWater(blocks);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintBlock(BlockColor color, Supplier<? extends Block>... blocks) {
        tints().block(color, blocks);
        return self();
    }

    default S tintItem(Supplier<? extends ItemLike> item, int layer, int color) {
        tints().itemConstant(layer, color, item);
        return self();
    }

    default S tintItem(Supplier<? extends ItemLike> item, Consumer<PiItemTintBuilder> configure) {
        PiItemTintBuilder builder = PiItemTintBuilder.create();
        Objects.requireNonNull(configure, "configure").accept(builder);
        tints().item(builder.build(), item);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintItems(int layer, int color, Supplier<? extends ItemLike>... items) {
        tints().itemConstant(layer, color, items);
        return self();
    }

    default S tintDurability(
            Supplier<? extends ItemLike> item,
            int layer,
            int brokenColor,
            int fullColor
    ) {
        tints().itemDurability(layer, brokenColor, fullColor, item);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintItem(ItemColor color, Supplier<? extends ItemLike>... items) {
        tints().item(color, items);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintBlockItem(Supplier<? extends Block>... blocks) {
        tints().itemFromBlock(blocks);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintBlockItemLayer(int layer, Supplier<? extends Block>... blocks) {
        tints().itemFromBlockLayer(layer, blocks);
        return self();
    }

    @SuppressWarnings("varargs")
    default S tintBlockAndItem(
            BlockColor blockColor,
            ItemColor itemColor,
            Supplier<? extends Block>... blocks
    ) {
        tints().blockAndItem(blockColor, itemColor, blocks);
        return self();
    }

    @SuppressWarnings("unchecked")
    private S self() {
        return (S) this;
    }
}
