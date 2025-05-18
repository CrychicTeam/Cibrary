package org.pickaid.pibrary.content.context.action.engine.selector;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EntitySelector;
import org.pickaid.pibrary.content.context.action.engine.core.SelectorType;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedHashSet;
import java.util.Set;

public record SelfSelector() implements EntitySelector<SelfSelector> {

	public static Codec<SelfSelector> CODEC = Codec.unit(new SelfSelector());

	@Override
	public SelectorType<SelfSelector> type() {
		return LibraryRegistries.SELF.get();
	}

	@Override
	public LinkedHashSet<LivingEntity> find(ServerLevel sl, EngineContext ctx, SelectionType type) {
		return new LinkedHashSet<>(Set.of(ctx.user().user()));
	}

}
