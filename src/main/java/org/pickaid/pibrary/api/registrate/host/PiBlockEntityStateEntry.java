package org.pickaid.pibrary.api.registrate.host;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.blockentity.PiStateBlockEntity;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateDefaults;

public final class PiBlockEntityStateEntry<S, T extends PiStateBlockEntity<S>> {
    private final PiRegistrate owner;
    private final String path;
    private final Class<S> stateType;
    private final Class<T> blockEntityType;

    public PiBlockEntityStateEntry(PiRegistrate owner, String path, Class<S> stateType, Class<T> blockEntityType) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.path = PiRegistrateDefaults.requirePath(path);
        this.stateType = Objects.requireNonNull(stateType, "stateType");
        this.blockEntityType = Objects.requireNonNull(blockEntityType, "blockEntityType");
    }

    public PiRegistrate owner() {
        return owner;
    }

    public String path() {
        return path;
    }

    public ResourceLocation id() {
        return owner.id(path);
    }

    public Class<S> stateType() {
        return stateType;
    }

    public Class<T> blockEntityType() {
        return blockEntityType;
    }
}
