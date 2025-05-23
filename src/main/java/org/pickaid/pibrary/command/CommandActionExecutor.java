package org.pickaid.pibrary.command;

import dev.xkmc.l2library.init.events.GeneralEventHandler;
import org.pickaid.pibrary.content.context.action.engine.context.ActionContext;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;
import org.pickaid.pibrary.content.context.interaction.InteractionActionCastType;
import net.minecraft.world.entity.LivingEntity;
import org.pickaid.pibrary.content.logic.ticker.BaseTicker;

public class CommandActionExecutor {

	public static boolean execute(LivingEntity le, InteractionAction spell, int time, double power, int distance) {
		if (spell.castType() == InteractionActionCastType.INSTANT) {
			var val = ActionContext.castAction(le, spell, time, power, distance);
			if (val == null) return false;
			spell.execute(val);
		} else {
			BaseTicker.schedule(new CommandActionExecutor(le, spell, time, power, distance)::tick, 0, false);
		}
		return true;
	}

	private final LivingEntity le;
	private final InteractionAction spell;
	private final int duration;
	private final double power;
	private final int distance;

	private int time;

	public CommandActionExecutor(LivingEntity le, InteractionAction spell, int time, double power, int distance) {
		this.le = le;
		this.spell = spell;
		this.duration = time;
		this.power = power;
		this.distance = distance;
	}

	public boolean tick() {
		double p = spell.castType() == InteractionActionCastType.CHARGE && time < duration ? 0 : power;
		var val = ActionContext.castAction(le, spell, time, p, distance);
		if (val != null) {
			spell.execute(val);
		}
		time++;
		return time > duration;
	}

}
