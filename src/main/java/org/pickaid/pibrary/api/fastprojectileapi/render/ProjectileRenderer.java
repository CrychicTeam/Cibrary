package org.pickaid.pibrary.api.fastprojectileapi.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.pickaid.pibrary.api.fastprojectileapi.entity.SimplifiedProjectile;


public interface ProjectileRenderer<T extends SimplifiedProjectile> {

	Quaternionf cameraOrientation();

	void render(T e, float pTick, PoseStack pose);

	double fading(SimplifiedProjectile e);

}