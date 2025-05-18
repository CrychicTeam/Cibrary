package org.pickaid.pibrary.content.context.action.engine.context;

import dev.xkmc.l2library.init.events.GeneralEventHandler;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import  org.objecthunter.exp4j.Expression;
import net.minecraft.util.RandomSource;
import org.pickaid.pibrary.content.logic.ticker.BaseTicker;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public record EngineContext(UserContext user, LocationContext loc, RandomSource rand, Map<String, Double> parameters) {

	public EngineContext with(LocationContext modified) {
		return new EngineContext(user, modified, rand, parameters);
	}

	public EngineContext withParam(String key, double val) {
		var ans = new LinkedHashMap<>(parameters);
		ans.put(key, val);
		return new EngineContext(user, loc, rand, ans);
	}

	public void iterateOn(LocationContext loc, @Nullable String index, int i, ConfiguredEngine<?> child) {
		if (index == null || index.isEmpty()) {
			execute(loc, child);
			return;
		}
		var param = new LinkedHashMap<>(parameters);
		param.put(index, (double) i);
		execute(loc, param, child);
	}

	public void execute(LocationContext loc, ConfiguredEngine<?> child) {
		execute(loc, parameters, child);
	}

	public void execute(ConfiguredEngine<?> child) {
		execute(loc, parameters, child);
	}

	public void execute(LocationContext loc, Map<String, Double> parameters, ConfiguredEngine<?> child) {
		child.execute(new EngineContext(user, loc, nextRand(), parameters));
	}

	public double eval(Expression exp) {
		exp.setVariables(parameters);
		if (user.scheduler() != null)
			exp.setVariable("Time", user.scheduler().time);
		exp.setVariable("PosX", loc.pos().x);
		exp.setVariable("PosY", loc.pos().y);
		exp.setVariable("PosZ", loc.pos().z);
		exp.setVariable("CasterX", user.user().getX());
		exp.setVariable("CasterY", user.user().getY());
		exp.setVariable("CasterZ", user.user().getZ());
		return exp.evaluate();
	}

	public void schedule(int tick, Runnable o) {
		var sche = user.scheduler();
		if (sche == null) throw new IllegalStateException("Scheduler is not present!");
		sche.schedule(tick, o);
	}

	public RandomSource nextRand() {
		return RandomSource.create(rand.nextLong());
	}

	public void registerScheduler() {
		var sche = user.scheduler();
		if (sche == null) return;
		if (!sche.isFinished()) {
			if (user().level().isClientSide())
				BaseTicker.schedule(sche::tick, sche.time, false);
			else GeneralEventHandler.schedulePersistent(sche::tick);
		}
	}

}
