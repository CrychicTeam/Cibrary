package org.pickaid.pibrary.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import org.pickaid.pibrary.kubejs.event.ConfiguredKeyEventJS;

public interface PiEventS {
    EventGroup GROUP = EventGroup.of("CiServer");

    EventHandler PRESSED = GROUP.server("keyPressed", () -> ConfiguredKeyEventJS.Pressed.class);
    EventHandler CHARGING = GROUP.server("charging", () -> ConfiguredKeyEventJS.Charginng.class);
    EventHandler RELEASED = GROUP.server("released", () -> ConfiguredKeyEventJS.Released.class);
}
