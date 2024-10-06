package org.crychicteam.cibrary;

import com.mojang.logging.LogUtils;
import dev.xkmc.l2damagetracker.contents.attack.AttackEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.crychicteam.cibrary.api.registry.armorset.ArmorSetRegistry;
import org.crychicteam.cibrary.content.armorset.ArmorSetAttackListener;
import org.crychicteam.cibrary.content.armorset.ArmorSetManager;
import org.crychicteam.cibrary.content.armorset.capability.ArmorSetCapability;
import org.crychicteam.cibrary.content.events.ArmorSetHandler;
import org.crychicteam.cibrary.content.armorset.integration.CuriosIntegration;
import org.crychicteam.cibrary.network.CibraryNetworkHandler;
import org.crychicteam.cibrary.content.sound.CibrarySoundManager;
import org.slf4j.Logger;

@Mod(Cibrary.MOD_ID)
public class Cibrary
{
	public static final String MOD_ID = "cibrary";
	public static Logger LOGGER = LogUtils.getLogger();
	public static final ArmorSetManager ARMOR_SET_MANAGER = ArmorSetManager.getInstance();
	public static final CibrarySoundManager SOUND_MANAGER = CibrarySoundManager.getInstance();

	public Cibrary() {
		FMLJavaModLoadingContext ctx = FMLJavaModLoadingContext.get();
		IEventBus bus = ctx.getModEventBus();
		bus.addListener(this::onCommonSetup);
		bus.addListener(this::initializeArmorSets);
		MinecraftForge.EVENT_BUS.register(new ArmorSetHandler());

		AttackEventHandler.register(4000, new ArmorSetAttackListener(ARMOR_SET_MANAGER));
		CuriosIntegration.init();
		bus.addListener(ArmorSetCapability::register);
	}

	private void initializeArmorSets(FMLCommonSetupEvent event) {
		event.enqueueWork(ArmorSetRegistry::registerAll);
	}

	public void onCommonSetup(FMLCommonSetupEvent event) {
		CibraryNetworkHandler.init();
	}
}