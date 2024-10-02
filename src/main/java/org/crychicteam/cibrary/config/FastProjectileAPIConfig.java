package org.crychicteam.cibrary.config;
import net.minecraftforge.common.ForgeConfigSpec;

public class FastProjectileAPIConfig {

    public static final ForgeConfigSpec.BooleanValue laserRenderAdditive;
    public static final ForgeConfigSpec.BooleanValue laserRenderInverted;
    private static final ForgeConfigSpec.DoubleValue laserTransparency;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        laserRenderAdditive = builder.define("laserRenderAdditive", true);
        laserRenderInverted = builder.define("laserRenderInverted", true);
        laserTransparency = builder.defineInRange("laserTransparency", 0.5, 0, 1);
    }

    public static boolean isLaserRenderAdditive() {
        return laserRenderAdditive.get();
    }

    public static boolean isLaserRenderInverted() {
        return laserRenderInverted.get();
    }

    public static double getLaserTransparency() {
        return laserTransparency.get();
    }
}