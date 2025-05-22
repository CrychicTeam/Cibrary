package org.pickaid.pibrary.init;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.context.action.engine.helper.EngineRegistryInstance;
import org.pickaid.pibrary.content.context.action.engine.particle.*;
import org.pickaid.pibrary.content.context.action.engine.sound.SoundInstance;
import org.pickaid.pibrary.content.context.action.entity.core.Pirojectile;
import org.pickaid.pibrary.content.context.action.entity.renderer.*;
import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticleType;
import org.pickaid.pibrary.content.context.conditions.ContextConditionType;
import org.pickaid.pibrary.content.context.conditions.ContextPredicate;
import org.pickaid.pibrary.content.context.action.engine.core.*;
import org.pickaid.pibrary.content.context.action.engine.logic.*;
import org.pickaid.pibrary.content.context.action.engine.modifier.*;
import org.pickaid.pibrary.content.context.action.engine.processor.*;
import org.pickaid.pibrary.content.context.action.engine.selector.*;
import org.pickaid.pibrary.content.context.action.engine.iterator.*;
import org.pickaid.pibrary.content.context.action.entity.core.MotionType;
import org.pickaid.pibrary.content.context.action.entity.core.ProjectileConfig;
import org.pickaid.pibrary.content.context.action.entity.motion.SimpleMotion;
import org.pickaid.pibrary.content.context.action.entity.motion.MovePosMotion;
import org.pickaid.pibrary.content.context.action.entity.motion.MoveDeltaMotion;
import org.pickaid.pibrary.content.context.action.particle.engine.*;
import org.pickaid.pibrary.content.context.action.entity.engine.ArrowShoot;
import org.pickaid.pibrary.content.context.action.entity.engine.CustomProjectileShoot;
import org.pickaid.pibrary.content.context.action.entity.engine.TridentShoot;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;

import java.util.function.Supplier;

public class LibraryRegistries {
    public static final ResourceKey<Registry<ContextPredicate>> PREDICATE = Pibrary.REG.create("predicate");
    public static final ResourceKey<Registry<ContextConditionType<?>>> PREDICATE_TYPE_KEY = Pibrary.REG.create("predicate_type");

    public static final DeferredRegister<ContextConditionType<?>> CONTEXT_PREDICATE_TYPE_DEFERRED = Pibrary.REG.make(PREDICATE_TYPE_KEY);
    public static final DeferredRegister<EntityType<?>> ENTITY = Pibrary.REG.make(Registries.ENTITY_TYPE);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE = Pibrary.REG.make(Registries.PARTICLE_TYPE);

