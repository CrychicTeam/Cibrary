package org.pickaid.pibrary.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;
import org.pickaid.pibrary.content.context.action.engine.core.*;
import org.pickaid.pibrary.content.context.action.engine.logic.*;
import org.pickaid.pibrary.content.context.action.engine.modifier.*;
import org.pickaid.pibrary.content.context.action.engine.processor.*;
import org.pickaid.pibrary.content.context.action.engine.selector.*;
import org.pickaid.pibrary.content.context.action.engine.iterator.*;
import org.pickaid.pibrary.content.context.action.engine.particle.*;
import org.pickaid.pibrary.content.context.action.engine.sound.SoundInstance;
import org.pickaid.pibrary.content.context.action.entity.core.Pirojectile;
import org.pickaid.pibrary.content.context.action.entity.core.MotionType;
import org.pickaid.pibrary.content.context.action.entity.motion.SimpleMotion;
import org.pickaid.pibrary.content.context.action.entity.motion.MovePosMotion;
import org.pickaid.pibrary.content.context.action.entity.motion.MoveDeltaMotion;
import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticleType;
import org.pickaid.pibrary.content.context.action.particle.engine.*;
import org.pickaid.pibrary.content.context.action.entity.engine.ArrowShoot;
import org.pickaid.pibrary.content.context.action.entity.engine.CustomProjectileShoot;
import org.pickaid.pibrary.content.context.action.entity.engine.TridentShoot;

public class LibraryObjects {
    public static final RegistryObject<ModifierType<ForwardOffsetModifier>> FORWARD =
            LibraryRegistries.MODIFIER_REGISTRY.register("forward", () -> ModifierType.of(ForwardOffsetModifier.CODEC));
    public static final RegistryObject<ModifierType<NormalOffsetModifier>> NORMAL_OFFSET =
            LibraryRegistries.MODIFIER_REGISTRY.register("normal_offset", () -> ModifierType.of(NormalOffsetModifier.CODEC));
    public static final RegistryObject<ModifierType<RotationModifier>> ROTATE =
            LibraryRegistries.MODIFIER_REGISTRY.register("rotate", () -> ModifierType.of(RotationModifier.CODEC));
    public static final RegistryObject<ModifierType<OffsetModifier>> OFFSET =
            LibraryRegistries.MODIFIER_REGISTRY.register("offset", () -> ModifierType.of(OffsetModifier.CODEC));
    public static final RegistryObject<ModifierType<SetPosModifier>> POSITION =
            LibraryRegistries.MODIFIER_REGISTRY.register("set_position", () -> ModifierType.of(SetPosModifier.CODEC));
    public static final RegistryObject<ModifierType<SetDirectionModifier>> DIRECTION =
            LibraryRegistries.MODIFIER_REGISTRY.register("direction", () -> ModifierType.of(SetDirectionModifier.CODEC));
    public static final RegistryObject<ModifierType<RandomOffsetModifier>> RANDOM_OFFSET =
            LibraryRegistries.MODIFIER_REGISTRY.register("random_offset", () -> ModifierType.of(RandomOffsetModifier.CODEC));
    public static final RegistryObject<ModifierType<SetNormalModifier>> NORMAL =
            LibraryRegistries.MODIFIER_REGISTRY.register("set_normal", () -> ModifierType.of(SetNormalModifier.CODEC));
    public static final RegistryObject<ModifierType<Dir2NormalModifier>> DIR_2_NORMAL =
            LibraryRegistries.MODIFIER_REGISTRY.register("direction_to_normal", () -> ModifierType.of(Dir2NormalModifier.CODEC));
    public static final RegistryObject<ModifierType<Normal2DirModifier>> NORMAL_2_DIR =
            LibraryRegistries.MODIFIER_REGISTRY.register("normal_to_direction", () -> ModifierType.of(Normal2DirModifier.CODEC));
    public static final RegistryObject<ModifierType<ToCurrentCasterPosModifier>> TO_CASTER_POS =
            LibraryRegistries.MODIFIER_REGISTRY.register("move_to_caster", () -> ModifierType.of(ToCurrentCasterPosModifier.CODEC));
    public static final RegistryObject<ModifierType<ToCurrentCasterDirModifier>> TO_CASTER_DIR =
            LibraryRegistries.MODIFIER_REGISTRY.register("align_with_caster", () -> ModifierType.of(ToCurrentCasterDirModifier.CODEC));

