package org.pickaid.pibrary.tools.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.BossEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;

public class CodecUtils {

    public static final Codec<BossBarColor> COLOR = Codec.STRING.xmap(BossEvent.BossBarColor::byName, BossEvent.BossBarColor::getName);
    public static final Codec<BossBarOverlay> OVERLAY = Codec.STRING.xmap(BossEvent.BossBarOverlay::byName, BossEvent.BossBarOverlay::getName);
    public static final Codec<MinMaxBounds.Ints> BOUNDS_INTS = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(MinMaxBounds::getMin),
            Codec.INT.fieldOf("max").forGetter(MinMaxBounds::getMax)
    ).apply(instance, MinMaxBounds.Ints::between));
    public static final Codec<MinMaxBounds.Doubles> BOUNDS_DOUBLES = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("min").forGetter(MinMaxBounds::getMin),
            Codec.DOUBLE.fieldOf("max").forGetter(MinMaxBounds::getMax)
    ).apply(instance, MinMaxBounds.Doubles::between));
    public static final Codec<LocationPredicate> LOCATION_PREDICATE = ExtraCodecs.JSON.xmap(LocationPredicate::fromJson, LocationPredicate::serializeToJson);
    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> clazz) {
        return Codec.INT.xmap(i -> clazz.getEnumConstants()[i], Enum::ordinal);
    }
}
