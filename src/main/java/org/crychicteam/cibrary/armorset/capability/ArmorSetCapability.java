package org.crychicteam.cibrary.armorset.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.armorset.ArmorSet;
import org.crychicteam.cibrary.armorset.ArmorSetManager;
import org.crychicteam.cibrary.armorset.DefaultArmorSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

public class ArmorSetCapability implements INBTSerializable<CompoundTag>, ICapabilityProvider {
    public static final Capability<ArmorSetCapability> ARMOR_SET_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final ResourceLocation ARMOR_SET_CAPABILITY_ID = new ResourceLocation(Cibrary.MOD_ID,"armor_set");

    private ArmorSet activeSet;

    public ArmorSetCapability() {
        this.activeSet = ArmorSetManager.getInstance().getArmorSetByIdentifier(DefaultArmorSet.DEFAULT_IDENTIFIER);
    }

    public ArmorSet getActiveSet() {
        return activeSet;
    }

    public void setActiveSet(ArmorSet set) {
        this.activeSet = set != null ? set : ArmorSetManager.getInstance().getArmorSetByIdentifier(DefaultArmorSet.DEFAULT_IDENTIFIER);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("activeSet", activeSet.getIdentifier());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        String identifier = nbt.getString("activeSet");
        this.activeSet = ArmorSetManager.getInstance().getArmorSetByIdentifier(identifier);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ArmorSetCapability.class);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return ARMOR_SET_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
    }
}