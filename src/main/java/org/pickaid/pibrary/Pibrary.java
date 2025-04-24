package org.pickaid.pibrary;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.Registrate;
import dev.xkmc.l2damagetracker.contents.attack.AttackEventHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.pickaid.pibrary.api.common.GlobalSoundManager;
import org.pickaid.pibrary.api.registry.ArmorSetRegistry;
import org.pickaid.pibrary.content.armorset.SkillKey;
import org.pickaid.pibrary.content.armorset.capability.ArmorSetCapability;
import org.pickaid.pibrary.content.armorset.common.ArmorSetAttackListener;
import org.pickaid.pibrary.content.armorset.integration.CuriosIntegration;
import org.pickaid.pibrary.content.events.server.ArmorSetHandler;
import org.pickaid.pibrary.content.events.server.ServerKeyHandler;
import org.pickaid.pibrary.content.events.server.SetEffectHandler;
import org.pickaid.pibrary.content.key.DefaultKey;
import org.pickaid.pibrary.content.key.KeyRegistry;
import org.pickaid.pibrary.network.PibraryNetworkHandler;
import org.slf4j.Logger;

@Mod(Pibrary.MOD_ID)
public class Pibrary {
	public static final String MOD_ID = "pibrary";
	public static Logger LOGGER = LogUtils.getLogger();
	public static final GlobalSoundManager SOUND_MANAGER = GlobalSoundManager.getInstance();
	public static final ServerKeyHandler KEY_HANDLER = ServerKeyHandler.getInstance();
	public static final Registrate CI_REGISTRATE = Registrate.create(MOD_ID);

	public static ResourceLocation source(String path) {
		return new ResourceLocation(Pibrary.MOD_ID, path);
	}

	public Pibrary() {
		FMLJavaModLoadingContext ctx = FMLJavaModLoadingContext.get();
		IEventBus modEventBus = ctx.getModEventBus();
		keySetUp();

		initializeRegistries(modEventBus);
		registerEventListeners();
		initializeArmorSets(modEventBus);
//		ArmorSetRegistryExample.init();
	}

	public static boolean isLoaded(String mod) {
		return ModList.get().isLoaded(mod);
	}

	private void initializeRegistries(IEventBus modEventBus) {
		ArmorSetRegistry.register(modEventBus);
		modEventBus.addListener(this::onCommonSetup);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void registerEventListeners() {
		MinecraftForge.EVENT_BUS.register(new ArmorSetHandler());
		MinecraftForge.EVENT_BUS.register(new SetEffectHandler());
		if (ModList.get().isLoaded("curios")) {
			MinecraftForge.EVENT_BUS.register(new CuriosIntegration());
		}
	}

	private void initializeArmorSets(IEventBus modEventBus) {
		AttackEventHandler.register(4000, new ArmorSetAttackListener());
		modEventBus.addListener(ArmorSetCapability::register);
	}

	public void onCommonSetup(FMLCommonSetupEvent event) {
		PibraryNetworkHandler.init();
		event.enqueueWork(KeyRegistry::init);
	}

	public void keySetUp() {
		DefaultKey.register();
		SkillKey.init();
	}
}