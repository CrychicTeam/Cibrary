package org.pickaid.pibrary.runtime.registrate.host;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.service.PiChunkServiceType;
import org.pickaid.pibrary.api.service.PiChunkServices;
import org.pickaid.pibrary.api.service.PiLevelServiceType;
import org.pickaid.pibrary.api.service.PiLevelServices;
import org.pickaid.pibrary.api.service.PiLivingServiceType;
import org.pickaid.pibrary.api.service.PiLivingServices;
import org.pickaid.pibrary.api.service.PiStateChunkService;
import org.pickaid.pibrary.api.service.PiStateLevelService;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;
import org.pickaid.pibrary.runtime.chunk.PiChunkServiceDescriptors;
import org.pickaid.pibrary.runtime.level.PiLevelServiceDescriptors;
import org.pickaid.pibrary.runtime.service.PiLivingServiceDescriptors;

public final class PiRegistrateHostBridge {
    private PiRegistrateHostBridge() {
    }

    public static <S, T extends PiStateLivingEntityService<S>> PiLivingServiceType<T> registerLiving(
            PiRegistrate owner,
            String path,
            Class<S> stateType,
            Class<T> serviceType
    ) {
        ResourceLocation expectedId = Objects.requireNonNull(owner, "owner").id(path);
        var descriptor = PiLivingServiceDescriptors.requireGenerated(serviceType);
        validate(expectedId, stateType, descriptor.id(), descriptor.stateType(), serviceType);
        return PiLivingServices.host(serviceType).register();
    }

    public static <S, T extends PiStateChunkService<S>> PiChunkServiceType<T> registerChunk(
            PiRegistrate owner,
            String path,
            Class<S> stateType,
            Class<T> serviceType
    ) {
        ResourceLocation expectedId = Objects.requireNonNull(owner, "owner").id(path);
        var descriptor = PiChunkServiceDescriptors.requireGenerated(serviceType);
        validate(expectedId, stateType, descriptor.id(), descriptor.stateType(), serviceType);
        return PiChunkServices.host(serviceType).register();
    }

    public static <S, T extends PiStateLevelService<S>> PiLevelServiceType<T> registerLevel(
            PiRegistrate owner,
            String path,
            Class<S> stateType,
            Class<T> serviceType
    ) {
        ResourceLocation expectedId = Objects.requireNonNull(owner, "owner").id(path);
        var descriptor = PiLevelServiceDescriptors.requireGenerated(serviceType);
        validate(expectedId, stateType, descriptor.id(), descriptor.stateType(), serviceType);
        return PiLevelServices.host(serviceType).register();
    }

    private static void validate(
            ResourceLocation expectedId,
            Class<?> expectedStateType,
            ResourceLocation actualId,
            Class<?> actualStateType,
            Class<?> serviceType
    ) {
        Objects.requireNonNull(expectedId, "expectedId");
        Objects.requireNonNull(expectedStateType, "expectedStateType");
        if (!expectedId.equals(actualId)) {
            throw new IllegalArgumentException("Pi registrate host entry id " + expectedId
                    + " does not match generated descriptor id " + actualId + " for " + serviceType.getName());
        }
        if (!expectedStateType.equals(actualStateType)) {
            throw new IllegalArgumentException("Pi registrate host entry state type " + expectedStateType.getName()
                    + " does not match generated descriptor state type " + actualStateType.getName()
                    + " for " + serviceType.getName());
        }
    }
}
