package org.pickaid.pibrary.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SkinLoadConfig {
    public static ForgeConfigSpec SKIN_CONFIG;

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec.ConfigValue<String> API_URL;
    public static ForgeConfigSpec.ConfigValue<String> API_PARAMS;

    static  {
        BUILDER.push("Skin settings");
        {
            API_URL = BUILDER.comment("set the api used by the current request avatar").define("apiUrl", "https://crafatar.com/avatars/");
            API_PARAMS = BUILDER.comment("set the api used by the current request avatar").define("apiParams", "?size=16");
        }
        BUILDER.pop();

        SKIN_CONFIG = BUILDER.build();
    }
}
