package org.crychicteam.cibrary;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Cibrary.MOD_ID)
public class Cibrary
{
	public static final String MOD_ID = "cibrary";
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}

	public static Logger LOGGER = LogUtils.getLogger();
}
