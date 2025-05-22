package org.pickaid.pibrary.api.fastprojectileapi.entity;

import dev.xkmc.l2serial.util.Wrappers;
import net.minecraft.world.entity.LivingEntity;
import org.pickaid.pibrary.api.fastprojectileapi.collision.UserCacheHolder;

public interface EntityCachingUser {

	UserCacheHolder entityCache();

	default LivingEntity self() {
		return Wrappers.cast(this);
	}

}
