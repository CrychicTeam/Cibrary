package org.pickaid.pibrary.content.context.action.engine.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import org.pickaid.pibrary.api.fastprojectileapi.collision.EntityStorageCache;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * SelectionType MONSTERS_ONLY = new SelectionType(
 *     "monsters_only",
 *     (entity, user) -> entity instanceof Monster monster && monster.isAlive()
 * );
 *
 * register it to the registry using:
 * SelectionType.register(MONSTERS_ONLY);
 */
public class SelectionType implements ExtendedEnumLike<SelectionType> {
	public static final SelectionType NONE = new SelectionType("none", (entity, user) -> false);
	public static final SelectionType ENEMY = new SelectionType("enemy", (entity, user) ->
			entity instanceof LivingEntity le && le.isAlive() &&
					!user.isPassengerOfSameVehicle(le) && !likesEachOther(user, le, false));
	public static final SelectionType ENEMY_NO_FAMILY = new SelectionType("enemy_no_family", (entity, user) ->
			entity instanceof LivingEntity le && le.isAlive() &&
					!user.isPassengerOfSameVehicle(le) && !likesEachOther(user, le, true));
	public static final SelectionType ALLY = new SelectionType("ally", (entity, user) ->
			entity instanceof LivingEntity le && le.isAlive() && likesEachOther(user, le, false));
	public static final SelectionType ALLY_AND_FAMILY = new SelectionType("ally_and_family", (entity, user) ->
			entity instanceof LivingEntity le && le.isAlive() && likesEachOther(user, le, true));
	public static final SelectionType ALL = new SelectionType("all", (entity, user) ->
			entity instanceof LivingEntity le && le.isAlive());
	private static final Map<String, SelectionType> REGISTRY = new HashMap<>();

	static {
		register(NONE);
		register(ENEMY);
		register(ENEMY_NO_FAMILY);
		register(ALLY);
		register(ALLY_AND_FAMILY);
		register(ALL);
	}

	public static final Codec<SelectionType> CODEC = Codec.STRING.comapFlatMap(
			name -> {
				SelectionType type = byName(name);
				return type != null ? DataResult.success(type) : DataResult.error(() -> "Unknown selection type: " + name);
			},
			SelectionType::getName
	);

	private static boolean likesEachOther(LivingEntity a, LivingEntity b, boolean checksFamily) {
		if (a.isAlliedTo(b) || b.isAlliedTo(a)) return true;
		if (!checksFamily) return false;
		if (hatesEachOther(a, b)) return false;
		return a.getType() == b.getType();
	}

	private static boolean hatesEachOther(LivingEntity a, LivingEntity b) {
		if (a.getLastHurtMob() == b || a.getLastHurtByMob() == b) return true;
		if (b.getLastHurtMob() == a || b.getLastHurtByMob() == a) return true;
		if (a instanceof Mob ma && ma.getTarget() == b) return true;
		return b instanceof Mob mb && mb.getTarget() == a;
	}

	public static void register(SelectionType type) {
		REGISTRY.put(type.getName(), type);
	}

	public static SelectionType byName(String name) {
		return REGISTRY.getOrDefault(name, NONE);
	}

	public static SelectionType[] values() {
		return REGISTRY.values().toArray(new SelectionType[0]);
	}

	private final String name;
	private final BiPredicate<Entity, LivingEntity> check;

	public SelectionType(String name, BiPredicate<Entity, LivingEntity> check) {
		this.name = name;
		this.check = check;
	}

	public String getName() {
		return name;
	}

	public boolean test(Entity target, LivingEntity user) {
		return check.test(target, user);
	}

	public Iterable<Entity> select(ServerLevel sl, EngineContext ctx, AABB aabb) {
		return EntityStorageCache.get(sl).foreach(aabb, x -> check.test(x, ctx.user().user()));
	}

	@Override
	public Codec<SelectionType> codec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		SelectionType that = (SelectionType) obj;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}