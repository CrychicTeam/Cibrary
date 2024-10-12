package org.crychicteam.cibrary.content.armorset.capability;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.crychicteam.cibrary.Cibrary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArmorSetCapability {
    public static final Capability<IArmorSetCapability> ARMOR_SET_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final ResourceLocation ARMOR_SET_CAPABILITY_ID = new ResourceLocation(Cibrary.MOD_ID, "armor_set");

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IArmorSetCapability.class);
    }

    public static class Provider implements ICapabilityProvider {
        private final LazyOptional<IArmorSetCapability> instance = LazyOptional.of(ArmorSetCapabilityHandler::new);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return ARMOR_SET_CAPABILITY.orEmpty(cap, instance);
        }
    }
}