    public static final RegistryObject<EngineType<PredicateLogic>> IF =
            LibraryRegistries.ENGINE_REGISTRY.register("if", () -> EngineType.of(PredicateLogic.CODEC));
    public static final RegistryObject<EngineType<ListLogic>> LIST =
            LibraryRegistries.ENGINE_REGISTRY.register("list", () -> EngineType.of(ListLogic.CODEC));
    public static final RegistryObject<EngineType<DelayLogic>> DELAY =
            LibraryRegistries.ENGINE_REGISTRY.register("delay", () -> EngineType.of(DelayLogic.CODEC));
    public static final RegistryObject<EngineType<RandomVariableLogic>> RANDOM =
            LibraryRegistries.ENGINE_REGISTRY.register("random", () -> EngineType.of(RandomVariableLogic.CODEC));
    public static final RegistryObject<EngineType<MoveEngine>> MOVE_ENGINE =
            LibraryRegistries.ENGINE_REGISTRY.register("move", () -> EngineType.of(MoveEngine.CODEC));
    public static final RegistryObject<EngineType<ProcessorEngine>> PROCESS_ENGINE =
            LibraryRegistries.ENGINE_REGISTRY.register("processor", () -> EngineType.of(ProcessorEngine.CODEC));

    public static final RegistryObject<EngineType<LoopIterator>> ITERATE =
            LibraryRegistries.ENGINE_REGISTRY.register("iterate", () -> EngineType.of(LoopIterator.CODEC));
    public static final RegistryObject<EngineType<DelayedIterator>> ITERATE_DELAY =
            LibraryRegistries.ENGINE_REGISTRY.register("iterate_delayed", () -> EngineType.of(DelayedIterator.CODEC));
    public static final RegistryObject<EngineType<LinearIterator>> ITERATE_LINEAR =
            LibraryRegistries.ENGINE_REGISTRY.register("iterate_linear", () -> EngineType.of(LinearIterator.CODEC));
    public static final RegistryObject<EngineType<RingIterator>> ITERATE_ARC =
            LibraryRegistries.ENGINE_REGISTRY.register("iterate_arc", () -> EngineType.of(RingIterator.CODEC));
    public static final RegistryObject<EngineType<RingRandomIterator>> RANDOM_FAN =
            LibraryRegistries.ENGINE_REGISTRY.register("random_pos_fan", () -> EngineType.of(RingRandomIterator.CODEC));
    public static final RegistryObject<EngineType<SphereRandomIterator>> RANDOM_SPHERE =
            LibraryRegistries.ENGINE_REGISTRY.register("random_pos_sphere", () -> EngineType.of(SphereRandomIterator.CODEC));

