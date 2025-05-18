package org.pickaid.pibrary.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.context.action.engine.helper.EngineRegistryInstance;
import org.pickaid.pibrary.content.context.action.entity.renderer.ProjectileRenderType;
import org.pickaid.pibrary.content.context.conditions.ContextConditionType;
import org.pickaid.pibrary.content.context.conditions.ContextPredicate;
import org.pickaid.pibrary.content.context.action.engine.core.*;
import org.pickaid.pibrary.content.context.action.engine.logic.*;
import org.pickaid.pibrary.content.context.action.engine.modifier.*;
import org.pickaid.pibrary.content.context.action.engine.processor.*;
import org.pickaid.pibrary.content.context.action.engine.selector.*;
import org.pickaid.pibrary.content.context.action.entity.core.MotionType;
import org.pickaid.pibrary.content.context.action.entity.core.ProjectileConfig;
import org.pickaid.pibrary.content.context.action.entity.motion.SimpleMotion;
import org.pickaid.pibrary.content.context.action.particle.engine.*;
import org.pickaid.pibrary.content.context.skill.Action;

public class LibraryRegistries {
    public static final ResourceKey<Registry<ContextPredicate>> PREDICATE = Pibrary.REG.create("predicate");
    public static final ResourceKey<Registry<ContextConditionType<?>>> PREDICATE_TYPE_KEY = Pibrary.REG.create("predicate_type");

    public static final DeferredRegister<ContextConditionType<?>> CONTEXT_PREDICATE_TYPE_DEFERRED = Pibrary.REG.make(PREDICATE_TYPE_KEY);

    public static final ResourceKey<Registry<Action>> ACTION = Pibrary.REG.create("action");
    public static final ResourceKey<Registry<ProjectileConfig>> PROJECTILE = Pibrary.REG.create("projectile");

    public static final EngineRegistryInstance<EngineType<?>> ENGINE = EngineRegistryInstance.of("configured_engine");
    public static final EngineRegistryInstance<ModifierType<?>> MODIFIER = EngineRegistryInstance.of("modifier");
    public static final EngineRegistryInstance<SelectorType<?>> SELECTOR = EngineRegistryInstance.of("selector");
    public static final EngineRegistryInstance<ProcessorType<?>> PROCESSOR = EngineRegistryInstance.of("processor");
    public static final EngineRegistryInstance<MotionType<?>> MOTION = EngineRegistryInstance.of("motion");
    public static final EngineRegistryInstance<ParticleRenderType<?>> PARTICLE_RENDERER = EngineRegistryInstance.of("particle_renderer");
    public static final EngineRegistryInstance<ProjectileRenderType<?>> PROJECTILE_RENDERER = EngineRegistryInstance.of("projectile_renderer");

    public static final DeferredRegister<EngineType<?>> ENGINE_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(Pibrary.source("configured_engine")), Pibrary.MODID);
    public static final DeferredRegister<ModifierType<?>> MODIFIER_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(Pibrary.source("modifier")), Pibrary.MODID);
    public static final DeferredRegister<SelectorType<?>> SELECTOR_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(Pibrary.source("selector")), Pibrary.MODID);
    public static final DeferredRegister<ProcessorType<?>> PROCESSOR_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(Pibrary.source("processor")), Pibrary.MODID);
    public static final DeferredRegister<MotionType<?>> MOTION_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(Pibrary.source("motion")), Pibrary.MODID);
    public static final DeferredRegister<ParticleRenderType<?>> PARTICLE_RENDERER_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(Pibrary.source("particle_renderer")), Pibrary.MODID);

    public static final RegistryObject<ModifierType<ForwardOffsetModifier>> FORWARD =
            MODIFIER_REGISTRY.register("forward", () -> ModifierType.of(ForwardOffsetModifier.CODEC));
    public static final RegistryObject<ModifierType<NormalOffsetModifier>> NORMAL_OFFSET =
            MODIFIER_REGISTRY.register("normal_offset", () -> ModifierType.of(NormalOffsetModifier.CODEC));
    public static final RegistryObject<ModifierType<RotationModifier>> ROTATE =
            MODIFIER_REGISTRY.register("rotate", () -> ModifierType.of(RotationModifier.CODEC));
    public static final RegistryObject<ModifierType<OffsetModifier>> OFFSET =
            MODIFIER_REGISTRY.register("offset", () -> ModifierType.of(OffsetModifier.CODEC));
    public static final RegistryObject<ModifierType<SetPosModifier>> POSITION =
            MODIFIER_REGISTRY.register("set_position", () -> ModifierType.of(SetPosModifier.CODEC));
    public static final RegistryObject<ModifierType<SetDirectionModifier>> DIRECTION =
            MODIFIER_REGISTRY.register("direction", () -> ModifierType.of(SetDirectionModifier.CODEC));

    public static final RegistryObject<EngineType<PredicateLogic>> IF =
            ENGINE_REGISTRY.register("if", () -> EngineType.of(PredicateLogic.CODEC));
    public static final RegistryObject<EngineType<ListLogic>> LIST =
            ENGINE_REGISTRY.register("list", () -> EngineType.of(ListLogic.CODEC));
    public static final RegistryObject<EngineType<DelayLogic>> DELAY =
            ENGINE_REGISTRY.register("delay", () -> EngineType.of(DelayLogic.CODEC));

    public static final RegistryObject<SelectorType<SelfSelector>> SELF =
            SELECTOR_REGISTRY.register("self", () -> new SelectorType<>(SelfSelector.CODEC));
    public static final RegistryObject<SelectorType<BoxSelector>> BOX =
            SELECTOR_REGISTRY.register("box", () -> new SelectorType<>(BoxSelector.CODEC));

    public static final RegistryObject<ProcessorType<DamageProcessor>> DAMAGE =
            PROCESSOR_REGISTRY.register("damage", () -> ProcessorType.of(DamageProcessor.CODEC));
    public static final RegistryObject<ProcessorType<KnockBackProcessor>> KB =
            PROCESSOR_REGISTRY.register("knockback", () -> ProcessorType.of(KnockBackProcessor.CODEC));

    public static final RegistryObject<MotionType<SimpleMotion>> SIMPLE_MOTION =
            MOTION_REGISTRY.register("simple", () -> MotionType.of(SimpleMotion.CODEC));

    private static void onDataPackRegistryNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(PREDICATE, ContextPredicate.DIRECT_CODEC);
    }

    public static void register(IEventBus bus) {
        CONTEXT_PREDICATE_TYPE_DEFERRED.register(bus);
        ENGINE_REGISTRY.register(bus);
        MODIFIER_REGISTRY.register(bus);
        SELECTOR_REGISTRY.register(bus);
        PROCESSOR_REGISTRY.register(bus);
        MOTION_REGISTRY.register(bus);
        PARTICLE_RENDERER_REGISTRY.register(bus);

        bus.addListener(LibraryRegistries::onDataPackRegistryNewRegistry);
    }
}