package org.pickaid.pibrary.init.data;


import org.pickaid.pibrary.init.data.spell.*;
import org.pickaid.pibrary.init.data.spell.fire.*;
import org.pickaid.pibrary.init.data.spell.ground.*;
import org.pickaid.pibrary.init.data.spell.ice.*;

import java.util.List;

public class SpellDataGenRegistry {

	public static final List<SpellDataGenEntry> LIST = List.of(
			new WinterStorm(),
			new FlameSpells(),
			new ArrowSpells(),
			new MagnetCore(),
			new IcyFlash(),
			new EarthSpike(),
			new FlameCharge()
	);

}