    public static final RegistryObject<EngineType<SimpleParticleInstance>> SIMPLE_PARTICLE =
            LibraryRegistries.ENGINE_REGISTRY.register("particle", () -> EngineType.of(SimpleParticleInstance.CODEC));
    public static final RegistryObject<EngineType<BlockParticleInstance>> BLOCK_PARTICLE =
            LibraryRegistries.ENGINE_REGISTRY.register("block_particle", () -> EngineType.of(BlockParticleInstance.CODEC));
    public static final RegistryObject<EngineType<ItemParticleInstance>> ITEM_PARTICLE =
            LibraryRegistries.ENGINE_REGISTRY.register("item_particle", () -> EngineType.of(ItemParticleInstance.CODEC));
    public static final RegistryObject<EngineType<DustParticleInstance>> DUST_PARTICLE =
            LibraryRegistries.ENGINE_REGISTRY.register("dust_particle", () -> EngineType.of(DustParticleInstance.CODEC));
    public static final RegistryObject<EngineType<TransitionParticleInstance>> TRANSITION_PARTICLE =
            LibraryRegistries.ENGINE_REGISTRY.register("transition_particle", () -> EngineType.of(TransitionParticleInstance.CODEC));
    public static final RegistryObject<EngineType<CustomParticleInstance>> CUSTOM_PARTICLE =
            LibraryRegistries.ENGINE_REGISTRY.register("custom_particle", () -> EngineType.of(CustomParticleInstance.CODEC));

    public static final RegistryObject<EngineType<SoundInstance>> SOUND =
            LibraryRegistries.ENGINE_REGISTRY.register("sound", () -> EngineType.of(SoundInstance.CODEC));

    public static final RegistryObject<EngineType<ArrowShoot>> ARROW =
            LibraryRegistries.ENGINE_REGISTRY.register("arrow", () -> EngineType.of(ArrowShoot.CODEC));
    public static final RegistryObject<EngineType<TridentShoot>> TRIDENT =
            LibraryRegistries.ENGINE_REGISTRY.register("trident", () -> EngineType.of(TridentShoot.CODEC));
    public static final RegistryObject<EngineType<CustomProjectileShoot>> CUSTOM_SHOOT =
            LibraryRegistries.ENGINE_REGISTRY.register("custom_projectile", () -> EngineType.of(CustomProjectileShoot.CODEC));

    public static final RegistryObject<SelectorType<SelfSelector>> SELF =
            LibraryRegistries.SELECTOR_REGISTRY.register("self", () -> SelectorType.of(SelfSelector.CODEC));
    public static final RegistryObject<SelectorType<BoxSelector>> BOX =
            LibraryRegistries.SELECTOR_REGISTRY.register("box", () -> SelectorType.of(BoxSelector.CODEC));
    public static final RegistryObject<SelectorType<MoveSelector>> MOVE_SELECTOR =
            LibraryRegistries.SELECTOR_REGISTRY.register("move", () -> SelectorType.of(MoveSelector.CODEC));
    public static final RegistryObject<SelectorType<CompoundEntitySelector>> COMPOUND =
            LibraryRegistries.SELECTOR_REGISTRY.register("compound", () -> SelectorType.of(CompoundEntitySelector.CODEC));
    public static final RegistryObject<SelectorType<LinearCubeSelector>> LINEAR =
            LibraryRegistries.SELECTOR_REGISTRY.register("line", () -> SelectorType.of(LinearCubeSelector.CODEC));
    public static final RegistryObject<SelectorType<ArcCubeSelector>> ARC =
            LibraryRegistries.SELECTOR_REGISTRY.register("arc", () -> SelectorType.of(ArcCubeSelector.CODEC));
    public static final RegistryObject<SelectorType<ApproxCylinderSelector>> CYLINDER =
            LibraryRegistries.SELECTOR_REGISTRY.register("cylinder", () -> SelectorType.of(ApproxCylinderSelector.CODEC));
    public static final RegistryObject<SelectorType<ApproxBallSelector>> BALL =
            LibraryRegistries.SELECTOR_REGISTRY.register("ball", () -> SelectorType.of(ApproxBallSelector.CODEC));

