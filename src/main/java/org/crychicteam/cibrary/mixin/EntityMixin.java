package org.crychicteam.cibrary.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import org.crychicteam.cibrary.content.event.StandOnFluidEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = Entity.class, priority = 1000)
public class EntityMixin {
    public EntityMixin() {}

    @ModifyVariable(
            method = {"move"},
            ordinal = 1,
            index = 3,
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    public Vec3 fluidCollision(Vec3 original) {
        Entity var3 = (Entity) (Object) this;
        if (var3 instanceof Player entity) {
            if (original.y > 0.0) {
                return original;
            } else {
                Level level = entity.getCommandSenderWorld();
                double[][] offsets = new double[][]{{0.5, 0.0, 0.5}, {0.5, 0.0, 0.0}, {0.5, -1.0, 0.0}, {0.5, 0.0, -0.5}, {0.0, 0.0, 0.5}, {0.0, 0.0, 0.0}, {0.0, -1.0, 0.0}, {0.0, 0.0, -0.5}, {-0.5, 0.0, 0.5}, {-0.5, 0.0, 0.0}, {-0.5, -1.0, 0.0}, {-0.5, 0.0, -0.5}};
                double highestValue = original.y;
                FluidState highestFluid = null;
                double[][] var8 = offsets;
                int var9 = offsets.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    double[] offset = var8[var10];
                    BlockPos sourcePos = entity.blockPosition();
                    BlockPos pos = BlockPos.containing((double)sourcePos.getX() + offset[0], (double)sourcePos.getY() + offset[1], (double)sourcePos.getZ() + offset[2]);
                    FluidState fluidState = level.getFluidState(pos);
                    if (!fluidState.isEmpty()) {
                        VoxelShape shape = Shapes.block().move((double)pos.getX(), (double)((float)pos.getY() + fluidState.getOwnHeight()), (double)pos.getZ());
                        if (Shapes.joinIsNotEmpty(shape, Shapes.create(entity.getBoundingBox().inflate(0.5)), BooleanOp.AND)) {
                            double height = shape.max(Direction.Axis.Y) - entity.getY() - 1.0;
                            if (highestValue < height) {
                                highestValue = height;
                                highestFluid = fluidState;
                            }
                        }
                    }
                }

                if (highestFluid == null) {
                    return original;
                } else {
                    StandOnFluidEvent event = new StandOnFluidEvent(entity, highestFluid);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (event.isCanceled()) {
                        entity.fallDistance = 0.0F;
                        entity.setOnGround(true);
                        return new Vec3(original.x, highestValue, original.z);
                    } else {
                        return original;
                    }
                }
            }
        } else {
            return original;
        }
    }
}
