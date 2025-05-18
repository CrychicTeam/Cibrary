package org.pickaid.pibrary.api.core.registrate;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Base class for registry objects that have a name and description.
 * Provides common functionality for registry objects like getting registry name,
 * description ID, and holder reference.
 *
 * @param <T> The type of the implementing class (self-referential generic)
 */
public class NamedObject<T extends NamedObject<T>> {
    private final Registry<T> registry;
    private String desc = null;
    private ResourceLocation id = null;

    /**
     * Creates a new named object with the specified registry.
     *
     * @param registry The forge registry this object belongs to
     */
    public NamedObject(Registry<T> registry) {
        this.registry = registry;
    }

    /**
     * Gets the translation key for this object.
     *
     * @return The translation key string
     */
    public String getDescriptionId() {
        if (desc != null)
            return desc;
        ResourceLocation resourceLocation = getRegistryName();
        ResourceLocation reg = registry.key().location();
        desc = reg.getPath() + "." + resourceLocation.getNamespace() + "." + resourceLocation.getPath();
        return desc;
    }

    /**
     * Gets a translatable component for this object's description.
     *
     * @return A translatable component using this object's description ID
     */
    public MutableComponent getDesc() {
        return Component.translatable(getDescriptionId());
    }

    /**
     * Gets the registry name for this object.
     *
     * @return The ResourceLocation that identifies this object in its registry
     * @throws IllegalStateException if this object is not registered
     */
    public ResourceLocation getRegistryName() {
        if (id != null) return id;
        id = registry.getKey(getThis());
        if (id == null) {
            throw new IllegalStateException("Entry %s is not registered".formatted(getClass().getSimpleName()));
        }
        return id;
    }

    /**
     * Gets the string representation of this object's registry name.
     *
     * @return The string representation of the registry name
     */
    public String getID() {
        return getRegistryName().toString();
    }

    /**
     * Gets this object cast to its implementing type.
     *
     * @return This object cast to T
     * &#064;suppress  Warning suppressed because this cast is guaranteed to work by the class design
     */
    @SuppressWarnings("unchecked")
    public T getThis() {
        return (T) this;
    }

    /**
     * Gets a holder wrapper for this object.
     *
     * @return A holder containing this object
     */
    public Holder<T> holder() {
        return registry.wrapAsHolder(getThis());
    }
}