    public static final RegistryObject<ProcessorType<DamageProcessor>> DAMAGE =
            LibraryRegistries.PROCESSOR_REGISTRY.register("damage", () -> ProcessorType.of(DamageProcessor.CODEC));
    public static final RegistryObject<ProcessorType<KnockBackProcessor>> KB =
            LibraryRegistries.PROCESSOR_REGISTRY.register("knockback", () -> ProcessorType.of(KnockBackProcessor.CODEC));
    public static final RegistryObject<ProcessorType<PushProcessor>> PUSH_ENTITY =
            LibraryRegistries.PROCESSOR_REGISTRY.register("push", () -> ProcessorType.of(PushProcessor.CODEC));
    public static final RegistryObject<ProcessorType<EffectProcessor>> EFFECT =
            LibraryRegistries.PROCESSOR_REGISTRY.register("effect", () -> ProcessorType.of(EffectProcessor.CODEC));
    public static final RegistryObject<ProcessorType<PropertyProcessor>> PROP =
            LibraryRegistries.PROCESSOR_REGISTRY.register("property", () -> ProcessorType.of(PropertyProcessor.CODEC));
    public static final RegistryObject<ProcessorType<TeleportProcessor>> TP =
            LibraryRegistries.PROCESSOR_REGISTRY.register("teleport", () -> ProcessorType.of(TeleportProcessor.CODEC));

    public static final RegistryObject<MotionType<SimpleMotion>> SIMPLE_MOTION =
            LibraryRegistries.MOTION_REGISTRY.register("simple", () -> MotionType.of(SimpleMotion.CODEC));
    public static final RegistryObject<MotionType<MovePosMotion>> MOVE_MOTION =
            LibraryRegistries.MOTION_REGISTRY.register("control_position", () -> MotionType.of(MovePosMotion.CODEC));
    public static final RegistryObject<MotionType<MoveDeltaMotion>> DELTA_MOTION =
            LibraryRegistries.MOTION_REGISTRY.register("control_velocity", () -> MotionType.of(MoveDeltaMotion.CODEC));

    public static final RegistryObject<ParticleRenderType<SimpleParticleData>> SIMPLE_RENDER =
            LibraryRegistries.PARTICLE_RENDERER_REGISTRY.register("simple", () -> ParticleRenderType.of(SimpleParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<DustParticleData>> COLOR_RENDER =
            LibraryRegistries.PARTICLE_RENDERER_REGISTRY.register("color", () -> ParticleRenderType.of(DustParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<TransitionParticleData>> TRANSITION_RENDER =
            LibraryRegistries.PARTICLE_RENDERER_REGISTRY.register("transition", () -> ParticleRenderType.of(TransitionParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<BlockParticleData>> BLOCK_RENDER =
            LibraryRegistries.PARTICLE_RENDERER_REGISTRY.register("block", () -> ParticleRenderType.of(BlockParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<ItemParticleData>> ITEM_RENDER =
            LibraryRegistries.PARTICLE_RENDERER_REGISTRY.register("item", () -> ParticleRenderType.of(ItemParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<StaticTextureParticleData>> STATIC_RENDER =
            LibraryRegistries.PARTICLE_RENDERER_REGISTRY.register("static", () -> ParticleRenderType.of(StaticTextureParticleData.CODEC));
    public static final RegistryObject<ParticleRenderType<OrientedParticleData>> ORIENTED_RENDER =
            LibraryRegistries.PARTICLE_RENDERER_REGISTRY.register("oriented", () -> ParticleRenderType.of(OrientedParticleData.CODEC));

    public static final RegistryObject<EntityType<Pirojectile>> GENERIC_PROJECTILE = LibraryRegistries.ENTITY.register(
            "generic_projectile",
            () -> EntityType.Builder.<Pirojectile>of(Pirojectile::new, MobCategory.MISC)
                    .setShouldReceiveVelocityUpdates(false)
                    .updateInterval(100)
                    .sized(0.01f, 0.01f)
                    .clientTrackingRange(4)
                    .build("generic_projectile")
    );

    public static final RegistryObject<PiGenericParticleType> GENERIC_PARTICLE = LibraryRegistries.PARTICLE_TYPE.register(
            "generic_particle",
            PiGenericParticleType::new
    );

    public static void register() {}
} 