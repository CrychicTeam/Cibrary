package org.pickaid.pibrary.tools.utils.raytrace;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.pickaid.pibrary.tools.Proxy;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class EntityTarget {
    public static final ArrayList<EntityTarget> LIST = new ArrayList();
    public final double max_distance;
    public final double max_angle;
    public final int max_time;
    public int time;
    public Entity target;

    public EntityTarget(double max_distance, double max_angle, int max_time) {
        this.max_distance = max_distance;
        this.max_angle = max_angle;
        this.max_time = max_time;
        LIST.add(this);
    }

    public void updateTarget(@Nullable Entity entity) {
        if (this.target != entity) {
            this.onChange(entity);
        }

        this.target = entity;
        this.time = 0;
    }

    public void onChange(@Nullable Entity entity) {
    }

    @OnlyIn(Dist.CLIENT)
    public void tickRender() {
        if (this.target != null) {
            Player player = Proxy.getClientPlayer();
            if (player == null) {
                this.updateTarget((Entity)null);
            } else {
                ItemStack stack = ((Player)player).getMainHandItem();
                int distance = 0;
                Item var5 = stack.getItem();
                if (var5 instanceof IGlowingTarget glow) {
                    distance = glow.getDistance(stack);
                }

                if (distance == 0) {
                    this.updateTarget((Entity)null);
                } else {
                    Vec3 pos_a = ((Player)player).getEyePosition();
                    Vec3 vec = ((Player)player).getViewVector(1.0F);
                    Vec3 pos_b = this.target.getPosition(1.0F);
                    Vec3 diff = pos_b.subtract(pos_a);
                    double dot = diff.dot(vec);
                    double len_d = diff.length();
                    double len_v = vec.length();
                    double angle = Math.acos(dot / len_d / len_v);
                    double dist = Math.sin(angle) * len_d;
                    if (angle > this.max_angle && dist > this.max_distance) {
                        this.updateTarget((Entity)null);
                    }

                    ++this.time;
                    if (this.time >= this.max_time) {
                        this.updateTarget((Entity)null);
                    }

                }
            }
        }
    }
}
