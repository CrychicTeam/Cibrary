package org.pickaid.pibrary.command;

import dev.xkmc.l2library.init.events.GeneralEventHandler;
import org.pickaid.pibrary.content.context.action.engine.context.ActionContext;
import org.pickaid.pibrary.content.context.skill.Action;
import org.pickaid.pibrary.content.context.skill.ActionCastType;
import net.minecraft.world.entity.LivingEntity;

public class CommandSpellExecutor {

	public static boolean execute(LivingEntity le, Action spell, int time, double power, int distance) {
		if (spell.castType() == ActionCastType.INSTANT) {
			var val = ActionContext.castSpell(le, spell, time, power, distance);
			if (val == null) return false;
			spell.execute(val);
		} else {
			GeneralEventHandler.schedulePersistent(new CommandSpellExecutor(le, spell, time, power, distance)::tick);
		}
		return true;
	}

	private final LivingEntity le;
	private final Action spell;
	private final int duration;
	private final double power;
	private final int distance;

	private int time;

	public CommandSpellExecutor(LivingEntity le, Action spell, int time, double power, int distance) {
		this.le = le;
		this.spell = spell;
		this.duration = time;
		this.power = power;
		this.distance = distance;
	}

	public boolean tick() {
		double p = spell.castType() == ActionCastType.CHARGE && time < duration ? 0 : power;
		var val = ActionContext.castSpell(le, spell, time, p, distance);
		if (val != null) {
			spell.execute(val);
		}
		time++;
		return time > duration;
	}

}
