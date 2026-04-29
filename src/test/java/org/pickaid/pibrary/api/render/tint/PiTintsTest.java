package org.pickaid.pibrary.api.render.tint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.ItemLike;
import org.junit.jupiter.api.Test;

class PiTintsTest {
    @Test
    void blockLayerOnlyTintsSelectedLayer() {
        var tint = PiBlockTints.layer(1, 0x123456);

        assertEquals(PiColors.NO_TINT, tint.getColor(null, null, null, 0));
        assertEquals(0x123456, tint.getColor(null, null, null, 1));
    }

    @Test
    void blockLayersCanDispatchComplexProviders() {
        var tint = PiBlockTints.layers(Map.of(
                0, PiBlockTints.constant(0x112233),
                2, PiBlockTints.multiply(PiBlockTints.constant(0x808080), PiBlockTints.constant(0x804020))
        ));

        assertEquals(0x112233, tint.getColor(null, null, null, 0));
        assertEquals(PiColors.NO_TINT, tint.getColor(null, null, null, 1));
        assertEquals(0x402010, tint.getColor(null, null, null, 2));
    }

    @Test
    void itemStackLayerOnlyTintsSelectedLayer() {
        var tint = PiItemTints.stackLayer(1, stack -> 0x123456);

        assertEquals(PiColors.NO_TINT, tint.getColor(null, 0));
        assertEquals(0x123456, tint.getColor(null, 1));
    }

    @Test
    void itemLayersCanDispatchComplexProviders() {
        var tint = PiItemTints.layers(Map.of(
                0, PiItemTints.constant(0x112233),
                2, PiItemTints.multiply(PiItemTints.constant(0x808080), PiItemTints.constant(0x804020))
        ));

        assertEquals(0x112233, tint.getColor(null, 0));
        assertEquals(PiColors.NO_TINT, tint.getColor(null, 1));
        assertEquals(0x402010, tint.getColor(null, 2));
    }

    @Test
    void tintRegistryCountsTargetsBeforeResolving() {
        Supplier<net.minecraft.world.level.block.Block> unresolvedBlock = () -> null;
        Supplier<ItemLike> unresolvedItem = () -> null;
        PiTintRegistry registry = new PiTintRegistry()
                .block(PiBlockTints.constant(0xFFFFFF), unresolvedBlock)
                .item(PiItemTints.constant(0xFFFFFF), unresolvedItem);

        assertEquals(1, registry.blockTargetCount());
        assertEquals(1, registry.itemTargetCount());
    }

    @Test
    void tintRegistryRejectsNullResolvedTargets() {
        PiTintRegistry registry = new PiTintRegistry()
                .block(PiBlockTints.constant(0xFFFFFF), () -> null);

        assertThrows(NullPointerException.class, registry::validate);
    }

    @Test
    void tintLayersRejectNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> PiBlockTints.layer(-1, 0xFFFFFF));
        assertThrows(IllegalArgumentException.class, () -> PiItemTints.layer(-1, 0xFFFFFF));
    }

    @Test
    void tintRegistryHasUseCaseNamedShortcutsForCommonBlockAndItemTints() {
        Supplier<Block> unresolvedBlock = () -> null;
        Supplier<ItemLike> unresolvedItem = () -> null;

        PiTintRegistry registry = new PiTintRegistry()
                .blockConstant(0x44AAFF, unresolvedBlock)
                .blockFoliage(unresolvedBlock)
                .itemConstant(0, 0xFF8844, unresolvedItem)
                .itemDurability(1, 0xAA2222, 0x22AAFF, unresolvedItem)
                .itemFromBlock(unresolvedBlock);

        assertEquals(2, registry.blockTargetCount());
        assertEquals(3, registry.itemTargetCount());
    }

    @Test
    void blockTintBuilderKeepsComplexLayerLogicReadable() {
        var tint = PiBlockTintBuilder.create()
                .layer(0).constant(0x112233)
                .layer(2).multiply(PiBlockTints.constant(0x808080), PiBlockTints.constant(0x804020))
                .build();

        assertEquals(0x112233, tint.getColor(null, null, null, 0));
        assertEquals(PiColors.NO_TINT, tint.getColor(null, null, null, 1));
        assertEquals(0x402010, tint.getColor(null, null, null, 2));
    }

    @Test
    void itemTintBuilderKeepsComplexLayerLogicReadable() {
        var tint = PiItemTintBuilder.create()
                .layer(0).constant(0x112233)
                .layer(2).multiply(PiItemTints.constant(0x808080), PiItemTints.constant(0x804020))
                .build();

        assertEquals(0x112233, tint.getColor(null, 0));
        assertEquals(PiColors.NO_TINT, tint.getColor(null, 1));
        assertEquals(0x402010, tint.getColor(null, 2));
    }
}
