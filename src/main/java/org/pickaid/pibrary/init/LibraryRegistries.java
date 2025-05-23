package org.pickaid.pibrary.init;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.content.context.action.engine.core.ProcessorType;
import org.pickaid.pibrary.content.context.action.engine.core.SelectorType;
import org.pickaid.pibrary.content.context.action.engine.helper.EngineRegistryInstance;
import org.pickaid.pibrary.content.context.action.entity.core.MotionType;
import org.pickaid.pibrary.content.context.action.entity.core.ProjectileConfig;
import org.pickaid.pibrary.content.context.action.entity.renderer.ProjectileRenderType;
import org.pickaid.pibrary.content.context.action.particle.engine.ParticleRenderType;
import org.pickaid.pibrary.content.context.conditions.ContextConditionType;
import org.pickaid.pibrary.content.context.conditions.ContextPredicate;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;

import java.util.function.Supplier;

public class LibraryRegistries {
    public static final ResourceKey<Registry<ContextPredicate>> PREDICATE = Pibrary.REG.create("predicate");
    public static final ResourceKey<Registry<ContextConditionType<?>>> PREDICATE_TYPE_KEY = Pibrary.REG.create("predicate_type");
    public static final ResourceKey<Registry<InteractionAction>> ACTION = Pibrary.REG.create("action");
    public static final ResourceKey<Registry<ProjectileConfig>> PROJECTILE = Pibrary.REG.create("projectile");

    public static final ResourceKey<Registry<EngineType<?>>> ENGINE_REGISTRY_KEY = 
        ResourceKey.createRegistryKey(new ResourceLocation(Pibrary.MOD_ID, "configured_engine"));
    public static final ResourceKey<Registry<ModifierType<?>>> MODIFIER_REGISTRY_KEY = 
        ResourceKey.createRegistryKey(new ResourceLocation(Pibrary.MOD_ID, "modifier"));
    public static final ResourceKey<Registry<SelectorType<?>>> SELECTOR_REGISTRY_KEY = 
        ResourceKey.createRegistryKey(new ResourceLocation(Pibrary.MOD_ID, "selector"));
    public static final ResourceKey<Registry<ProcessorType<?>>> PROCESSOR_REGISTRY_KEY = 
        ResourceKey.createRegistryKey(new ResourceLocation(Pibrary.MOD_ID, "processor"));
    public static final ResourceKey<Registry<MotionType<?>>> MOTION_REGISTRY_KEY = 
        ResourceKey.createRegistryKey(new ResourceLocation(Pibrary.MOD_ID, "motion"));
    public static final ResourceKey<Registry<ParticleRenderType<?>>> PARTICLE_RENDERER_REGISTRY_KEY = 
        ResourceKey.createRegistryKey(new ResourceLocation(Pibrary.MOD_ID, "particle_renderer"));
    public static final ResourceKey<Registry<ProjectileRenderType<?>>> PROJECTILE_RENDERER_REGISTRY_KEY = 
        ResourceKey.createRegistryKey(new ResourceLocation(Pibrary.MOD_ID, "projectile_renderer"));

    public static final DeferredRegister<ContextConditionType<?>> CONTEXT_PREDICATE_TYPE_DEFERRED = Pibrary.REG.make(PREDICATE_TYPE_KEY);
    public static final DeferredRegister<EntityType<?>> ENTITY = Pibrary.REG.make(Registries.ENTITY_TYPE);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE = Pibrary.REG.make(Registries.PARTICLE_TYPE);

    public static final DeferredRegister<EngineType<?>> ENGINE_REGISTRY = DeferredRegister.create(ENGINE_REGISTRY_KEY, Pibrary.MODID);
    public static final DeferredRegister<ModifierType<?>> MODIFIER_REGISTRY = DeferredRegister.create(MODIFIER_REGISTRY_KEY, Pibrary.MODID);
    public static final DeferredRegister<SelectorType<?>> SELECTOR_REGISTRY = DeferredRegister.create(SELECTOR_REGISTRY_KEY, Pibrary.MODID);
    public static final DeferredRegister<ProcessorType<?>> PROCESSOR_REGISTRY = DeferredRegister.create(PROCESSOR_REGISTRY_KEY, Pibrary.MODID);
    public static final DeferredRegister<MotionType<?>> MOTION_REGISTRY = DeferredRegister.create(MOTION_REGISTRY_KEY, Pibrary.MODID);
    public static final DeferredRegister<ParticleRenderType<?>> PARTICLE_RENDERER_REGISTRY = DeferredRegister.create(PARTICLE_RENDERER_REGISTRY_KEY, Pibrary.MODID);
    public static final DeferredRegister<ProjectileRenderType<?>> PROJECTILE_RENDERER_REGISTRY = DeferredRegister.create(PROJECTILE_RENDERER_REGISTRY_KEY, Pibrary.MODID);

    public static final Supplier<IForgeRegistry<ContextConditionType<?>>> CONTEXT_PREDICATE_TYPE = CONTEXT_PREDICATE_TYPE_DEFERRED.makeRegistry(RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<EngineType<?>>> ENGINE_TYPE = ENGINE_REGISTRY.makeRegistry(RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<ModifierType<?>>> MODIFIER_TYPE = MODIFIER_REGISTRY.makeRegistry(RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<SelectorType<?>>> SELECTOR_TYPE = SELECTOR_REGISTRY.makeRegistry(RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<ProcessorType<?>>> PROCESSOR_TYPE = PROCESSOR_REGISTRY.makeRegistry(RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<MotionType<?>>> MOTION_TYPE = MOTION_REGISTRY.makeRegistry(RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<ParticleRenderType<?>>> PARTICLE_RENDERER_TYPE = PARTICLE_RENDERER_REGISTRY.makeRegistry(RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<ProjectileRenderType<?>>> PROJECTILE_RENDERER_TYPE = PROJECTILE_RENDERER_REGISTRY.makeRegistry(RegistryBuilder::new);

    private static void onDataPackRegistryNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(PREDICATE, ContextPredicate.DIRECT_CODEC, ContextPredicate.DIRECT_CODEC);
        event.dataPackRegistry(ACTION, InteractionAction.CODEC, InteractionAction.CODEC);
        event.dataPackRegistry(PROJECTILE, ProjectileConfig.CODEC, ProjectileConfig.CODEC);
    }

    public static void register(IEventBus bus) {
        CONTEXT_PREDICATE_TYPE_DEFERRED.register(bus);
        MODIFIER_REGISTRY.register(bus);
        ENGINE_REGISTRY.register(bus);
        SELECTOR_REGISTRY.register(bus);
        PROCESSOR_REGISTRY.register(bus);
        MOTION_REGISTRY.register(bus);
        PARTICLE_RENDERER_REGISTRY.register(bus);
        PROJECTILE_RENDERER_REGISTRY.register(bus);
        bus.addListener(LibraryRegistries::onDataPackRegistryNewRegistry);
    }
}