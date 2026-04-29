package org.pickaid.pibrary.api.registrate;

import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Ready-to-use Registrate entry point for simple mods.
 *
 * <p>Use this class when the normal Registrate chain is enough. If a project
 * needs its own domain methods, extend {@link PiBaseRegistrate} instead of
 * adding those methods to this generic entry point.</p>
 */
public final class PiRegistrate extends PiBaseRegistrate<PiRegistrate> {
    private PiRegistrate(String modid) {
        super(modid);
    }

    public static PiRegistrate create(String modid) {
        PiRegistrate registrate = new PiRegistrate(modid);
        return registrate.registerToDefaultBus();
    }

    public static PiRegistrate create(String modid, IEventBus modBus) {
        PiRegistrate registrate = new PiRegistrate(modid);
        return registrate.registerTo(modBus);
    }
}
