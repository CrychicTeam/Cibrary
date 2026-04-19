package org.pickaid.pibrary.api.registrate.config;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateDefaults;
import org.pickaid.pibrary.runtime.registrate.config.PiCreativeTabRegistry;

public final class PiCreativeTabRegistration {
    private final PiRegistrate owner;
    private final String path;
    private final String title;

    public PiCreativeTabRegistration(PiRegistrate owner, String path, String title) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.path = PiRegistrateDefaults.requirePath(path);
        this.title = Objects.requireNonNull(title, "title");
    }

    public ResourceLocation id() {
        return owner.id(path);
    }

    public String title() {
        return title;
    }

    public String translationKey() {
        return "itemGroup." + owner.getModid() + "." + path;
    }

    public PiCreativeTabRegistration register() {
        return PiCreativeTabRegistry.register(this);
    }
}
