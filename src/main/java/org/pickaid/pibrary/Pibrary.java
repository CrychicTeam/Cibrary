package org.pickaid.pibrary;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pickaid.pibrary.api.common.GlobalSoundManager;
import org.pickaid.pibrary.api.core.Reg;
import org.pickaid.pibrary.api.fastprojectileapi.collision.FastMapInit;
import org.pickaid.pibrary.config.SkinLoadConfig;
import org.pickaid.pibrary.content.context.action.engine.selector.SelectionType;
import org.pickaid.pibrary.content.handler.server.PlayerEffectHandler;
import org.pickaid.pibrary.content.handler.server.ServerKeyHandler;
import org.pickaid.pibrary.init.LibraryObjects;
import org.pickaid.pibrary.init.LibraryRegistries;
import org.pickaid.pibrary.init.data.PiDatapackRegistriesGen;
import org.pickaid.pibrary.network.PibraryNetworkHandler;

@Mod(Pibrary.MOD_ID)
public class Pibrary {
	public static final String MOD_ID = "pibrary";
	public static Logger LOGGER = LogManager.getLogger();
	public static final GlobalSoundManager SOUND_MANAGER = GlobalSoundManager.getInstance();
	public static final ServerKeyHandler KEY_HANDLER = ServerKeyHandler.getInstance();
	public static Reg REG = new Reg(MOD_ID);

	public static ResourceLocation source(String path) {
		return new ResourceLocation(Pibrary.MOD_ID, path);
	}

	public Pibrary() {
		FMLJavaModLoadingContext ctx = FMLJavaModLoadingContext.get();
		ModLoadingContext loadingContext = ModLoadingContext.get();
		IEventBus modEventBus = ctx.getModEventBus();
		
		registerEventListeners();
		loadingContext.registerConfig(ModConfig.Type.CLIENT, SkinLoadConfig.SKIN_CONFIG);
		modEventBus.addListener(this::onCommonSetup);
		
		FastMapInit.init();

		LibraryRegistries.register(modEventBus);
		LibraryObjects.register();
		
		modEventBus.addListener(this::onLoadComplete);
		modEventBus.addListener(this::gatherData);
	}

	public static boolean isLoaded(String mod) {
		return ModList.get().isLoaded(mod);
	}

	private void registerEventListeners() {
		MinecraftForge.EVENT_BUS.register(new PlayerEffectHandler());
	}

	public void onCommonSetup(FMLCommonSetupEvent event) {
		PibraryNetworkHandler.register();
	}
	
	private void onLoadComplete(FMLLoadCompleteEvent event) {
		 event.enqueueWork(() -> {
		 	SelectionType MONSTERS_ONLY = new SelectionType(
		 			"MONSTERS_ONLY",
		 			(entity, user) -> entity instanceof Monster monster && monster.isAlive()
		 	);
		 	SelectionType.register(MONSTERS_ONLY);
		 });
	}

	public void gatherData(GatherDataEvent event) {
		boolean run = event.includeServer();
		var gen = event.getGenerator();
		PackOutput output = gen.getPackOutput();
		var pvd = event.getLookupProvider();
		var helper = event.getExistingFileHelper();
		gen.addProvider(run, new PiDatapackRegistriesGen(output, pvd));
	}
}