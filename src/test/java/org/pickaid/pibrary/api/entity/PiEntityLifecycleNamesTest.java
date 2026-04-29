package org.pickaid.pibrary.api.entity;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class PiEntityLifecycleNamesTest {
    @Test
    void entityLifecycleAccessorUsesRegistryName() {
        PiEntityLifecycleRegistry previous = PiEntityLifecycles.find().orElse(null);
        PiEntityLifecycleRegistry custom = new PiEntityLifecycleRegistry() {
            @Override
            public <E extends net.minecraft.world.entity.Entity> void register(
                    Class<E> entityType,
                    PiEntityLifecycleHandler<? super E> handler
            ) {
            }

            @Override
            public void onJoinLevel(
                    net.minecraft.world.entity.Entity entity,
                    net.minecraft.world.level.Level level,
                    boolean loadedFromDisk
            ) {
            }

            @Override
            public void onEnterSection(
                    net.minecraft.world.entity.Entity entity,
                    net.minecraft.core.SectionPos oldSection,
                    net.minecraft.core.SectionPos newSection
            ) {
            }
        };

        try {
            PiEntityLifecycles.install(custom);
            assertSame(custom, PiEntityLifecycles.require());
        } finally {
            PiEntityLifecycles.install(previous);
        }
    }

    @Test
    void vehicleLifecycleAccessorUsesRegistryName() {
        PiVehicleLifecycleRegistry previous = PiVehicleLifecycles.find().orElse(null);
        PiVehicleLifecycleRegistry custom = new PiVehicleLifecycleRegistry() {
            @Override
            public <E extends net.minecraft.world.entity.Entity> void register(
                    Class<E> entityType,
                    PiVehicleLifecycleHandler<? super E> handler
            ) {
            }

            @Override
            public void onMount(
                    net.minecraft.world.entity.Entity entity,
                    net.minecraft.world.entity.Entity vehicle,
                    net.minecraft.world.level.Level level
            ) {
            }

            @Override
            public void onDismount(
                    net.minecraft.world.entity.Entity entity,
                    net.minecraft.world.entity.Entity vehicle,
                    net.minecraft.world.level.Level level
            ) {
            }
        };

        try {
            PiVehicleLifecycles.install(custom);
            assertSame(custom, PiVehicleLifecycles.require());
        } finally {
            PiVehicleLifecycles.install(previous);
        }
    }
}
