package org.pickaid.pibrary.content.context.action.entity.core;

import java.util.LinkedHashMap;

public record ProjectileParams(
		int life, boolean bypassWall, boolean bypassEntity,
		long seed, LinkedHashMap<String, Double> params
) {

}
