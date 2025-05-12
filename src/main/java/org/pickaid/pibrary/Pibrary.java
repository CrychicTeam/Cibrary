package org.pickaid.pibrary;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.pickaid.pibrary.api.common.GlobalSoundManager;
import org.pickaid.pibrary.api.effect.IPlayerEffect;
import org.pickaid.pibrary.content.events.server.PlayerEffectHandler;
import org.pickaid.pibrary.content.events.server.ServerKeyHandler;
import org.pickaid.pibrary.content.key.DefaultKey;
import org.pickaid.pibrary.content.key.combo.ComboRegistrationSample;
import org.pickaid.pibrary.network.PibraryNetworkHandler;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(Pibrary.MOD_ID)
public class Pibrary {
	public static final String MOD_ID = "pibrary";
	public static Logger LOGGER = LogUtils.getLogger();
	public static final GlobalSoundManager SOUND_MANAGER = GlobalSoundManager.getInstance();
	public static final ServerKeyHandler KEY_HANDLER = ServerKeyHandler.getInstance();

	public static Map<LivingEntity, IPlayerEffect> activeEffects = new HashMap<>();

	public static ResourceLocation source(String path) {
		return new ResourceLocation(Pibrary.MOD_ID, path);
	}

	public Pibrary() {
		FMLJavaModLoadingContext ctx = FMLJavaModLoadingContext.get();
		IEventBus modEventBus = ctx.getModEventBus();
		registerEventListeners();
		modEventBus.addListener(this::onCommonSetup);
	}

	public static boolean isLoaded(String mod) {
		return ModList.get().isLoaded(mod);
	}

	private void registerEventListeners() {
		MinecraftForge.EVENT_BUS.register(new PlayerEffectHandler());
	}

	public void onCommonSetup(FMLCommonSetupEvent event) {
		PibraryNetworkHandler.init();
		keySetUp();
	}

	public void keySetUp() {
		DefaultKey.register();
	}
}