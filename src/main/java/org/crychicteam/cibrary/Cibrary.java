package org.crychicteam.cibrary;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.crychicteam.cibrary.sound.CibrarySoundManager;
import org.crychicteam.cibrary.sound.CibrarySoundManagerHandler;
import org.slf4j.Logger;

@Mod(Cibrary.MOD_ID)
public class Cibrary
{
	public static final String MOD_ID = "cibrary";
	public static Logger LOGGER = LogUtils.getLogger();

	public static final CibrarySoundManager SOUND_MANAGER = CibrarySoundManager.getInstance();
	public static final CibrarySoundManagerHandler CLIENT_SOUND_MANAGER = CibrarySoundManagerHandler.getInstance();

	public Cibrary() {
		FMLJavaModLoadingContext ctx = FMLJavaModLoadingContext.get();
		IEventBus bus = ctx.getModEventBus();
	}
}
