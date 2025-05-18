package org.pickaid.pibrary.content.context.action.engine.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EntitySelector;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.SelectorType;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedHashSet;
import java.util.List;

public record MoveSelector(List<Modifier<?>> modifiers, EntitySelector<?> child)
		implements EntitySelector<MoveSelector> {

	public static final Codec<MoveSelector> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.list(Modifier.CODEC).fieldOf("modifiers").forGetter(e -> e.modifiers),
			EntitySelector.CODEC.fieldOf("child").forGetter(e -> e.child)
	).apply(i, MoveSelector::new));

	@Override
	public SelectorType<MoveSelector> type() {
		return LibraryRegistries.MOVE_SELECTOR.get();
	}

	@Override
	public LinkedHashSet<LivingEntity> find(ServerLevel sl, EngineContext ctx, SelectionType type) {
		for (var e : modifiers) {
			ctx = ctx.with(e.modify(ctx));
		}
		return child().find(sl, ctx, type);
	}

}