    public static final ResourceKey<Registry<InteractionAction>> ACTION = Pibrary.REG.create("action");
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
    public static final DeferredRegister<ProjectileRenderType<?>> PROJECTILE_RENDERER_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(Pibrary.source("projectile_renderer")), Pibrary.MODID);

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
    public static final RegistryObject<ModifierType<RandomOffsetModifier>> RANDOM_OFFSET =
            MODIFIER_REGISTRY.register("random_offset", () -> ModifierType.of(RandomOffsetModifier.CODEC));
    public static final RegistryObject<ModifierType<SetNormalModifier>> NORMAL =
            MODIFIER_REGISTRY.register("set_normal", () -> ModifierType.of(SetNormalModifier.CODEC));
    public static final RegistryObject<ModifierType<Dir2NormalModifier>> DIR_2_NORMAL =
            MODIFIER_REGISTRY.register("direction_to_normal", () -> ModifierType.of(Dir2NormalModifier.CODEC));
    public static final RegistryObject<ModifierType<Normal2DirModifier>> NORMAL_2_DIR =
            MODIFIER_REGISTRY.register("normal_to_direction", () -> ModifierType.of(Normal2DirModifier.CODEC));
    public static final RegistryObject<ModifierType<ToCurrentCasterPosModifier>> TO_CASTER_POS =
            MODIFIER_REGISTRY.register("move_to_caster", () -> ModifierType.of(ToCurrentCasterPosModifier.CODEC));
    public static final RegistryObject<ModifierType<ToCurrentCasterDirModifier>> TO_CASTER_DIR =
            MODIFIER_REGISTRY.register("align_with_caster", () -> ModifierType.of(ToCurrentCasterDirModifier.CODEC));

    public static final RegistryObject<EngineType<PredicateLogic>> IF =
            ENGINE_REGISTRY.register("if", () -> EngineType.of(PredicateLogic.CODEC));
    public static final RegistryObject<EngineType<ListLogic>> LIST =
            ENGINE_REGISTRY.register("list", () -> EngineType.of(ListLogic.CODEC));
    public static final RegistryObject<EngineType<DelayLogic>> DELAY =
            ENGINE_REGISTRY.register("delay", () -> EngineType.of(DelayLogic.CODEC));
    public static final RegistryObject<EngineType<RandomVariableLogic>> RANDOM =
            ENGINE_REGISTRY.register("random", () -> EngineType.of(RandomVariableLogic.CODEC));
    public static final RegistryObject<EngineType<MoveEngine>> MOVE_ENGINE =
            ENGINE_REGISTRY.register("move", () -> EngineType.of(MoveEngine.CODEC));
    public static final RegistryObject<EngineType<ProcessorEngine>> PROCESS_ENGINE =
            ENGINE_REGISTRY.register("processor", () -> EngineType.of(ProcessorEngine.CODEC));

    public static final RegistryObject<EngineType<LoopIterator>> ITERATE =
            ENGINE_REGISTRY.register("iterate", () -> EngineType.of(LoopIterator.CODEC));
    public static final RegistryObject<EngineType<DelayedIterator>> ITERATE_DELAY =
            ENGINE_REGISTRY.register("iterate_delayed", () -> EngineType.of(DelayedIterator.CODEC));
    public static final RegistryObject<EngineType<LinearIterator>> ITERATE_LINEAR =
            ENGINE_REGISTRY.register("iterate_linear", () -> EngineType.of(LinearIterator.CODEC));
    public static final RegistryObject<EngineType<RingIterator>> ITERATE_ARC =
            ENGINE_REGISTRY.register("iterate_arc", () -> EngineType.of(RingIterator.CODEC));
    public static final RegistryObject<EngineType<RingRandomIterator>> RANDOM_FAN =
            ENGINE_REGISTRY.register("random_pos_fan", () -> EngineType.of(RingRandomIterator.CODEC));
    public static final RegistryObject<EngineType<SphereRandomIterator>> RANDOM_SPHERE =
            ENGINE_REGISTRY.register("random_pos_sphere", () -> EngineType.of(SphereRandomIterator.CODEC));

    public static final RegistryObject<EngineType<SimpleParticleInstance>> SIMPLE_PARTICLE =
            ENGINE_REGISTRY.register("particle", () -> EngineType.of(SimpleParticleInstance.CODEC));
    public static final RegistryObject<EngineType<BlockParticleInstance>> BLOCK_PARTICLE =
            ENGINE_REGISTRY.register("block_particle", () -> EngineType.of(BlockParticleInstance.CODEC));
    public static final RegistryObject<EngineType<ItemParticleInstance>> ITEM_PARTICLE =
            ENGINE_REGISTRY.register("item_particle", () -> EngineType.of(ItemParticleInstance.CODEC));
    public static final RegistryObject<EngineType<DustParticleInstance>> DUST_PARTICLE =
            ENGINE_REGISTRY.register("dust_particle", () -> EngineType.of(DustParticleInstance.CODEC));
    public static final RegistryObject<EngineType<TransitionParticleInstance>> TRANSITION_PARTICLE =
            ENGINE_REGISTRY.register("transition_particle", () -> EngineType.of(TransitionParticleInstance.CODEC));
    public static final RegistryObject<EngineType<CustomParticleInstance>> CUSTOM_PARTICLE =
            ENGINE_REGISTRY.register("custom_particle", () -> EngineType.of(CustomParticleInstance.CODEC));

    public static final RegistryObject<EngineType<SoundInstance>> SOUND =
            ENGINE_REGISTRY.register("sound", () -> EngineType.of(SoundInstance.CODEC));

    public static final RegistryObject<EngineType<ArrowShoot>> ARROW =
            ENGINE_REGISTRY.register("arrow", () -> EngineType.of(ArrowShoot.CODEC));
    public static final RegistryObject<EngineType<TridentShoot>> TRIDENT =
            ENGINE_REGISTRY.register("trident", () -> EngineType.of(TridentShoot.CODEC));
    public static final RegistryObject<EngineType<CustomProjectileShoot>> CUSTOM_SHOOT =
            ENGINE_REGISTRY.register("custom_projectile", () -> EngineType.of(CustomProjectileShoot.CODEC));

    public static final RegistryObject<SelectorType<SelfSelector>> SELF =
            SELECTOR_REGISTRY.register("self", () -> SelectorType.of(SelfSelector.CODEC));
    public static final RegistryObject<SelectorType<BoxSelector>> BOX =
            SELECTOR_REGISTRY.register("box", () -> SelectorType.of(BoxSelector.CODEC));
    public static final RegistryObject<SelectorType<MoveSelector>> MOVE_SELECTOR =
            SELECTOR_REGISTRY.register("move", () -> SelectorType.of(MoveSelector.CODEC));
    public static final RegistryObject<SelectorType<CompoundEntitySelector>> COMPOUND =
            SELECTOR_REGISTRY.register("compound", () -> SelectorType.of(CompoundEntitySelector.CODEC));
    public static final RegistryObject<SelectorType<LinearCubeSelector>> LINEAR =
            SELECTOR_REGISTRY.register("line", () -> SelectorType.of(LinearCubeSelector.CODEC));
    public static final RegistryObject<SelectorType<ArcCubeSelector>> ARC =
            SELECTOR_REGISTRY.register("arc", () -> SelectorType.of(ArcCubeSelector.CODEC));
    public static final RegistryObject<SelectorType<ApproxCylinderSelector>> CYLINDER =
            SELECTOR_REGISTRY.register("cylinder", () -> SelectorType.of(ApproxCylinderSelector.CODEC));
    public static final RegistryObject<SelectorType<ApproxBallSelector>> BALL =
            SELECTOR_REGISTRY.register("ball", () -> SelectorType.of(ApproxBallSelector.CODEC));

    public static final RegistryObject<ProcessorType<DamageProcessor>> DAMAGE =
            PROCESSOR_REGISTRY.register("damage", () -> ProcessorType.of(DamageProcessor.CODEC));
    public static final RegistryObject<ProcessorType<KnockBackProcessor>> KB =
            PROCESSOR_REGISTRY.register("knockback", () -> ProcessorType.of(KnockBackProcessor.CODEC));
    public static final RegistryObject<ProcessorType<PushProcessor>> PUSH_ENTITY =
            PROCESSOR_REGISTRY.register("push", () -> ProcessorType.of(PushProcessor.CODEC));
    public static final RegistryObject<ProcessorType<EffectProcessor>> EFFECT =
            PROCESSOR_REGISTRY.register("effect", () -> ProcessorType.of(EffectProcessor.CODEC));
    public static final RegistryObject<ProcessorType<PropertyProcessor>> PROP =
            PROCESSOR_REGISTRY.register("property", () -> ProcessorType.of(PropertyProcessor.CODEC));
    public static final RegistryObject<ProcessorType<TeleportProcessor>> TP =
            PROCESSOR_REGISTRY.register("teleport", () -> ProcessorType.of(TeleportProcessor.CODEC));

    public static final RegistryObject<MotionType<SimpleMotion>> SIMPLE_MOTION =
            MOTION_REGISTRY.register("simple", () -> MotionType.of(SimpleMotion.CODEC));
    public static final RegistryObject<MotionType<MovePosMotion>> MOVE_MOTION =
            MOTION_REGISTRY.register("control_position", () -> MotionType.of(MovePosMotion.CODEC));
    public static final RegistryObject<MotionType<MoveDeltaMotion>> DELTA_MOTION =
            MOTION_REGISTRY.register("control_velocity", () -> MotionType.of(MoveDeltaMotion.CODEC));

    public static final RegistryObject<ParticleRenderType<SimpleParticleData>> SIMPLE_RENDER =
            PARTICLE_RENDERER_REGISTRY.register("simple", () -> ParticleRenderType.of(SimpleParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<DustParticleData>> COLOR_RENDER =
            PARTICLE_RENDERER_REGISTRY.register("color", () -> ParticleRenderType.of(DustParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<TransitionParticleData>> TRANSITION_RENDER =
            PARTICLE_RENDERER_REGISTRY.register("transition", () -> ParticleRenderType.of(TransitionParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<BlockParticleData>> BLOCK_RENDER =
            PARTICLE_RENDERER_REGISTRY.register("block", () -> ParticleRenderType.of(BlockParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<ItemParticleData>> ITEM_RENDER =
            PARTICLE_RENDERER_REGISTRY.register("item", () -> ParticleRenderType.of(ItemParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<StaticTextureParticleData>> STATIC_RENDER =
            PARTICLE_RENDERER_REGISTRY.register("static", () -> ParticleRenderType.of(StaticTextureParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<OrientedParticleData>> ORIENTED_RENDER =
            PARTICLE_RENDERER_REGISTRY.register("oriented", () -> ParticleRenderType.of(OrientedParticleData.CODEC));

    public static final Supplier<IForgeRegistry<ContextConditionType<?>>> CONTEXT_PREDICATE_TYPE = CONTEXT_PREDICATE_TYPE_DEFERRED.makeRegistry(RegistryBuilder::new);

    public static final RegistryObject<EntityType<Pirojectile>> GENERIC_PROJECTILE = ENTITY.register(
            "generic_projectile",
            () -> EntityType.Builder.<Pirojectile>of(Pirojectile::new, MobCategory.MISC)
                    .setShouldReceiveVelocityUpdates(false)
                    .updateInterval(100)
                    .sized(0.01f, 0.01f)
                    .clientTrackingRange(4)
                    .build("generic_projectile")
    );

    public static final RegistryObject<PiGenericParticleType> GENERIC_PARTICLE = PARTICLE_TYPE.register(
            "generic_particle",
            PiGenericParticleType::new
    );

    private static void onDataPackRegistryNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(PREDICATE, ContextPredicate.DIRECT_CODEC);
        event.dataPackRegistry(PROJECTILE, ProjectileConfig.CODEC);
    }

    public static void register(IEventBus bus) {
        CONTEXT_PREDICATE_TYPE_DEFERRED.register(bus);
        ENGINE_REGISTRY.register(bus);
        MODIFIER_REGISTRY.register(bus);
        SELECTOR_REGISTRY.register(bus);
        PROCESSOR_REGISTRY.register(bus);
        MOTION_REGISTRY.register(bus);
        PARTICLE_RENDERER_REGISTRY.register(bus);
        PROJECTILE_RENDERER_REGISTRY.register(bus);

        bus.addListener(LibraryRegistries::onDataPackRegistryNewRegistry);
    }
}