package org.pickaid.pibrary.content.context.action.engine.context;

import org.pickaid.pibrary.content.context.action.engine.helper.Orientation;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ClientActionHandler {

	public static void useAction(int user, ResourceLocation actionId,
								Vec3 origin, Orientation facing, long seed, double tickUsing, double power) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		var e = level.getEntity(user);
		if (!(e instanceof LivingEntity le)) return;
		var action = level.registryAccess().registryOrThrow(LibraryRegistries.ACTION)
				.get(actionId);
		if (action == null) return;
		action.execute(new ActionContext(le, origin, facing, seed, tickUsing, power));
	}
}
